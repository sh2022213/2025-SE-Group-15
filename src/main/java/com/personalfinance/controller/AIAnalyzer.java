package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI财务分析引擎
 */
public class AIAnalyzer {
    private static final int RECENT_MONTHS = 6; // 分析最近6个月数据
    private final        FinanceController controller;

    public AIAnalyzer(FinanceController controller) {
        this.controller = controller;
    }

    /**
     * 获取消费习惯分析报告
     */
    public String getSpendingHabitsReport() {
        List<Transaction> transactions = controller.getTransactions();
        if (transactions.isEmpty()) {
            return "No transaction data available for analysis at the moment";
        }

        StringBuilder report = new StringBuilder();
        report.append("=== Consumer Habit Analysis Report ===\n\n");

        // 1. 主要消费分类
        report.append("1. Main consumption categories:\n");
            Map<String, BigDecimal> categorySpending = getCategorySpending(transactions);
        categorySpending.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(entry -> {
                    report.append(String.format(" - %s: %s (%.1f%%)\n",
                            entry.getKey(),
                            formatMoney(entry.getValue()),
                            getPercentage(entry.getValue(), getTotalSpending(transactions))
                    ));
                });
        report.append("\n");

        // 2. 月度消费趋势
        report.append("2. Monthly consumption trend:\n");
        Map<String, BigDecimal> monthlyTrend = getMonthlyTrend(transactions);
        monthlyTrend.forEach((month, amount) -> {
            report.append(String.format(" - %s: %s\n", month, formatMoney(amount)));
        });
        report.append("\n");

        // 3. 异常消费检测
        report.append("3. Abnormal consumption detection:\n");
        detectAnomalies(transactions).forEach(anomaly -> {
            report.append(String.format(" - [Exception] %s: %s (%s)\n",
                    anomaly.get("date"),
                    formatMoney(new BigDecimal(anomaly.get("amount"))),
                    anomaly.get("description")
            ));
        });
        if (detectAnomalies(transactions).isEmpty()) {
            report.append(" - No obvious abnormal consumption detected\n");
        }
        report.append("\n");

        // 4. 预算执行情况
        report.append("4. Budget recommendations:\n");
        generateBudgetAdvice().forEach(advice -> {
            report.append(String.format(" - %s\n", advice));
        });

        return report.toString();
    }

    /**
     * 检测异常消费（超过平均值的2倍标准差）
     */
    public List<Map<String, String>> detectAnomalies(List<Transaction> transactions) {
        List<Transaction> expenses = transactions.stream()
                .filter(t -> t.isExpense())
                .collect(Collectors.toList());

        if (expenses.isEmpty()) { return Collections.emptyList(); }

        // 计算平均值和标准差
        BigDecimal average = getAverageSpending(expenses);
        BigDecimal stdDev = calculateStandardDeviation(expenses, average);

        // 检测异常（平均值 + 2倍标准差）
        BigDecimal threshold = average.add(stdDev.multiply(new BigDecimal(2)));

        return expenses.stream()
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
                .map(t -> {
                    Map<String, String> anomaly = new HashMap<>();
                    anomaly.put("date", formatDate(t.getDate()));
                    anomaly.put("amount", t.getAmount().toString());
                    anomaly.put("description", t.getDescription());
                    anomaly.put("category", t.getCategory());
                    return anomaly;
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成预算优化建议
     */
    public List<String> generateBudgetAdvice() {
        List<String> advice = new ArrayList<>();
        BigDecimal totalIncome = getTotalIncome();
        BigDecimal totalSpending = getTotalSpending(controller.getTransactions());

        // 储蓄率分析
        BigDecimal savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0 ?
                totalIncome.subtract(totalSpending).divide(totalIncome, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        if (savingsRate.compareTo(new BigDecimal("0.2")) < 0) {
            advice.add("The current savings rate is relatively low(" + formatPercentage(savingsRate) + ")，It is recommended to increase the savings ratio to over 20%");
        } else {
            advice.add("The current savings rate is good(" + formatPercentage(savingsRate) + ")，Continue to maintain");
        }

        // 分类支出建议
        getCategorySpending(controller.getTransactions()).entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    advice.add(String.format("Main expenditure category 【%s】 accounts for%.1f%%, it is recommended to pay attention to it",
                            entry.getKey(),
                            getPercentage(entry.getValue(), totalSpending))
                    );
                });

        // 月度波动建议
        if (hasSignificantMonthlyVariation()) {
            advice.add("Detected significant fluctuations in monthly expenses, it is recommended to balance monthly consumption!");
        }

        return advice;
    }

    /**
     * 获取未来3个月支出预测
     */
    public Map<String, BigDecimal> getSpendingForecast() {
        Map<String, BigDecimal> monthlyTrend = getMonthlyTrend(controller.getTransactions());

        // 简单预测：取最近3个月平均值
        BigDecimal avgLast3Months = monthlyTrend.values().stream()
                .limit(3)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(Math.min(3, monthlyTrend.size())), 2, RoundingMode.HALF_UP);

        LocalDate now = LocalDate.now();
        Map<String, BigDecimal> forecast = new LinkedHashMap<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate nextMonth = now.plusMonths(i);
            forecast.put(nextMonth.getMonthValue() + "Month", avgLast3Months);
        }

        return forecast;
    }

    // ========== 辅助方法 ==========

    private Map<String, BigDecimal> getCategorySpending(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    private Map<String, BigDecimal> getMonthlyTrend(List<Transaction> transactions) {
        Map<String, BigDecimal> trend = new TreeMap<>();
        LocalDate now = LocalDate.now();

        for (int i = RECENT_MONTHS - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.getMonthValue() + "Month";

            BigDecimal monthlyTotal = transactions.stream()
                    .filter(t -> isSameMonth(t.getDate(), month))
                    .filter(Transaction::isExpense)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.put(monthKey, monthlyTotal);
        }

        return trend;
    }

    private BigDecimal getTotalSpending(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalIncome() {
        return controller.getTransactions().stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getAverageSpending(List<Transaction> transactions) {
        if (transactions.isEmpty()) { return BigDecimal.ZERO; }
        BigDecimal total = getTotalSpending(transactions);
        return total.divide(new BigDecimal(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDeviation(List<Transaction> transactions, BigDecimal mean) {
        if (transactions.size() < 2) { return BigDecimal.ZERO; }

        BigDecimal variance = transactions.stream()
                .map(t -> t.getAmount().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(transactions.size()), 10, RoundingMode.HALF_UP);

        return new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasSignificantMonthlyVariation() {
        Map<String, BigDecimal> monthlyTrend = getMonthlyTrend(controller.getTransactions());
        if (monthlyTrend.size() < 3) { return false; }

        BigDecimal avg = monthlyTrend.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(monthlyTrend.size()), 2, RoundingMode.HALF_UP);

        BigDecimal stdDev = monthlyTrend.values().stream()
                .map(v -> v.subtract(avg).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(monthlyTrend.size()), 10, RoundingMode.HALF_UP);
        stdDev = new BigDecimal(Math.sqrt(stdDev.doubleValue()));

        return stdDev.compareTo(avg.multiply(new BigDecimal("0.3"))) > 0;
    }

    private boolean isSameMonth(Date date, LocalDate localDate) {
        LocalDate transactionDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return transactionDate.getMonth() == localDate.getMonth() &&
                transactionDate.getYear() == localDate.getYear();
    }

    private String formatMoney(BigDecimal amount) {
        return "¥" + amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatPercentage(BigDecimal decimal) {
        return decimal.multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP) + "%";
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private double getPercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {return 0;}
        return part.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    /**
     * 根据交易描述智能匹配分类
     * @param description 交易描述
     * @return 匹配到的分类，如果无法匹配则返回null
     */
    public String matchCategory(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }

        // 获取所有可用分类
        List<String> categories = controller.getUser().getCategories();
        if (categories.isEmpty()) {
            return null;
        }

        // 转换为小写方便匹配
        String descLower = description.toLowerCase();

        // 1. 首先检查是否有明确的分类关键词匹配
        Map<String, List<String>> keywordMap = createCategoryKeywordsMap();
        for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
            String category = entry.getKey();
            if (categories.contains(category)) { // 确保该分类在当前用户分类列表中
                for (String keyword : entry.getValue()) {
                    if (descLower.contains(keyword)) {
                        return category;
                    }
                }
            }
        }

        // 2. 如果没有明确匹配，查找用户历史交易中最相似的描述
        Map<String, Integer> categoryMatchCount = new HashMap<>();
        List<Transaction> userTransactions = controller.getTransactions();

        for (Transaction t : userTransactions) {
            if (t.getDescription() != null && t.getDescription().toLowerCase().contains(descLower)) {
                categoryMatchCount.merge(t.getCategory(), 1, Integer::sum);
            }
        }

        if (!categoryMatchCount.isEmpty()) {
            // 返回匹配次数最多的分类
            return categoryMatchCount.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }

        // 3. 如果还是没有匹配，使用默认分类
        return getDefaultCategory(descLower);
    }

    // 创建分类关键词映射表
    private Map<String, List<String>> createCategoryKeywordsMap() {
        Map<String, List<String>> keywordMap = new HashMap<>();

        // 餐饮相关 Food & Dining
        //keywordMap.put("餐饮", Arrays.asList("餐厅", "饭店", "外卖", "早餐", "午餐", "晚餐", "小吃", "咖啡", "奶茶", "火锅", "烧烤"));
        keywordMap.put("Food", Arrays.asList("restaurant", "dining", "takeout", "breakfast", "lunch", "dinner", "snack", "coffee", "bubble tea", "hotpot", "bbq", "food", "eat", "meal"));

        // 交通相关 Transportation
        //keywordMap.put("交通", Arrays.asList("打车", "公交", "地铁", "火车", "高铁", "机票", "出租车", "滴滴", "加油", "停车"));
        keywordMap.put("Transportation", Arrays.asList("taxi", "bus", "subway", "train",
                "flight", "plane", "gas", "parking",
                "uber", "lyft", "transport", "commute"));
        // 购物相关 Shopping
        //keywordMap.put("购物", Arrays.asList("商场", "超市", "网购", "淘宝", "亚马逊", "衣服", "鞋子", "电器", "日用品"));
        keywordMap.put("Shopping", Arrays.asList("mall", "supermarket", "online", "taobao", "amazon", "clothes", "shoes", "electronics", "groceries", "purchase", "buy"));

        // 房租相关 Housing
        //keywordMap.put("房租", Arrays.asList("房租", "水电", "物业", "房贷", "租金"));
        keywordMap.put("Housing", Arrays.asList("rent", "utilities", "mortgage", "electricity", "water", "property", "housing", "apartment"));

        // 娱乐相关 Entertainment
        //keywordMap.put("娱乐", Arrays.asList("电影", "ktv", "游戏", "游乐场", "演唱会", "门票", "旅游", "度假"));
        keywordMap.put("Entertainment", Arrays.asList("movie", "cinema", "karaoke", "game", "amusement", "concert", "ticket", "travel", "vacation", "netflix", "spotify"));

        // 工资收入 Income/Salary
        //keywordMap.put("工资", Arrays.asList("工资", "薪水", "薪资", "奖金", "绩效"));
        keywordMap.put("Salary", Arrays.asList("salary", "paycheck", "income", "bonus", "payment", "wage", "earnings"));

        // Education
        keywordMap.put("Education", Arrays.asList("school", "tuition", "book", "course", "learning", "education", "student"));

        // Other common categories
        keywordMap.put("Gifts", Arrays.asList("gift", "donation", "present", "charity"));
        keywordMap.put("Investments", Arrays.asList("stock", "investment", "fund", "savings"));
        keywordMap.put("Personal Care", Arrays.asList("haircut", "spa", "beauty", "cosmetics"));

        return keywordMap;
    }

    // 获取默认分类
    private String getDefaultCategory(String description) {
        // 这里可以根据描述中的关键词返回更合适的默认分类
        if (description.contains("salary") || description.contains("paycheck")) {
            return "Salary";
        }
        if (description.contains("food") || description.contains("eat")) {
            return "Food";
        }
        if (description.contains("rent") || description.contains("mortgage")) {
            return "Housing";
        }
        // 默认返回第一个分类
        List<String> categories = controller.getUser().getCategories();
        return categories.isEmpty() ? null : categories.get(0);
    }
}
