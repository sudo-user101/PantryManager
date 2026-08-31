package com.example.pantrybasic.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Small helper around the ISO-8601 "yyyy-MM-dd" date strings stored for pantry item expiry
 * dates - used to drive the optional "expiring soon" highlighting on the Pantry List screen
 * (toggled from Settings).
 */
public final class DateUtils {

    public static final String PATTERN = "yyyy-MM-dd";
    public static final String DISPLAY_PATTERN = "dd MMM yyyy";

    /** Items expiring within this many days are flagged as "expiring soon". */
    public static final int EXPIRING_SOON_WINDOW_DAYS = 3;

    private DateUtils() {
    }

    private static SimpleDateFormat isoFormat() {
        return new SimpleDateFormat(PATTERN, Locale.getDefault());
    }

    private static SimpleDateFormat displayFormat() {
        return new SimpleDateFormat(DISPLAY_PATTERN, Locale.getDefault());
    }

    public static String formatForStorage(Calendar calendar) {
        return isoFormat().format(calendar.getTime());
    }

    /** Returns a friendly "dd MMM yyyy" string for display, or the raw value if it can't be parsed. */
    public static String formatForDisplay(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return "";
        try {
            Date date = isoFormat().parse(isoDate);
            return date != null ? displayFormat().format(date) : isoDate;
        } catch (ParseException e) {
            return isoDate;
        }
    }

    /** Whole days from today until the given ISO date (negative if it is already in the past). */
    public static long daysUntil(String isoDate) {
        if (isoDate == null || isoDate.trim().isEmpty()) return Long.MAX_VALUE;
        try {
            Date target = isoFormat().parse(isoDate);
            if (target == null) return Long.MAX_VALUE;

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffMs = target.getTime() - today.getTimeInMillis();
            return TimeUnit.MILLISECONDS.toDays(diffMs);
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    public static boolean isExpired(String isoDate) {
        long days = daysUntil(isoDate);
        return days != Long.MAX_VALUE && days < 0;
    }

    public static boolean isExpiringSoon(String isoDate) {
        long days = daysUntil(isoDate);
        return days != Long.MAX_VALUE && days >= 0 && days <= EXPIRING_SOON_WINDOW_DAYS;
    }
}
