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
 * AI Financial Analysis Engine
 */
public class AIAnalyzer {
    private static final int RECENT_MONTHS = 6;
    private static final BigDecimal SAVINGS_RATE_TARGET = new BigDecimal("0.2");
    private static final BigDecimal MONTHLY_VARIATION_THRESHOLD = new BigDecimal("0.3");
    private static final int STANDARD_DEVIATION_MULTIPLIER = 2;
    
    private final FinanceController controller;
    private final CategoryMatcher categoryMatcher;

    public AIAnalyzer(FinanceController controller) {
        this.controller = controller;
        this.categoryMatcher = new CategoryMatcher(controller);
    }

    // ========== Public API Methods ==========

    public String getSpendingHabitsReport() {
        List<Transaction> transactions = controller.getTransactions();
        if (transactions.isEmpty()) {
            return "No transaction data available for analysis at the moment";
        }

        ReportBuilder report = new ReportBuilder()
            .appendHeader("Consumer Habit Analysis Report")
            
            // 1. Main consumption categories
            .appendSection("Main consumption categories:", 
                generateCategorySpendingAnalysis(transactions))
            
            // 2. Monthly consumption trend
            .appendSection("Monthly consumption trend:", 
                generateMonthlyTrendAnalysis(transactions))
            
            // 3. Abnormal consumption detection
            .appendSection("Abnormal consumption detection:", 
                generateAnomalyAnalysis(transactions))
            
            // 4. Budget recommendations
            .appendSection("Budget recommendations:", 
                generateBudgetAdvice());

        return report.build();
    }

    public List<Map<String, String>> detectAnomalies(List<Transaction> transactions) {
        List<Transaction> expenses = filterExpenses(transactions);
        if (expenses.isEmpty()) return Collections.emptyList();

        BigDecimal average = calculateAverageSpending(expenses);
        BigDecimal stdDev = calculateStandardDeviation(expenses, average);
        BigDecimal threshold = average.add(stdDev.multiply(new BigDecimal(STANDARD_DEVIATION_MULTIPLIER)));

        return expenses.stream()
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .map(this::createAnomalyRecord)
                .collect(Collectors.toList());
    }

    public List<String> generateBudgetAdvice() {
        List<String> advice = new ArrayList<>();
        BigDecimal totalIncome = calculateTotalIncome();
        BigDecimal totalSpending = calculateTotalSpending(controller.getTransactions());
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalSpending);

        advice.add(generateSavingsRateAdvice(savingsRate));
        advice.addAll(generateTopCategoryAdvice());
        
        if (hasSignificantMonthlyVariation()) {
            advice.add("Detected significant fluctuations in monthly expenses, it is recommended to balance monthly consumption!");
        }

        return advice;
    }

    public Map<String, BigDecimal> getSpendingForecast() {
        Map<String, BigDecimal> monthlyTrend = calculateMonthlyTrend(controller.getTransactions());
        BigDecimal avgLast3Months = calculateRecentMonthsAverage(monthlyTrend, 3);

        LocalDate now = LocalDate.now();
        Map<String, BigDecimal> forecast = new LinkedHashMap<>();
        for (int i = 1; i <= 3; i++) {
            LocalDate nextMonth = now.plusMonths(i);
            forecast.put(nextMonth.getMonthValue() + "Month", avgLast3Months);
        }

        return forecast;
    }

    public String matchCategory(String description) {
        return categoryMatcher.match(description);
    }

    // ========== Analysis Generation Methods ==========

    private List<String> generateCategorySpendingAnalysis(List<Transaction> transactions) {
        BigDecimal totalSpending = calculateTotalSpending(transactions);
        Map<String, BigDecimal> categorySpending = calculateCategorySpending(transactions);

        return categorySpending.entrySet().stream() 
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(entry -> formatCategorySpending(entry, totalSpending))
                .collect(Collectors.toList());
    }

    private List<String> generateMonthlyTrendAnalysis(List<Transaction> transactions) {
        return calculateMonthlyTrend(transactions).entrySet().stream()
                .map(entry -> String.format(" - %s: %s", entry.getKey(), formatMoney(entry.getValue())))
                .collect(Collectors.toList());
    }

    private List<String> generateAnomalyAnalysis(List<Transaction> transactions) {
        List<Map<String, String>> anomalies = detectAnomalies(transactions);
        if (anomalies.isEmpty()) {
            return Collections.singletonList(" - No obvious abnormal consumption detected");
        }
        
        return anomalies.stream()
                .map(this::formatAnomaly)
                .collect(Collectors.toList());
    }

    private List<String> generateTopCategoryAdvice() {
        Map<String, BigDecimal> categorySpending = calculateCategorySpending(controller.getTransactions());
        BigDecimal totalSpending = calculateTotalSpending(controller.getTransactions());

        return categorySpending.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(entry -> String.format("Main expenditure category 【%s】 accounts for%.1f%%, it is recommended to pay attention to it",
                        entry.getKey(),
                        calculatePercentage(entry.getValue(), totalSpending)))
                .collect(Collectors.toList());
    }

    // ========== Calculation Methods ==========

    private Map<String, BigDecimal> calculateCategorySpending(List<Transaction> transactions) {
        return filterExpenses(transactions).stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));
    }

    private Map<String, BigDecimal> calculateMonthlyTrend(List<Transaction> transactions) {
        Map<String, BigDecimal> trend = new TreeMap<>();
        LocalDate now = LocalDate.now();

        for (int i = RECENT_MONTHS - 1; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String monthKey = month.getMonthValue() + "Month";

            BigDecimal monthlyTotal = filterExpenses(transactions).stream()
                    .filter(t -> isSameMonth(t.getDate(), month))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            trend.put(monthKey, monthlyTotal);
        }

        return trend;
    }

    private BigDecimal calculateTotalSpending(List<Transaction> transactions) {
        return filterExpenses(transactions).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalIncome() {
        return controller.getTransactions().stream()
                .filter(Transaction::isIncome)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageSpending(List<Transaction> transactions) {
        if (transactions.isEmpty()) return BigDecimal.ZERO;
        return calculateTotalSpending(transactions)
                .divide(new BigDecimal(transactions.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateStandardDeviation(List<Transaction> transactions, BigDecimal mean) {
        if (transactions.size() < 2) return BigDecimal.ZERO;

        BigDecimal variance = transactions.stream()
                .map(t -> t.getAmount().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(transactions.size()), 10, RoundingMode.HALF_UP);

        return new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSavingsRate(BigDecimal totalIncome, BigDecimal totalSpending) {
        return totalIncome.compareTo(BigDecimal.ZERO) > 0 ?
                totalIncome.subtract(totalSpending).divide(totalIncome, 4, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
    }

    private BigDecimal calculateRecentMonthsAverage(Map<String, BigDecimal> monthlyTrend, int months) {
        return monthlyTrend.values().stream()
                .limit(months)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(Math.min(months, monthlyTrend.size())), 2, RoundingMode.HALF_UP);
    }

    private double calculatePercentage(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) return 0;
        return part.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100;
    }

    // ========== Helper Methods ==========

    private List<Transaction> filterExpenses(List<Transaction> transactions) {
        return transactions.stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.toList());
    }

    private boolean hasSignificantMonthlyVariation() {
        Map<String, BigDecimal> monthlyTrend = calculateMonthlyTrend(controller.getTransactions());
        if (monthlyTrend.size() < 3) return false;

        BigDecimal avg = calculateRecentMonthsAverage(monthlyTrend, monthlyTrend.size());
        BigDecimal stdDev = calculateStandardDeviation(
                monthlyTrend.values().stream()
                        .map(amount -> new Transaction(null, amount, null, null, true))
                        .collect(Collectors.toList()),
                avg
        );

        return stdDev.compareTo(avg.multiply(MONTHLY_VARIATION_THRESHOLD)) > 0;
    }

    private boolean isSameMonth(Date date, LocalDate localDate) {
        LocalDate transactionDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return transactionDate.getMonth() == localDate.getMonth() &&
                transactionDate.getYear() == localDate.getYear();
    }

    private Map<String, String> createAnomalyRecord(Transaction transaction) {
        Map<String, String> anomaly = new HashMap<>();
        anomaly.put("date", formatDate(transaction.getDate()));
        anomaly.put("amount", transaction.getAmount().toString());
        anomaly.put("description", transaction.getDescription());
        anomaly.put("category", transaction.getCategory());
        return anomaly;
    }

    private String generateSavingsRateAdvice(BigDecimal savingsRate) {
        String status = savingsRate.compareTo(SAVINGS_RATE_TARGET) < 0 ? "relatively low" : "good";
        return String.format("The current savings rate is %s(%s), %s",
                status,
                formatPercentage(savingsRate),
                savingsRate.compareTo(SAVINGS_RATE_TARGET) < 0 ? 
                        "It is recommended to increase the savings ratio to over 20%" : 
                        "Continue to maintain");
    }

    // ========== Formatting Methods ==========

    private String formatCategorySpending(Map.Entry<String, BigDecimal> entry, BigDecimal total) {
        return String.format(" - %s: %s (%.1f%%)",
                entry.getKey(),
                formatMoney(entry.getValue()),
                calculatePercentage(entry.getValue(), total));
    }

    private String formatAnomaly(Map<String, String> anomaly) {
        return String.format(" - [Exception] %s: %s (%s)",
                anomaly.get("date"),
                formatMoney(new BigDecimal(anomaly.get("amount"))),
                anomaly.get("description"));
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

    // ========== Inner Helper Classes ==========

    private static class ReportBuilder {
        private final StringBuilder builder = new StringBuilder();
        
        public ReportBuilder appendHeader(String header) {
            builder.append("=== ").append(header).append(" ===\n\n");
            return this;
        }
        
        public ReportBuilder appendSection(String title, List<String> content) {
            builder.append(title).append("\n");
            content.forEach(line -> builder.append(line).append("\n"));
            builder.append("\n");
            return this;
        }
        
        public String build() {
            return builder.toString();
        }
    }

    private static class CategoryMatcher {
        private final FinanceController controller;
        private final Map<String, List<String>> keywordMap;
        
        public CategoryMatcher(FinanceController controller) {
            this.controller = controller;
            this.keywordMap = createKeywordMap();
        }
        
        public String match(String description) {
            if (description == null || description.trim().isEmpty()) {
                return null;
            }

            String descLower = description.toLowerCase();
            List<String> categories = controller.getUser().getCategories();
            if (categories.isEmpty()) return null;

            // 1. Check keyword matches
            String keywordMatch = matchByKeywords(descLower, categories);
            if (keywordMatch != null) return keywordMatch;

            // 2. Check historical transactions
            String historyMatch = matchByHistory(descLower);
            if (historyMatch != null) return historyMatch;

            // 3. Default category
            return getDefaultCategory(descLower);
        }
        
        private String matchByKeywords(String description, List<String> categories) {
            for (Map.Entry<String, List<String>> entry : keywordMap.entrySet()) {
                String category = entry.getKey();
                if (categories.contains(category)) {
                    for (String keyword : entry.getValue()) {
                        if (description.contains(keyword)) {
                            return category;
                        }
                    }
                }
            }
            return null;
        }
        
        private String matchByHistory(String description) {
            Map<String, Integer> categoryMatchCount = new HashMap<>();
            List<Transaction> userTransactions = controller.getTransactions();

            for (Transaction t : userTransactions) {
                if (t.getDescription() != null && t.getDescription().toLowerCase().contains(description)) {
                    categoryMatchCount.merge(t.getCategory(), 1, Integer::sum);
                }
            }

            return categoryMatchCount.isEmpty() ? null :
                    categoryMatchCount.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);
        }
        
        private String getDefaultCategory(String description) {
            if (description.contains("salary") || description.contains("paycheck")) return "Salary";
            if (description.contains("food") || description.contains("eat")) return "Food";
            if (description.contains("rent") || description.contains("mortgage")) return "Housing";
            
            List<String> categories = controller.getUser().getCategories();
            return categories.isEmpty() ? null : categories.get(0);
        }
        
        private static Map<String, List<String>> createKeywordMap() {
            Map<String, List<String>> map = new HashMap<>();
            
            map.put("Food", Arrays.asList("restaurant", "dining", "takeout", "breakfast", 
                "lunch", "dinner", "snack", "coffee", "bubble tea", "hotpot", "bbq", "food", "eat", "meal"));
            
            map.put("Transportation", Arrays.asList("taxi", "bus", "subway", "train",
                "flight", "plane", "gas", "parking", "uber", "lyft", "transport", "commute"));
                
            map.put("Shopping", Arrays.asList("mall", "supermarket", "online", "taobao", 
                "amazon", "clothes", "shoes", "electronics", "groceries", "purchase", "buy"));
                
            map.put("Housing", Arrays.asList("rent", "utilities", "mortgage", "electricity", 
                "water", "property", "housing", "apartment"));
                
            map.put("Entertainment", Arrays.asList("movie", "cinema", "karaoke", "game", 
                "amusement", "concert", "ticket", "travel", "vacation", "netflix", "spotify"));
                
            map.put("Salary", Arrays.asList("salary", "paycheck", "income", "bonus", 
                "payment", "wage", "earnings"));
                
            map.put("Education", Arrays.asList("school", "tuition", "book", "course", 
                "learning", "education", "student"));
                
            map.put("Gifts", Arrays.asList("gift", "donation", "present", "charity"));
            map.put("Investments", Arrays.asList("stock", "investment", "fund", "savings"));
            map.put("Personal Care", Arrays.asList("haircut", "spa", "beauty", "cosmetics"));
            
            return map;
        }
    }
}