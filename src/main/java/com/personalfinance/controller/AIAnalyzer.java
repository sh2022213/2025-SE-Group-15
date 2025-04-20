package com.personalfinance.controller;

import com.personalfinance.model.Transaction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return categories.isEmpty() ? "other" : categories.get(0);
    }
}
