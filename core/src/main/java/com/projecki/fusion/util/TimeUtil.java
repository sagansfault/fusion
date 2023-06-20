package com.projecki.fusion.util;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @since April 09, 2020
 * @author Andavin
 */
public final class TimeUtil {

    /**
     * The default {@link ZoneId} to be used.
     */
    public static final ZoneId DEFAULT_TIME_ZONE = ZoneId.of("America/Denver");
    private static final EnumSet<Unit> DEFAULT_TIME_UNITS = EnumSet.range(Unit.YEAR, Unit.SECOND);
    private static final long[] PARSEABLE_UNITS = DEFAULT_TIME_UNITS.stream().mapToLong(Unit::getMillis).toArray();
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?:(\\d+)\\s*ye?a?r?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*mon?t?h?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*we?e?k?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*da?y?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*ho?u?r?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*(?:mill?i?s?e?c?o?n?d?s?|ms)[,\\s]*)?" +
            "(?:(\\d+)\\s*mi?n?u?t?e?s?[,\\s]*)?" +
            "(?:(\\d+)\\s*(?:se?c?o?n?d?s?)?)?",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Parse a string of time. The string should be in the relative format of
     * {@code [0-9] identifier} where {@code identifier} is the unit of time.
     * There can be multiple formats in the string and an undefined amount or
     * type of spacing.
     * <p>
     * <b>Warning</b>: It is always better for the string to be formatted with the
     * start being the highest unit of time and working it's way down to the
     * lowest (years, then months, then weeks etc.). If it is not in this order
     * the search algorithm may mix up units and some units may end up being incorrect.
     *
     * @param timeFormat The string of time to parse for {@link Unit Units}.
     * @return The amount of time in milliseconds that was parsed from the string.
     * @throws NumberFormatException If the string contained elements other than the numbers and identifiers.
     */
    public static long parse(String timeFormat) throws NumberFormatException {

        long total = 0;
        boolean found = false;
        Matcher matcher = TIME_PATTERN.matcher(timeFormat);
        while (matcher.find()) {

            String main = matcher.group();
            if (main == null || main.isEmpty()) {
                continue;
            }

            int length = PARSEABLE_UNITS.length;
            for (int i = 0; i <= length; i++) {

                String group = matcher.group(i + 1);
                if (group == null || group.isEmpty()) {
                    continue;
                }

                found = true;
                if (i < 5) {
                    total += Integer.parseInt(group) * PARSEABLE_UNITS[i];
                } else if (i == 5) {
                    total += Long.parseLong(group);
                } else {
                    total += Integer.parseInt(group) * PARSEABLE_UNITS[i - 1];
                }
            }
        }

        if (!found && !timeFormat.isEmpty()) {
            throw new NumberFormatException(timeFormat);
        }

        return total;
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for all {@link Unit Units} except for {@link Unit#MILLISECOND}.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to) {
        return formatDifference(System.currentTimeMillis(), to, true, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for all {@link Unit Units} except for {@link Unit#MILLISECOND}.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, boolean abbreviate) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for each of the {@link Unit Units} specified.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link Unit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, Unit unit, Unit... units) {
        return formatDifference(System.currentTimeMillis(), to, true, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for each of the {@link Unit Units} specified.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link Unit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, boolean abbreviate, Unit unit, Unit... units) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between {@link System#currentTimeMillis()}
     * and the specified time for each of the {@link Unit Units} specified.
     * <p>
     * For example, if the current time is 0 (midnight January 1st, 1970) formatting
     * 65,000 milliseconds would be returned as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link Unit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     * <p>
     * Note that, as implied by "difference", if the specified time is in the past
     * (i.e. before {@link System#currentTimeMillis()}), then the formatted string
     * will still be positive.
     *
     * @param to The time (in milliseconds) to format the difference from the current time.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param units All the {@link Unit Units} to format for (cannot be empty).
     * @return The formatted version of the difference between the specified
     *         time and the current time.
     */
    public static String formatFromNow(long to, boolean abbreviate, EnumSet<Unit> units) {
        return formatDifference(System.currentTimeMillis(), to, abbreviate, units);
    }

    /**
     * Format the difference in time between the two times specified
     * for all {@link Unit Units} except for {@link Unit#MILLISECOND}.
     * This will default to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to) {
        return formatDifference(from, to, true, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the difference in time between the two times specified
     * for all {@link Unit Units} except for {@link Unit#MILLISECOND}.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, boolean abbreviate) {
        return formatDifference(from, to, abbreviate, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link Unit Units} specified. This will default
     * to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, Unit unit, Unit... units) {
        return formatDifference(from, to, true, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link Unit Units} specified.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the difference between the specified times.
     */
    public static String formatDifference(long from, long to, boolean abbreviate, Unit unit, Unit... units) {
        return formatDifference(from, to, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the difference in time between the two times specified
     * for each of the {@link Unit Units} specified.
     * <p>
     * For example, formatting from 1,000 to 65,000 milliseconds would be
     * returned as {@code 1m 4s} for 1 minute 4 seconds as the difference
     * between those two times is 64,000 milliseconds (64 seconds).
     * <p>
     * Note that, as implied by "difference", if the specified {@code from}
     * time is before {@code to}, then the formatted string will still be positive.
     *
     * @param from The time (in milliseconds) to format the difference from.
     * @param to The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param units All the {@link Unit Units} to format for (cannot be empty).
     * @return The formatted version of the difference between the specified times.
     * @throws IllegalArgumentException If there are no {@link Unit Units} contained
     *                                  within the {@link EnumSet}.
     */
    public static String formatDifference(long from, long to, boolean abbreviate, EnumSet<Unit> units) throws IllegalArgumentException {
        return format(to - from, abbreviate, units);
    }

    /**
     * Format the given amount of time for all {@link Unit Units}
     * except for {@link Unit#MILLISECOND}. This will default
     * to abbreviating the suffixes for each unit of time.
     * <p>
     * For example, formatting 65,000 milliseconds would be returned
     * as {@code 1m 5s} for 1 minute 5 seconds.
     *
     * @param time The time to format (in milliseconds).
     * @return The formatted version of the time given.
     */
    public static String format(long time) {
        return format(time, true, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the given amount of time for all {@link Unit Units}
     * except for {@link Unit#MILLISECOND}.
     * <p>
     * For example, formatting 65,000 milliseconds would be returned
     * as {@code 1m 5s} when {@code abbreviate} if true or {@code 1 minute
     * 5 seconds} when it is false.
     *
     * @param time The time to format (in milliseconds).
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @return The formatted version of the time given.
     */
    public static String format(long time, boolean abbreviate) {
        return format(time, abbreviate, DEFAULT_TIME_UNITS);
    }

    /**
     * Format the given amount of time for each of the {@link Unit Units}
     * specified. This will default to abbreviating the suffixes for
     * each unit of time.
     * <p>
     * For example, formatting 65,000 milliseconds with {@link Unit#MINUTE}
     * and {@link Unit#SECOND} would return as {@code 1m 5s} for 1 minute 5 seconds.
     * <p>
     * If no units can fit within the time specified (e.g. only {@link Unit#MINUTE}
     * when given 10,000 milliseconds), then the lowest unit will be chosen and {@code 0}
     * of that unit will be returned as the format (e.g. 0 minutes).
     *
     * @param time The time to format (in milliseconds).
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the time given.
     */
    public static String format(long time, Unit unit, Unit... units) {
        return format(time, true, EnumSet.of(unit, units));
    }

    /**
     * Format the given amount of time for each of the {@link Unit Units} specified.
     * <p>
     * For example, formatting 65,000 milliseconds with {@link Unit#MINUTE}
     * and {@link Unit#SECOND} would return as {@code 1m 5s} for 1 minute 5 seconds.
     *
     * @param time The time to format (in milliseconds).
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param unit The first {@link Unit} to include.
     * @param units Any extra {@link Unit Units} to include.
     * @return The formatted version of the time given.
     */
    public static String format(long time, boolean abbreviate, Unit unit, Unit... units) {
        return format(time, abbreviate, EnumSet.of(unit, units));
    }

    /**
     * Format the given amount of time for each of the {@link Unit Units} specified.
     * <p>
     * For example, formatting 65,000 milliseconds with {@link Unit#MINUTE}
     * and {@link Unit#SECOND} would return as {@code 1m 5s} for 1 minute 5 seconds.
     *
     * @param time The time (in milliseconds) to format the difference to.
     * @param abbreviate If the suffixes for each time unit should be abbreviated.
     * @param units All the {@link Unit Units} to format for (cannot be empty).
     * @return The formatted version of the difference between the specified times.
     * @throws IllegalArgumentException If there are no {@link Unit Units} contained
     *                                  within the {@link EnumSet}.
     */
    public static String format(long time, boolean abbreviate, EnumSet<Unit> units) throws IllegalArgumentException {

        checkArgument(!units.isEmpty(), "requires at least one TimeUnit");
        // Ensure time is always greater than zero
        if (time < 0) {
            time *= -1;
        }

        Unit lowest = null;
        StringBuilder sb = new StringBuilder(units.size() * (abbreviate ? 10 : 15)); // Estimate size
        for (Unit unit : units) {

            lowest = unit; // Will always be the last iteration
            long ms = unit.getMillis();
            long found = time / ms;
            if (found > 0) {
                time -= found * ms;
                sb.append(found).append(unit.getSuffix(abbreviate, found == 1)).append(' ');
            }
        }

        if (sb.isEmpty()) { // Nothing was formatted
            return sb.append('0').append(lowest.getSuffix(abbreviate, false)).toString();
        }
        // Cut off the extra space at the end
        return sb.substring(0, sb.length() - 1);
    }

    public enum Unit {

        // Greatest to least for iteration order
        YEAR(1000L * 60 * 60 * 24 * 365, " year", "y"),
        MONTH(1000L * 60 * 60 * 24 * 30, " month", "mo"),
        WEEK(1000L * 60 * 60 * 24 * 7, " week", "w"),
        DAY(1000L * 60 * 60 * 24, " day", "d"),
        HOUR(1000L * 60 * 60, " hour", "h"),
        MINUTE(1000L * 60, " minute", "m"),
        SECOND(1000L, " second", "s"),
        MILLISECOND(1, " millisecond", "ms");

        private final long milliseconds;
        private final String singular, plural, abbreviated;

        Unit(long milliseconds, String singular, String abbreviated) {
            this.milliseconds = milliseconds;
            this.singular = singular;
            this.plural = singular + 's'; // All just add an s
            this.abbreviated = abbreviated;
        }

        /**
         * Get how many milliseconds that this unit of time
         * contains (e.g. 86,400,000 milliseconds in {@link #DAY}).
         *
         * @return The amount of milliseconds in this unit of time.
         */
        public long getMillis() {
            return milliseconds;
        }

        /**
         * Get how many milliseconds that this unit of time
         * contains (e.g. 86,400,000 milliseconds in {@link #DAY}).
         *
         * @return The amount of milliseconds in this unit of time.
         */
        public long getMilliseconds() {
            return milliseconds;
        }

        /**
         * Get the suffix for this unit of time based on the
         * given parameters.
         *
         * @param abbreviate If the suffix should be abbreviated such as
         *                   {@code s} instead of {@code seconds}.
         * @param singular If the suffix should be the singular
         *                 version such as {@code day} instead of {@code days}.
         * @return The suffix for this unit of time.
         */
        public String getSuffix(boolean abbreviate, boolean singular) {
            return abbreviate ? this.abbreviated : singular ? this.singular : this.plural;
        }
    }
}
