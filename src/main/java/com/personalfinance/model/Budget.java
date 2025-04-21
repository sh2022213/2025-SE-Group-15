package com.personalfinance.model;

import java.math.BigDecimal;
import java.util.Date;

// 预算实体类，用于管理特定类别的消费预算
public class Budget {
    // 唯一标识符（UUID格式）
    private String id;
    // 预算关联的分类（如：Food, Transportation）
    private String category;
    // 预算总金额（使用BigDecimal保证计算精度）
    private BigDecimal amount;
    // 预算周期开始日期
    private Date startDate;
    // 预算周期结束日期
    private Date endDate;
    // 已消费金额（默认值0，随交易自动累计）
    private BigDecimal spentAmount = BigDecimal.ZERO;

    // 默认构造函数（需手动设置字段）
    public Budget() {}

    // 全参数构造函数（自动生成ID）
    // 参数校验建议：金额需正数，结束日期应在开始日期之后
    public Budget(String category, BigDecimal amount, Date startDate, Date endDate) {
        this.id = java.util.UUID.randomUUID().toString();
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 获取预算唯一ID
    public String getId() { return id; }
    
    // 设置预算ID（通常仅用于数据持久化操作）
    public void setId(String id) { this.id = id; }

    // 获取预算分类
    public String getCategory() { return category; }
    
    // 设置预算分类（应与交易分类一致）
    public void setCategory(String category) { this.category = category; }

    // 获取预算总额
    public BigDecimal getAmount() { return amount; }
    
    // 设置预算总额（需保证金额非负数）
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    // 获取周期开始日期
    public Date getStartDate() { return startDate; }
    
    // 设置开始日期（建议添加校验：不得晚于结束日期）
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    // 获取周期结束日期
    public Date getEndDate() { return endDate; }
    
    // 设置结束日期（建议添加校验：不得早于开始日期）
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    // 获取已消费金额
    public BigDecimal getSpentAmount() { return spentAmount; }
    
    // 更新已消费金额（通常由系统根据交易记录自动计算）
    public void setSpentAmount(BigDecimal spentAmount) { 
        this.spentAmount = spentAmount; 
    }

    // 计算剩余可用预算金额（总额 - 已消费）
    public BigDecimal getRemainingAmount() {
        return amount.subtract(spentAmount);
    }

    // 判断是否超支（已消费 > 预算总额时返回true）
    public boolean isOverBudget() {
        return spentAmount.compareTo(amount) > 0;
    }
}