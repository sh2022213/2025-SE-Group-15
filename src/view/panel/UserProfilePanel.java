package com.personalfinance.view.panel;

import com.personalfinance.controller.FinanceController;
import com.personalfinance.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
        avatarLabel = new JLabel(loadAvatar(System.getProperty("user.dir") + File.separator + user.getAvatarPath()));
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

    private ImageIcon loadAvatar(String path) {
        ImageIcon icon = new ImageIcon(path);
        // Scale the avatar to a suitable size
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    private void changeAvatar(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.isDirectory() || name.endsWith(".jpg") || name.endsWith(".png");
            }

            @Override
            public String getDescription() {
                return "Image files (*.jpg, *.png)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String basePath = System.getProperty("user.dir");
            String avatarPath = "/avatars/" + selectedFile.getName();
            // Copy the file to the application directory
            try {
                File destDir = new File(basePath + File.separator + "avatars");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                File destFile = new File(basePath + File.separator + avatarPath);
                try (FileInputStream fis = new FileInputStream(selectedFile);
                     FileOutputStream fos = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            controller.getUser().setAvatarPath(avatarPath);
            avatarLabel.setIcon(loadAvatar(basePath + File.separator + avatarPath));
            controller.saveUser();
        }
    }

    private void saveChanges(String newPassword, String confirmPassword) {
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "The two entered passwords do not match", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(this,
                        "The password length cannot be less than 6 characters", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            controller.getUser().setPassword(newPassword);
        }

        controller.saveUser();
        JOptionPane.showMessageDialog(this,
                "Changes have been saved", "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
