// 包声明
package com.personalfinance.view.component;

// 导入必要的类
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 自定义货币输入文本框，提供格式化的货币金额输入功能。
 * 支持 BigDecimal 类型金额的获取和设置，并确保输入值的有效性。
 */
public class CurrencyTextField extends JFormattedTextField {
    // 数字格式化器，用于统一显示和解析货币金额
    private final NumberFormat format;

    /**
     * 构造函数，初始化货币文本框的格式和默认设置
     */
    public CurrencyTextField() {
        // 初始化数字格式（保留2位小数）
        format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        // 配置格式化器（不允许无效输入，最小值为0）
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);

        // 应用格式化器到文本框
        setFormatter(formatter);
        
        // 设置UI属性
        setColumns(10);  // 默认显示10个字符宽度
        setHorizontalAlignment(SwingConstants.RIGHT);  // 文本右对齐
        setValue(BigDecimal.ZERO);  // 默认值为0
    }

    /**
     * 获取当前输入的金额（BigDecimal类型）
     * @return 当前金额，解析失败时返回 BigDecimal.ZERO
     * @throws ParseException 当文本内容无法解析为数字时抛出
     */
    public BigDecimal getAmount() throws ParseException {
        Object value = getValue();
        return (value instanceof Number) 
                ? BigDecimal.valueOf(((Number) value).doubleValue()) 
                : BigDecimal.ZERO;
    }

    /**
     * 设置显示的金额
     * @param amount 要设置的金额（BigDecimal类型）
     */
    public void setAmount(BigDecimal amount) {
        setValue(amount != null ? amount : BigDecimal.ZERO);
    }

    /**
     * 重写设置值方法，确保BigDecimal类型的正确处理
     * @param value 要设置的值
     */
    @Override
    public void setValue(Object value) {
        if (value instanceof BigDecimal) {
            super.setValue(((BigDecimal) value).doubleValue());
        } else {
            super.setValue(value); 
        }
    }
}