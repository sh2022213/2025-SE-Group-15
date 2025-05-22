package com.personalfinance.view.panel;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UserProfilePanel extends JPanel {
    private final FinanceController controller;
    private JLabel avatarLabel;

    public UserProfilePanel(FinanceController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        User user = controller.getUser();

        // Avatar display area
        JPanel avatarPanel = new JPanel(new BorderLayout());
        avatarLabel = new JLabel(loadAvatar(user));
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton changeAvatarBtn = new JButton("Change Avatar");
        changeAvatarBtn.addActionListener(this::changeAvatar);

        avatarPanel.add(avatarLabel, BorderLayout.CENTER);
        avatarPanel.add(changeAvatarBtn, BorderLayout.SOUTH);
        avatarPanel.setBorder(BorderFactory.createTitledBorder("Avatar"));

        // User information form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        JTextField usernameField = new JTextField(user.getUsername(), 15);
        usernameField.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        // Password modification
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("New Password:"), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        // Confirm password
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        JPasswordField confirmField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(confirmField, gbc);

        // Save button
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> saveChanges(
                new String(passwordField.getPassword()),
                new String(confirmField.getPassword())
        ));

        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(saveBtn, gbc);

        // Layout combination
        add(avatarPanel, BorderLayout.WEST);
        add(formPanel, BorderLayout.CENTER);
    }

    private ImageIcon loadAvatar(User user) {
        String avatarPath = getAvatarPath(user);
        if (avatarPath != null && new File(avatarPath).exists()) {
            ImageIcon icon = new ImageIcon(avatarPath);
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return new ImageIcon(System.getProperty("user.dir") + File.separator + "/avatars/default-avatar.png"); // 默认头像
    }

    private String getAvatarPath(User user) {
        if (user.getAvatarPath() == null || user.getAvatarPath().isEmpty()) {
            return null;
        }
        // 头像路径格式: "data/{username}/avatar.{ext}"
        return System.getProperty("user.dir") + File.separator + user.getAvatarPath();
    }

    private void changeAvatar(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.jpeg, *.png)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            User currentUser = controller.getUser();
            // 创建用户专属头像目录
            String userAvatarDir = "data" + File.separator + currentUser.getUsername() + File.separator + "avatars";
            Path avatarDirPath = Paths.get(userAvatarDir);
            try {
                Files.createDirectories(avatarDirPath);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to create avatar directory", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            /// 获取文件扩展名
            String fileName = selectedFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String newAvatarName = "avatar" + extension;

            // 新头像路径 (相对路径)
            String newAvatarRelativePath = "data" + File.separator +
                    currentUser.getUsername() + File.separator +
                    "avatars" + File.separator + newAvatarName;

            // 完整路径
            String newAvatarFullPath = System.getProperty("user.dir") +
                    File.separator + newAvatarRelativePath;

            try {
                // 先删除旧头像文件（如果存在）
                Path targetPath = Paths.get(newAvatarFullPath);
                if (Files.exists(targetPath)) {
                    Files.delete(targetPath);
                }

                // 复制新头像文件
                Files.copy(selectedFile.toPath(), Paths.get(newAvatarFullPath));

                // 删除旧头像 (如果有且不是同一个文件)
               /* if (currentUser.getAvatarPath() != null && !currentUser.getAvatarPath().isEmpty()) {
                    String oldAvatarPath = getAvatarPath(currentUser);
                    if (!oldAvatarPath.equals(newAvatarFullPath)) {
                        Files.deleteIfExists(Paths.get(oldAvatarPath));
                    }
                }*/

                // 更新用户头像路径
                currentUser.setAvatarPath(newAvatarRelativePath);

                // 保存用户信息到用户列表
                if (controller.updateUser(currentUser)) {
                    avatarLabel.setIcon(loadAvatar(currentUser));
                    JOptionPane.showMessageDialog(this,
                            "Avatar updated successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to save user profile",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Failed to save avatar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveChanges(String newPassword, String confirmPassword) {
        User currentUser = controller.getUser();

        if (!newPassword.isEmpty()) {
            // 验证密码
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "The two entered passwords do not match",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 6 characters",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 修改密码
            if (controller.changePassword(currentUser.getUsername(), newPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Password changed successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to change password",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // 只保存头像修改
            if (controller.updateUser(currentUser)) {
                JOptionPane.showMessageDialog(this,
                        "Profile updated successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
