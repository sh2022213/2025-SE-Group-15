package com.personalfinance.controller;

import com.google.gson.reflect.TypeToken;
import com.personalfinance.model.Budget;
import com.personalfinance.model.Transaction;
import com.personalfinance.model.User;
import com.personalfinance.storage.JsonDataManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;   
import java.util.stream.Collectors;  
                                                     
public class FinanceController {
    private final AIAnalyzer        aiAnalyzer;
    private final JsonDataManager   dataManager;
    private       List<Transaction> transactions;
    private       List<Budget>      budgets;
    private       User              user;

    public FinanceController() {
        this.aiAnalyzer = new AIAnalyzer(this);
        this.dataManager = new JsonDataManager();
        loadAllData();
        initializeDefaultData();
    }

    public AIAnalyzer getAIAnalyzer() {
        return aiAnalyzer;
    }

    // 核心数据操作方法
    // ==============================================

    private void loadAllData() {
        // 加载交易记录
        this.transactions = dataManager.loadCollection(
                "transactions.json",
                new TypeToken<List<Transaction>>() {}
        );

        // 加载预算
        this.budgets = dataManager.loadCollection(
                "budgets.json",
                new TypeToken<List<Budget>>() {}
        );

        // 加载用户数据
        this.user = dataManager.load("user.json", User.class);
        if (this.user == null) {
            this.user = new User();
        }
    }

    private void initializeDefaultData() {
        // 如果没有任何交易记录，初始化一些示例数据
        if (transactions.isEmpty()) {
            Calendar cal = Calendar.getInstance();

            // 添加示例收入
            transactions.add(new Transaction(
                    new BigDecimal("15000.00"),
                    "Salary",
                    "INCOME",
                    cal.getTime(),
                    "monthly pay"
            ));

            // 添加示例支出
            cal.add(Calendar.DATE, -5);
            transactions.add(new Transaction(
                    new BigDecimal("2500.00"),
                    "Housing",
                    "EXPENSE",
                    cal.getTime(),
                    "Rent for November"
            ));

            saveTransactions();
        }

        // 如果没有预算数据，初始化默认预算
        if (budgets.isEmpty()) {
            Calendar cal = Calendar.getInstance();
            Date startDate = cal.getTime();
            cal.add(Calendar.MONTH, 1);
            Date endDate = cal.getTime();

            budgets.add(new Budget("Food", new BigDecimal("1000.00"), startDate, endDate));
            budgets.add(new Budget("Transportation", new BigDecimal("500.00"), startDate, endDate));
            budgets.add(new Budget("Shopping", new BigDecimal("1500.00"), startDate, endDate));

            saveBudgets();
        }

        // 更新预算的实际支出
        updateBudgetSpending();
    }

    private void updateBudgetSpending() {
        budgets.forEach(budget -> {
            BigDecimal spent = transactions.stream()
                    .filter(t -> t.getCategory() != null)
                    .filter(t -> t.getCategory().equals(budget.getCategory()))
                    .filter(t -> t.getType().equals("EXPENSE"))
                    .filter(t -> !t.getDate().before(budget.getStartDate()))
                    .filter(t -> !t.getDate().after(budget.getEndDate()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            budget.setSpentAmount(spent);
        });
    }

    // 交易记录相关方法
    // ==============================================

    public void addTransaction(Transaction transaction) {
        transaction.setId(UUID.randomUUID().toString());
        transactions.add(transaction);
        saveTransactions();
        updateBudgetSpending();
    }

    public void updateTransaction(Transaction transaction) {
        transactions.removeIf(t -> t.getId().equals(transaction.getId()));
        transactions.add(transaction);
        saveTransactions();
        updateBudgetSpending();
    }

    public boolean deleteTransaction(String transactionId) {
        boolean removed = transactions.removeIf(t -> t.getId().equals(transactionId));
        if (removed) {
            dataManager.save("transactions.json", transactions);
            saveTransactions();
            updateBudgetSpending();
            return true;
        }
        return false;
    }

    public List<Transaction> getTransactionsByMonth(int year, int month) {
        return transactions.stream()
                .filter(t -> {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(t.getDate());
                    return cal.get(Calendar.YEAR) == year &&
                            cal.get(Calendar.MONTH) == month - 1;
                })
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                .collect(Collectors.toList());
    }

    public List<Transaction> getRecentTransactions(int count) {
        return transactions.stream()
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                .limit(count)
                .collect(Collectors.toList());
    }

    // 预算相关方法
    // ==============================================

    public void addBudget(Budget budget) {
        budget.setId(UUID.randomUUID().toString());
        budgets.add(budget);
        saveBudgets();
        updateBudgetSpending();
    }

    public void updateBudget(Budget budget) {
        budgets.removeIf(b -> b.getId().equals(budget.getId()));
        budgets.add(budget);
        saveBudgets();
        updateBudgetSpending();
    }

    public void deleteBudget(String budgetId) {
        budgets.removeIf(b -> b.getId().equals(budgetId));
        saveBudgets();
    }

    // 统计分析相关方法
    // ==============================================

    public Map<String, BigDecimal> getCategorySpending() {
        Map<String, BigDecimal> result = new HashMap<>();

        // 初始化所有分类
        user.getCategories().forEach(category ->
                result.put(category, BigDecimal.ZERO));

        // 计算实际支出
        transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .forEach(t -> result.merge(t.getCategory(), t.getAmount(), BigDecimal::add));

        return result;
    }

    public Map<String, BigDecimal> getMonthlySpendingTrend() {
        Map<String, BigDecimal> trend = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        // 初始化最近12个月
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            String month = monthFormat.format(cal.getTime());
            trend.put(month, BigDecimal.ZERO);
            cal.add(Calendar.MONTH, -1);
        }

        // 填充实际数据
        transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .forEach(t -> {
                    String month = monthFormat.format(t.getDate());
                    trend.computeIfPresent(month, (k, v) -> v.add(t.getAmount()));
                });

        return trend;
    }

    public BigDecimal getCurrentBalance() {
        BigDecimal income = getTotalIncome();
        BigDecimal expense = getTotalSpending();
        return income.subtract(expense);
    }

    public BigDecimal getTotalIncome() {
        return transactions.stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalSpending() {
        return transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAverageSpending() {
        long count = transactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .count();

        return count > 0 ? getTotalSpending().divide(new BigDecimal(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    public BigDecimal getMonthlyIncome() {
        Calendar cal = Calendar.getInstance();
        return getTransactionsByMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1).stream()
                .filter(t -> "INCOME".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getMonthlyExpense() {
        Calendar cal = Calendar.getInstance();
        return getTransactionsByMonth(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1).stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 数据持久化方法
    // ==============================================

    public void saveTransactions() {
        dataManager.save("transactions.json", transactions);
    }

    public void saveBudgets() {
        dataManager.save("budgets.json", budgets);
    }

    public void saveUser() {
        dataManager.save("user.json", user);
    }

    // Getter方法
    // ==============================================

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    public List<Budget> getBudgets() {
        return new ArrayList<>(budgets);
    }

    public User getUser() {
        return user;
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (user.validatePassword(oldPassword)) {
            user.setPassword(newPassword);
            saveUser();
            return true;
        }
        return false;
    }

    /**
     * 检查用户名是否存在
     */
    public boolean userExists(String username) {
        User existingUser = dataManager.load("user.json", User.class);
        return existingUser != null && existingUser.getUsername().equals(username);
    }

    /**
     * 注册新用户
     */
    public boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        dataManager.save("user.json", newUser);
        this.user = newUser;
        return true;
    }

}
