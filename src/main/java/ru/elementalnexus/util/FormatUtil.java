package ru.elementalnexus.util;

import java.util.Locale;

/**
 * Форматирование чисел для сообщений: целые без дробной части
 * (15 вместо 15.0), дробные — с одним знаком после запятой.
 */
public final class FormatUtil {

    private FormatUtil() {
    }

    public static String num(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
