package com.personalfinance.model;
// User.java：抽象用户实体，包含身份验证、分类配置等属性
import java.util.Arrays;
import java.util.List;






public class User {
    private String username;
    private String passwordHash;
    private String avatarPath = "avatars/default-avatar.png";
    private List<String> categories = Arrays.asList("Food", "Transportation", "Shopping", "Housing", "Entertainment", "Salary", "Health" , "Education" , "Gifts"    );
    private String currency = "CNY";

    public User(){}
    // 验证密码
    public boolean validatePassword(String inputPassword) {
        return this.passwordHash.equals(hashPassword(inputPassword));
    }

    // 密码加密
    public static String hashPassword(String password) {
        // 实际项目应该使用BCrypt等安全哈希
        return Integer.toHexString(password.hashCode());
    }

    // Getters and Setters
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
