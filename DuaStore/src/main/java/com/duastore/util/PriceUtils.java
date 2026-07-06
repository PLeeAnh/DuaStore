package com.duastore.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PriceUtils {

    private static final DecimalFormat VND;

    static {
        VND = new DecimalFormat("#,##0₫");
        VND.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(new Locale("vi", "VN")));
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "0₫";
        }
        return VND.format(amount);
    }
}
