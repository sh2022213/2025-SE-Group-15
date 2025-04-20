package com.personalfinance.view.util;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class MoneyUtils {
    public static String formatMoney(BigDecimal amount) {
        return "¥" + amount.setScale(2, RoundingMode.HALF_UP);
    }
}
