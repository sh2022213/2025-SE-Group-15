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

    // 构造函数
    public MainFrame(FinanceController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        // 窗口属性设置
        setTitle("Personal Financial Management System");
        setSize(1400, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        //添加了两个选项卡
        dashboardPanel = new DashboardPanel(controller); 
        transactionPanel = new TransactionPanel(controller);
        tabbedPane.addTab("Dashboard", dashboardPanel);
        tabbedPane.addTab("Transaction Record", transactionPanel);

        // 添加选项卡切换监听.
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedComponent() == dashboardPanel) {
                dashboardPanel.refreshData(); // 切换到仪表盘时刷新
            }
        });

        // 添加用户菜单
        JMenuBar menuBar = new JMenuBar();
            // 创建用户菜单
            JMenu userMenu = new JMenu("User");
            // 添加“Profile”菜单项
            JMenuItem profileItem = new JMenuItem("Profile");
            profileItem.addActionListener(e -> showProfileDialog()); // 事件监听
            userMenu.add(profileItem);
            // 添加“Log out”菜单项
            JMenuItem logoutItem = new JMenuItem("Log out");
            logoutItem.addActionListener(e -> logout()); // 事件监听
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

    // Profile对话框
    private void showProfileDialog() {
        JDialog profileDialog = new JDialog(this, "Profile", true); 
        profileDialog.add(new UserProfilePanel(controller)); // 添加
        profileDialog.pack(); // 自动调整对话框大小
        profileDialog.setLocationRelativeTo(this); // 居中显示
        profileDialog.setVisible(true); // 显示对话框
    }

    // logout对话框
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog( // 显示界面
                this,
                "Are you sure you want to log out?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose(); // 关闭界面
            new MainSystem().startApplication(); //重启应用
        }
    }

    // About对话框
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
