package me.travja.crave.common.util;

import java.text.NumberFormat;
import java.util.Locale;

public class Formatter {

    private static NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);

    public static String formatCurrency(double num) {
        return format.format(num);
    }

}
