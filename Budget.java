package com.personalfinance.model;
// Budget.java：描述预算规则，关联分类和时间范围
import java.math.BigDecimal;
import java.util.Date;

public class Budget {
    private String id;
    private String category;
    private BigDecimal amount;
    private Date startDate;
    private Date endDate;
    private BigDecimal spentAmount = BigDecimal.ZERO;

    public Budget() {}

    public Budget(String category, BigDecimal amount, Date startDate, Date endDate) {
        this.id = java.util.UUID.randomUUID().toString();
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(spentAmount);
    }

    public boolean isOverBudget() {
        return spentAmount.compareTo(amount) > 0;
    }
    
}
