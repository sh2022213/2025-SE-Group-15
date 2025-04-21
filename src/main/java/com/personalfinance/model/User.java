// 用户实体类，用于表示个人财务管理应用中的用户信息
package com.personalfinance.model;

import java.util.Arrays;
import java.util.List;

public class User {
    // 用户基本信息
    private String username;           // 用户登录名
    private String passwordHash;       // 使用哈希算法处理后的密码
    private String avatarPath = "avatars/default-avatar.png";  // 用户头像存储路径（默认头像）
    
    // 用户财务分类配置（9个默认分类）
    private List<String> categories = Arrays.asList(
        "Food", "Transportation", "Shopping", "Housing", 
        "Entertainment", "Salary", "Health", "Education", "Gifts"
    );
    
    // 用户首选货币单位（默认人民币）
    private String currency = "CNY";

    // 密码验证方法：比较输入密码的哈希值与存储的哈希值
    public boolean validatePassword(String inputPassword) {
        return this.passwordHash.equals(hashPassword(inputPassword));
    }

    // 密码哈希生成方法（注意：仅用于演示，实际应使用安全算法）
    public static String hashPassword(String password) {
        // 将密码的哈希码转换为十六进制字符串
        // 安全警告：此实现不安全，正式项目应使用BCrypt等加密算法
        return Integer.toHexString(password.hashCode());
    }

    // 以下是各属性的访问器和修改器方法
    
    // 用户名相关方法
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    // 密码哈希相关方法
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    // 设置明文密码（自动转换为哈希值存储）
    public void setPassword(String plainPassword) {
        this.passwordHash = hashPassword(plainPassword);
    }

    // 头像路径相关方法
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { 
        this.avatarPath = avatarPath; 
    }

    // 分类管理相关方法
    public List<String> getCategories() { return categories; }
    // 允许用户自定义财务分类列表
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    // 货币单位相关方法
    public String getCurrency() { return currency; }
    // 允许用户设置首选货币（如USD, EUR等）
    public void setCurrency(String currency) { 
        this.currency = currency; 
    }
}
