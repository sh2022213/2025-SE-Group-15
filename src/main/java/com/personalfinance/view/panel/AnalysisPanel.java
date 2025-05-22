package com.personalfinance.view.panel;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.view.util.MoneyUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Map;

public class AnalysisPanel extends JPanel {
    private final FinanceController controller;
    private JFreeChart categoryChart;
    private JFreeChart monthlyTrendChart;
    private JTextArea analysisReport;
    /**
     * 预算执行情况
     */
    private JFreeChart budgetChart;

    public AnalysisPanel(FinanceController controller) {
        this.controller = controller;
        //创建主题样式
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        //设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        //设置图例的字体
        standardChartTheme.setRegularFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        //设置轴向的字体
        standardChartTheme.setLargeFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        //应用主题样式
        ChartFactory.setChartTheme(standardChartTheme);
        setLayout(new BorderLayout());
        initComponents();
        refreshData(); // 初始加载数据
    }

    public void refreshData() {
        refreshCategoryChart();
        refreshMonthlyTrendChart();
        refreshBudgetChart();
        refreshSummaryPanels();
        refreshAiAnalysisData();
    }

    public void refreshAiAnalysisData(){
        analysisReport.setText(controller.getAIAnalyzer().getSpendingHabitsReport());
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. 分类支出分析
        tabbedPane.addTab("Category Expense Analysis", createCategoryAnalysisPanel());

        // 2. 月度趋势分析
        tabbedPane.addTab("Monthly Trend Analysis", createMonthlyTrendPanel());

        // 3. 预算分析
        tabbedPane.addTab("Budget Analysis", createBudgetAnalysisPanel());

        analysisReport = new JTextArea(controller.getAIAnalyzer().getSpendingHabitsReport());
        analysisReport.setEditable(false);
        tabbedPane.addTab("AI Analysis", new JScrollPane(analysisReport));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCategoryAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultPieDataset dataset = new DefaultPieDataset();

        controller.getCategorySpending().forEach((category, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(category + " (" + MoneyUtils.formatMoney(amount) + ")", amount);
            }
        });
        categoryChart = ChartFactory.createPieChart(
                "Expense Category Proportion",
                dataset,
                true, true, false
        );

        PiePlot plot = (PiePlot) categoryChart.getPlot();
        plot.setSectionPaint(0, new Color(79, 129, 189));
        plot.setSectionPaint(1, new Color(192, 80, 77));
        plot.setSectionPaint(2, new Color(155, 187, 89));

        panel.add(new ChartPanel(categoryChart), BorderLayout.CENTER);
        panel.add(createCategorySummaryPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private void refreshCategoryChart() {
        PiePlot plot = (PiePlot) categoryChart.getPlot();
        DefaultPieDataset dataset = (DefaultPieDataset) plot.getDataset();
        dataset.clear();
        controller.getCategorySpending().forEach((category, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(category, amount);
            }
        });
    }

    private JPanel createMonthlyTrendPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        monthlyTrendChart = ChartFactory.createLineChart(
                "Monthly Expense Trend",
                "Month",
                "Amount",
                dataset
        );

        CategoryPlot plot = monthlyTrendChart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(79, 129, 189));

        panel.add(new ChartPanel(monthlyTrendChart), BorderLayout.CENTER);
        return panel;
    }

    private void refreshMonthlyTrendChart() {
        DefaultCategoryDataset dataset = (DefaultCategoryDataset) monthlyTrendChart.getCategoryPlot().getDataset();
        dataset.clear();
        controller.getMonthlySpendingTrend().forEach((month, amount) -> {
            dataset.addValue(amount, "Expense", month);
        });
    }

    private JPanel createBudgetAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        budgetChart = ChartFactory.createBarChart(
                "Budget Execution Status",
                "Category",
                "Amount",
                dataset
        );

        CategoryPlot plot = budgetChart.getCategoryPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(79, 129, 189)); // 预算
        plot.getRenderer().setSeriesPaint(1, new Color(155, 187, 89)); // 实际

        ChartPanel chartPanel = new ChartPanel(budgetChart);
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(createBudgetSummaryPanel(), BorderLayout.SOUTH);
        // 初始刷新数据
        refreshBudgetChart();
        return panel;
    }

    private void refreshBudgetChart() {
        DefaultCategoryDataset dataset = (DefaultCategoryDataset) budgetChart.getCategoryPlot().getDataset();
        dataset.clear();

        controller.getBudgets().forEach(budget -> {
            dataset.addValue(budget.getAmount(), "Budget", budget.getCategory());
            dataset.addValue(budget.getSpentAmount(), "Actual", budget.getCategory());
        });

    }

    private JPanel createCategorySummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        BigDecimal totalSpending = controller.getTotalSpending();
        BigDecimal avgSpending = controller.getAverageSpending();
        String maxCategory = controller.getCategorySpending().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        panel.add(createSummaryCard("Total Expense", formatCurrency(totalSpending), Color.RED));
        panel.add(createSummaryCard("Average Expense", formatCurrency(avgSpending), Color.BLUE));
        panel.add(createSummaryCard("Category with Maximum Expense", maxCategory, new Color(0, 128, 0)));

        return panel;
    }

    private JPanel createBudgetSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        long overBudgetCount = controller.getBudgets().stream()
                .filter(b -> b.isOverBudget())
                .count();

        long onTrackCount = controller.getBudgets().size() - overBudgetCount;

        panel.add(createSummaryCard("Total Number of Budgets", String.valueOf(controller.getBudgets().size()), Color.BLUE));
        panel.add(createSummaryCard("Number of Budgets Over Budget", String.valueOf(overBudgetCount), Color.RED));
        panel.add(createSummaryCard("Number of Budgets on Track", String.valueOf(onTrackCount), new Color(0, 128, 0)));

        return panel;
    }

    private void refreshSummaryPanels() {
        // 在实际应用中可能需要更精细的刷新逻辑
        // 这里简化为重新创建整个面板
        Component[] components = getComponents();
        if (components.length > 0 && components[0] instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) components[0];

            // 刷新分类分析面板
            Component categoryPanel = tabbedPane.getComponentAt(0);
            if (categoryPanel instanceof JPanel) {
                ((JPanel) categoryPanel).removeAll();
                ((JPanel) categoryPanel).add(createCategoryAnalysisPanel());
            }

            // 刷新预算分析面板
            Component budgetPanel = tabbedPane.getComponentAt(2);
            if (budgetPanel instanceof JPanel) {
                ((JPanel) budgetPanel).removeAll();
                ((JPanel) budgetPanel).add(createBudgetAnalysisPanel());
            }
        }
    }

    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }
}
