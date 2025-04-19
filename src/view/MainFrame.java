package com.personalfinance.view;

import com.personalfinance.MainSystem;
import com.personalfinance.controller.FinanceController;
import com.personalfinance.view.panel.DashboardPanel;
import com.personalfinance.view.panel.TransactionPanel;
import com.personalfinance.view.panel.UserProfilePanel;

import javax.swing.*;

public class MainFrame extends JFrame {
    private final FinanceController controller;
    private DashboardPanel dashboardPanel;
    private TransactionPanel transactionPanel;

    public MainFrame(FinanceController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Personal Financial Management System");
        setSize(1400, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 设置应用图标
        //ImageIcon icon = new ImageIcon(getClass().getResource("/icons/app-icon.png"));
        //setIconImage(icon.getImage());

        // 创建主选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 初始化面板时保留引用
        dashboardPanel = new DashboardPanel(controller);
        transactionPanel = new TransactionPanel(controller);

        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Transaction Record", transactionPanel);

        // 添加选项卡切换监听
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refreshData(); // 切换到仪表盘时刷新
            }
        });

        // 添加用户菜单
        JMenuBar menuBar = new JMenuBar();

        // 用户菜单
        JMenu userMenu = new JMenu("User");
        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> showProfileDialog());
        userMenu.add(profileItem);

        JMenuItem logoutItem = new JMenuItem("Log out");
        logoutItem.addActionListener(e -> logout());
        userMenu.add(logoutItem);

        menuBar.add(userMenu);

        // 帮助菜单
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        add(tabbedPane);
    }

    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "Profile", true);
        profileDialog.add(new UserProfilePanel(controller));
        profileDialog.pack();
        profileDialog.setLocationRelativeTo(this);
        profileDialog.setVisible(true);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new MainSystem().startApplication();
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Personal Financial Management System v1.0\nAI - empowered personal finance tracker\n\n© 2025 Personal Finance Team",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWindow() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }

    // 提供刷新所有面板的方法
    public void refreshAll() {
        dashboardPanel.refreshData();
    }
}
