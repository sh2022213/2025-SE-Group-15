package com.personalfinance.view.component;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A custom formatted text field for currency input.
 * 
 * This component ensures:
 * - Only valid numeric values are accepted
 * - Values are always formatted to two decimal places
 * - Values are aligned to the right
 * - Input is treated as non-negative monetary values
 */
public class CurrencyTextField extends JFormattedTextField {
    private final NumberFormat format;

    /**
     * Constructs a CurrencyTextField with default settings:
     * - Two decimal places
     * - Minimum value: 0
     * - Initial value: 0.00
     * - Right-aligned text
     */
    public CurrencyTextField() {
        // Create a number format with two decimal places
        format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        // Create a number formatter that disallows invalid input and sets minimum to 0
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0.0);

        // Set formatter and field properties
        setFormatter(formatter);
        setColumns(10); // Sets preferred number of character columns
        setHorizontalAlignment(SwingConstants.RIGHT); // Align text to the right
        setValue(0.00); // Initial value
    }

    /**
     * Returns the value entered as a BigDecimal.
     * 
     * @return the current amount as BigDecimal
     * @throws ParseException if the value cannot be parsed
     */
    public BigDecimal getAmount() throws ParseException {
        Object value = getValue();
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    /**
     * Sets the field's value to the given BigDecimal.
     * 
     * @param amount the amount to be set
     */
    public void setAmount(BigDecimal amount) {
        setValue(amount.doubleValue());
    }

    /**
     * Overrides setValue to support BigDecimal input.
     * 
     * @param value the value to set; can be BigDecimal or other supported types
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
