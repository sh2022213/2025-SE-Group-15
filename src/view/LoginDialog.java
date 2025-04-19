package com.personalfinance.view;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {
    private final FinanceController controller;
    private boolean authenticated = false;
    private String registeredUsername; // 用于存储刚注册的用户名

    public LoginDialog(JFrame parent, FinanceController controller) {
        super(parent, "User Login", true);
        this.controller = controller;
        initUI();
        setSize(400, 300); // 调整窗口大小
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 创建卡片布局切换登录/注册面板
        JPanel cardPanel = new JPanel(new CardLayout());

        // 登录面板
        JPanel loginPanel = createLoginPanel(cardPanel);

        // 注册面板
        JPanel registerPanel = createRegisterPanel(cardPanel);

        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");

        mainPanel.add(cardPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createLoginPanel(JPanel cardPanel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("User Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        // 用户名输入
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        JTextField usernameField = new JTextField(15);

        if (registeredUsername != null) {
            usernameField.setText(registeredUsername);
        }
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(usernameField, gbc);

        // 密码输入
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(passwordField, gbc);

        usernameField.addActionListener((ActionEvent e) -> {
            passwordField.requestFocus();
        });

        // 登录按钮
        JButton loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.addActionListener((ActionEvent e) -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (authenticate(username, password)) {
                authenticated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Incorrect username or password", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // 注册按钮
        JButton registerButton = new JButton("Register Account");
        registerButton.addActionListener(e -> {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, "register");
        });


        passwordField.addActionListener((ActionEvent e) -> {
            loginButton.doClick();
        });

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(loginButton);
        if (controller.getUser() == null || controller.getUser().getUsername() == null) {
            buttonPanel.add(registerButton);
        }
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        // 窗口关闭处理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                authenticated = false;
            }
        });

        return panel;
    }

    private JPanel createRegisterPanel(JPanel cardPanel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Register New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        // 用户名输入
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Username:"), gbc);
        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(usernameField, gbc);

        // 密码输入
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(passwordField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Confirm Password:"), gbc);
        JPasswordField confirmField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(confirmField, gbc);

        // 注册按钮
        JButton registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(100, 30));
        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "用户名和密码不能为空", "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                        "两次输入的密码不一致", "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this,
                        "密码长度不能少于6位", "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (controller.userExists(username)) {
                JOptionPane.showMessageDialog(this,
                        "用户名已存在", "注册失败",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 注册新用户
            controller.registerUser(username, password);
            registeredUsername = username;

            JOptionPane.showMessageDialog(this,
                    "注册成功，请登录", "注册成功",
                    JOptionPane.INFORMATION_MESSAGE);

            // 切换回登录面板
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, "login");
        });

        // 返回登录按钮
        JButton backButton = new JButton("Back to Login");
        backButton.addActionListener(e -> {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, "login");
        });

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.add(backButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private boolean authenticate(String username, String password) {
        User user = controller.getUser();
        if (user == null || !username.equals(user.getUsername())) {
            return false;
        }
        return user.validatePassword(password);
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
