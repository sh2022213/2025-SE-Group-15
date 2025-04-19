package com.personalfinance.view.component;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

public class CurrencyTextField extends JFormattedTextField {
    private final NumberFormat format;

    public CurrencyTextField() {
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

    public BigDecimal getAmount() throws ParseException {
        Object value = getValue();
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    public void setAmount(BigDecimal amount) {
        setValue(amount.doubleValue());
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof BigDecimal) {
            super.setValue(((BigDecimal) value).doubleValue());
        } else {
            super.setValue(value);
        }
    }
}
