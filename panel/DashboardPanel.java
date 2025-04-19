package com.personalfinance.view.panel;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.view.util.MoneyUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

public class DashboardPanel extends JPanel {
    private final FinanceController controller;
    /**
     * 本月支出分类
     */
    private JFreeChart spendingChart;
    /**
     * 预算执行情况
     */
    private JFreeChart budgetChart;
    private ChartPanel spendingChartPanel;
    private ChartPanel budgetChartPanel;
    private JTable recentTransactionsTable;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DashboardPanel(FinanceController controller) {
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

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
        refreshData(); // 初始加载数据
    }

    private void initComponents() {
        // 1. 顶部 - 关键指标
        add(createMetricsPanel(), BorderLayout.NORTH);

        // 2. 中部 - 图表
        JSplitPane centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // 消费分类图表
        DefaultPieDataset spendingDataset = new DefaultPieDataset();
        spendingChart = ChartFactory.createPieChart(
                "This Month's Expense Categories",
                spendingDataset,
                true, true, false
        );
        spendingChartPanel = new ChartPanel(spendingChart);
        spendingChartPanel.setPreferredSize(new Dimension(400, 300));

        // 预算执行图表
        DefaultPieDataset budgetDataset = new DefaultPieDataset();
        budgetChart = ChartFactory.createPieChart(
                "Budget Execution Status",
                budgetDataset,
                true, true, false
        );
        budgetChartPanel = new ChartPanel(budgetChart);
        budgetChartPanel.setPreferredSize(new Dimension(400, 300));

        centerPanel.setLeftComponent(spendingChartPanel);
        centerPanel.setRightComponent(budgetChartPanel);
        centerPanel.setResizeWeight(0.5);
        add(centerPanel, BorderLayout.CENTER);

        // 3. 底部 - 最近交易
        add(createRecentTransactionsPanel(), BorderLayout.SOUTH);
    }

    public void refreshData() {
        refreshSpendingChart();
        refreshBudgetChart();
        refreshRecentTransactions();
        refreshMetrics();
    }

    private void refreshSpendingChart() {
        PiePlot plot = (PiePlot) spendingChart.getPlot();
        DefaultPieDataset dataset = (DefaultPieDataset) plot.getDataset();
        dataset.clear();
        controller.getCategorySpending().forEach((category, amount) -> {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                dataset.setValue(category + " (" + MoneyUtils.formatMoney(amount) + ")", amount);
            }
        });
    }

    private void refreshBudgetChart() {
        PiePlot plot = (PiePlot) budgetChart.getPlot();
        DefaultPieDataset dataset = (DefaultPieDataset) plot.getDataset();
        dataset.clear();
        controller.getBudgets().forEach(budget -> {
            String status = budget.isOverBudget() ? "Over Budget" : "On Track";
            dataset.setValue(budget.getCategory() + " (" + status + ")",
                    budget.getSpentAmount());
        });
    }

    private void refreshRecentTransactions() {
        DefaultTableModel model = (DefaultTableModel) recentTransactionsTable.getModel();
        model.setRowCount(0);
        controller.getRecentTransactions(10).forEach(t -> {
            model.addRow(new Object[]{
                    dateFormat.format(t.getDate()),
                    t.getCategory(),
                    t.getType().equals("INCOME") ? "Income" : "Expense",
                    MoneyUtils.formatMoney(t.getAmount()),
                    t.getDescription()
            });
        });
    }

    private void refreshMetrics() {
        // 移除旧的指标面板
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && comp.getBounds().y < 100) { // 简单判断顶部面板
                remove(comp);
                break;
            }
        }

        // 添加新的指标面板
        add(createMetricsPanel(), BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Key Metrics"));

        BigDecimal balance = controller.getCurrentBalance();
        BigDecimal income = controller.getMonthlyIncome();
        BigDecimal expense = controller.getMonthlyExpense();
        long activeBudgets = controller.getBudgets().stream()
                .filter(b -> !b.isOverBudget())
                .count();

        panel.add(createMetricCard("Current Balance", balance,
                balance.compareTo(BigDecimal.ZERO) >= 0 ? Color.GREEN.darker() : Color.RED));
        panel.add(createMetricCard("This Month's Income", income, new Color(0, 128, 0)));
        panel.add(createMetricCard("This Month's Expense", expense, Color.RED));
        panel.add(createMetricCard("On - Track Budgets", activeBudgets + "/" + controller.getBudgets().size(),
                new Color(0, 0, 139)));

        return panel;
    }

    private JPanel createMetricCard(String title, BigDecimal value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(MoneyUtils.formatMoney(value), SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setBackground(new Color(240, 240, 240));

        return card;
    }

    private JPanel createMetricCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.setBackground(new Color(240, 240, 240));

        return card;
    }

    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recent Transactions (10)"));

        String[] columnNames = {"Date", "Category", "Type", "Amount", "Remarks"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recentTransactionsTable = new JTable(model);
        recentTransactionsTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(recentTransactionsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(this.getWidth(), 300));
        return panel;
    }

}
