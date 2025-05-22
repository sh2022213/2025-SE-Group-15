package com.personalfinance;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.view.LoginDialog;
import com.personalfinance.view.MainFrame;

import javax.swing.*;

public class MainSystem {
    public static void main(String[] args) {
        // 高DPI支持
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> {
            // 初始化控制器
            FinanceController controller = new FinanceController();

            // 显示登录对话框
            LoginDialog loginDialog = new LoginDialog(null, controller);
            loginDialog.setVisible(true);
            boolean authenticated = loginDialog.isAuthenticated();

            if (authenticated) {
                MainFrame frame = new MainFrame(controller);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 确保主窗口关闭时退出
                frame.showWindow();
            } else {
                System.exit(0); // 用户取消登录时退出程序
            }
        });
    }


    public void startApplication() {
        // 重新启动
        com.personalfinance.MainSystem.main(new String[]{});
    }

}
