package com.projecki.fusion.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since March 15, 2022
 * @author Andavin
 */
public final class NumberUtil {

    public static final char DECIMAL = '.';
    public static final long THOUSAND = 1000, MILLION = 1000000, BILLION = 1000000000;
    private static final List<DecimalFormat> FORMATS = List.of(
            new DecimalFormat("#,###"),
            new DecimalFormat("#,###.#"),
            new DecimalFormat("#,###.##"),
            new DecimalFormat("#,###.###"),
            new DecimalFormat("#,###.####"),
            new DecimalFormat("#,###.#####")
    );

    /**
     * Determine whether the specified character represents
     * a numeric digit.
     *
     * @param c The character to determine.
     * @return If the character is a digit.
     */
    public static boolean isNumeric(char c) {
        return '0' <= c && c <= '9';
    }

    /**
     * Get the place with the correct suffix (e.g. 1 = 1st,
     * 2 = 2nd, 25 = 25th, etc.).
     *
     * @param place The place to add the suffix to.
     * @return The place with the proper suffix.
     */
    public static String placeOf(int place) {
        return place + suffixOf(place);
    }

    private static String suffixOf(int place) {
        // If it is negative make it positive
        if (place < 0) {
            place *= -1;
        }
        // We have to check less than 20 because every
        // multiple of 10 has a 1st, 2nd and 3rd except
        // for 11th, 12th and 13th
        if (place > 20) {
            int remain = place > 100 ? place % 100 : place;
            return remain > 20 ? suffixOf(place % 10) : suffixOf(remain);
        }
        // Get the suffix for the place
        return switch (place) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    /**
     * Get the roman numeral equivalent for a number. The number
     * can be anything below a reasonable expectancy of about
     * 5,000 since the maximum single numeral character is only
     * {@code X} or 1,000.
     * <p>
     * Negative numbers will be prefixed with a minus sign and
     * calculated the same as a positive number.
     * <br>
     * {@code 0} will not be given a character and instead is
     * equal to nothing. If the number {@code 0} is passed in
     * then an empty string will be returned ({@code ""});
     *
     * @param number The number to get the numeral for.
     * @return The numeral string for the number.
     */
    public static String numeralOf(int number) {

        if (number == 0) {
            return "";
        }

        if (number > 10000) {
            number = 10000;
        }
        // Ensure that number is greater than zero
        StringBuilder sb = new StringBuilder();
        if (number < 0) {
            sb.append('-');
            number *= -1;
        }

        for (Numeral numeral : Numeral.values()) {

            while (number >= numeral.value) {
                number -= numeral.value;
                sb.append(numeral.name());
            }
        }

        return sb.toString();
    }

    /**
     * A utility method to minimize the amount given to a smaller
     * number and adding a suffix for thousands, millions etc.
     *
     * @param amount The amount to minimize.
     * @return The string with the amount and its suffix.
     */
    public static String minimize(long amount) {

        if (amount < THOUSAND) {
            return String.valueOf(amount);
        }

        if (amount < MILLION) {
            return String.valueOf(amount / THOUSAND) + 'k';
        }

        if (amount < BILLION) {
            return String.valueOf(amount / MILLION) + 'm';
        }

        return String.valueOf(amount / BILLION) + 'b';
    }

    /**
     * A utility method to minimize the amount given to a smaller
     * number and adding a suffix for thousands, millions etc.
     * <p>
     * This method allows for including decimals in the minimization.
     * The given amount of decimal places will be shown. For example,
     * {@code 1.23m} for {@code 1,230,000} minimized with {@code 2}
     * decimal places.
     *
     * @param amount The amount to minimize.
     * @param decimalPlaces The amount of decimal places to show.
     *         before decimals are included.
     * @return The string with the amount and its suffix.
     */
    public static String minimize(long amount, int decimalPlaces) {
        return NumberUtil.minimize(amount, decimalPlaces, 0);
    }

    /**
     * A utility method to minimize the amount given to a smaller
     * number and adding a suffix for thousands, millions etc.
     * <p>
     * This method allows for including decimals in the minimization.
     * The given amount of decimal places will be shown. For example,
     * {@code 1.23m} for {@code 1,230,000} minimized with {@code 2}
     * decimal places.
     *
     * @param amount The amount to minimize.
     * @param decimalPlaces The amount of decimal places to show.
     * @param minimum The amount required do the minimization.
     * @return The string with the amount and its suffix.
     */
    public static String minimize(long amount, int decimalPlaces, long minimum) {

        long abs = Math.abs(amount);
        if (abs < THOUSAND) {
            return String.valueOf(amount);
        }

        minimum = Math.abs(minimum);
        if (minimum != 0 && abs < minimum) {
            return NumberFormat.getNumberInstance().format(amount);
        }

        if (decimalPlaces < 1) {
            return NumberUtil.minimize(amount);
        }

        char suffix;
        double minimized;
        if (abs < MILLION) {
            suffix = 'k';
            minimized = (double) amount / THOUSAND;
        } else if (abs < BILLION) {
            suffix = 'm';
            minimized = (double) amount / MILLION;
        } else {
            suffix = 'b';
            minimized = (double) amount / BILLION;
        }

        if (decimalPlaces < FORMATS.size()) {
            return FORMATS.get(decimalPlaces).format(minimized) + suffix;
        } else {
            return String.format("%,." + decimalPlaces + "f" + suffix, minimized);
        }
    }

    /**
     * Parse the given string to a long allowing for
     * the following suffixes:
     * <ul>
     *     <li>{@code k} or {@code K} for thousand</li>
     *     <li>{@code m} or {@code M} for million</li>
     *     <li>{@code b} or {@code B} for billion</li>
     * </ul>
     * If any of the suffixes are present, then the
     * string will be parsed as a double rather than a
     * long, multiplied by the applicable number and
     * cast to a long.
     *
     * @param s The string to parse the long from.
     * @return The parsed long.
     * @see Long#parseLong(String)
     * @see Double#parseDouble(String)
     * @see #minimize(long) The Opposite Function
     */
    public static long parseLongWithSuffix(String s) {

        int length = s.length();
        checkArgument(length > 0, "invalid length: %s", length);
        long multiplier;
        switch (s.charAt(length - 1)) {
            case 'k':
            case 'K':
                multiplier = THOUSAND;
                break;
            case 'm':
            case 'M':
                multiplier = MILLION;
                break;
            case 'b':
            case 'B':
                multiplier = BILLION;
                break;
            default:
                return Long.parseLong(s);
        }
        // No need to check for overflow since double cannot overflow
        // instead it will be clamped to Long.MAX_VALUE or Long.MIN_VALUE
        return (long) (Double.parseDouble(s.substring(0, length - 1)) * multiplier);
    }

    private enum Numeral {

        M(1000), CM(900), D(500), CD(400), C(100), XC(90),
        L(50), XL(40), X(10), IX(9), V(5), IV(4), I(1);

        private final int value;

        Numeral(int value) {
            this.value = value;
        }
    }
}
