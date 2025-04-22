package com.personalfinance;


import com.personalfinance.controller.FinanceController;
import com.personalfinance.view.LoginDialog;
import com.personalfinance.view.MainFrame;


import javax.swing.*; 


public class MainSystem {
    public static void main(String[] args) {
        // 控制了用户界面（UI）的缩放比例，"1.0"：不缩放，默认大小。
        System.setProperty("sun.java2d.uiScale", "1.0");

        SwingUtilities.invokeLater(() -> { //确保 GUI 更新操作在事件调度线程上执行，从而避免线程安全问题。
            // 初始化控制器
            FinanceController controller = new FinanceController();

            // 创建了一个 LoginDialog 对象（登录对话框），并将其设置为可见。
            LoginDialog loginDialog = new LoginDialog(null, controller);
            loginDialog.setVisible(true);

            // 检查用户是否成功登录
            boolean authenticated = loginDialog.isAuthenticated();

            // 根据登录结果启动主窗口或退出程序
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
        // 重新启动应用
        MainSystem.main(new String[]{});
    }

}
