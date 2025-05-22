// Package declaration
package com.personalfinance.view.component;

// Required imports
import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * A custom formatted text field for currency input.
 * <p>
 * This component supports decimal input formatted to two decimal places,
 * disallows invalid values, and provides convenient methods to get and set
 * the amount as {@link BigDecimal}.
 */
public class CurrencyTextField extends JFormattedTextField {
    // Number formatter to control display and parsing of currency values
    private final NumberFormat format;

    /**
     * Constructs a new CurrencyTextField with default formatting.
     * <p>
     * The field displays numbers with two decimal places, right-aligned,
     * with a default value of 0.00. Invalid input is disallowed.
     */
    public CurrencyTextField() {
        // Configure number format to 2 decimal places
        format = NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        // Create a number formatter with input restrictions
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setAllowsInvalid(false); // Disallow letters or invalid symbols
        formatter.setMinimum(0.0);         // Disallow negative values

        // Apply the formatter to this text field
        setFormatter(formatter);

        // UI properties
        setColumns(10);                              // Set preferred column width
        setHorizontalAlignment(SwingConstants.RIGHT); // Right-align the text
        setValue(BigDecimal.ZERO);                   // Set default value to 0.00
    }

    /**
     * Returns the current amount entered in the text field as a BigDecimal.
     *
     * @return the current amount, or BigDecimal.ZERO if parsing fails
     * @throws ParseException if the current input cannot be parsed
     */
    public BigDecimal getAmount() throws ParseException {
        Object value = getValue();
        return (value instanceof Number)
                ? BigDecimal.valueOf(((Number) value).doubleValue())
                : BigDecimal.ZERO;
    }

    /**
     * Sets the amount displayed in the text field.
     *
     * @param amount the BigDecimal value to display; if null, sets to 0.00
     */
    public void setAmount(BigDecimal amount) {
        setValue(amount != null ? amount : BigDecimal.ZERO);
    }

    /**
     * Overrides the default setValue to ensure BigDecimal is properly handled.
     *
     * @param value the value to set; can be BigDecimal or any other supported type
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
