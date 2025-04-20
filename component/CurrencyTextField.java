//包名
package com.personalfinance.view.component;
//引入
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

public class CurrencyTextField extends JFormattedTextField {
    private final NumberFormat format;

    public CurrencyTextField() {//文本域
        format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);

        setFormatter(formatter);
        setColumns(10);
        setHorizontalAlignment(SwingConstants.RIGHT);
        setValue(0.00);
    }

    public BigDecimal getAmount() throws ParseException {//get账户
        Object value = getValue();
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    public void setAmount(BigDecimal amount) {//设置账户
        setValue(amount.doubleValue());
    }

    @Override
    public void setValue(Object value) {//设置数值
        if (value instanceof BigDecimal) {
            super.setValue(((BigDecimal) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }
}
