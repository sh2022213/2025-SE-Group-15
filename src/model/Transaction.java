package com.personalfinance.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * 交易记录实体类
 */
public class Transaction {
    private String id;          // 唯一标识符
    private BigDecimal amount;  // 交易金额
    private String category;    // 交易分类
    private String type;        // 交易类型（INCOME/EXPENSE）
    private Date date;          // 交易日期
    private String description; // 交易描述
    private String account;     // 关联账户（可选）
    private boolean isRecurring;// 是否为周期性交易

    // 交易类型常量
    public static final String TYPE_INCOME = "INCOME";
    public static final String TYPE_EXPENSE = "EXPENSE";

    /**
     * 默认构造函数
     */
    public Transaction() {
        this.id = UUID.randomUUID().toString();
        this.date = new Date();
    }

    /**
     * 全参数构造函数
     */
    public Transaction(BigDecimal amount, String category, String type,
                       Date date, String description) {
        this();
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.date = date;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (!TYPE_INCOME.equals(type) && !TYPE_EXPENSE.equals(type)) {
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    // 业务方法
    /**
     * 获取带符号的金额（支出为负数）
     */
    public BigDecimal getSignedAmount() {
        return TYPE_EXPENSE.equals(type) ? amount.negate() : amount;
    }

    /**
     * 检查是否是收入
     */
    public boolean isIncome() {
        return TYPE_INCOME.equals(type);
    }

    /**
     * 检查是否是支出
     */
    public boolean isExpense() {
        return TYPE_EXPENSE.equals(type);
    }
    

    // 重写方法
    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }

    // Builder模式（可选）
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private BigDecimal amount;
        private String category;
        private String type;
        private Date date;
        private String description;
        private String account;
        private boolean isRecurring;

        private Builder() {}

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder account(String account) {
            this.account = account;
            return this;
        }

        public Builder isRecurring(boolean isRecurring) {
            this.isRecurring = isRecurring;
            return this;
        }

        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.setId(id);
            transaction.setAmount(amount);
            transaction.setCategory(category);
            transaction.setType(type);
            transaction.setDate(date);
            transaction.setDescription(description);
            transaction.setAccount(account);
            transaction.setRecurring(isRecurring);
            return transaction;
        }
    }
    
}


