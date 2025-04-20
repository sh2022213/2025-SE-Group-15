package com.personalfinance.model;

import java.util.Arrays;
import java.util.List;

public class User {
    private String username;
    private String passwordHash;
    private String avatarPath = "avatars/default-avatar.png";
    private List<String> categories = Arrays.asList("Food", "Transportation", "Shopping", "Housing", "Entertainment", "Salary", "Health", "Education", "Gifts");
    private String currency = "CNY";

    // Validates the password
    public boolean validatePassword(String inputPassword) {
        return this.passwordHash.equals(hashPassword(inputPassword));
    }

    // Hashes the password
    // In a real project, a secure hash like BCrypt should be used
    public static String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPassword(String plainPassword) {
        this.passwordHash = hashPassword(plainPassword);
    }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
