package me.travja.crave.common.util;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Formatter {


    private static DateFormat   dateFormat     = new SimpleDateFormat("yyyy-MM-dd");
    private static NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public static String formatCurrency(double num) {
        return currencyFormat.format(num);
    }

    public static Date parseDate(String dateStr) throws ParseException {
        return dateFormat.parse(dateStr);
    }

}
