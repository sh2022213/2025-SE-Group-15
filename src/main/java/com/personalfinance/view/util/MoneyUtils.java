package com.personalfinance.view.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author <a href="mailto:zkq1026@gmail.com">keqin</a>
 * @description
 * @date 2025年04月06日 21:19
 */
public class MoneyUtils {
    public static String formatMoney(BigDecimal amount) {
        return "¥" + amount.setScale(2, RoundingMode.HALF_UP);
    }
}
