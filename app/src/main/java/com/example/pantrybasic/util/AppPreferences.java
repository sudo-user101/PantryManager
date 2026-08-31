package com.example.pantrybasic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/**
 * Thin wrapper around the app's small SharedPreferences file, so every screen reads/writes the
 * same keys through one place instead of repeating raw strings. Dark Mode and expiry alerts
 * live here so far - unit system and a default icon preference belong to features this project
 * doesn't have yet and are deliberately not stubbed.
 */
public final class AppPreferences {

    private static final String PREFS_NAME = "pantry_basic_prefs";
    public static final String KEY_DARK_MODE = "dark_mode_enabled";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";

    public static final boolean DEFAULT_EXPIRY_ALERTS = true;

    private AppPreferences() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Whether the app should render in dark mode. Undefined on first run, in which case the
     * device's current system setting is used as the default so simply installing the app
     * never itself changes its appearance - from that point on it's an explicit, persisted
     * user choice.
     */
    public static boolean isDarkModeEnabled(Context context) {
        SharedPreferences p = prefs(context);
        if (!p.contains(KEY_DARK_MODE)) {
            int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return uiMode == Configuration.UI_MODE_NIGHT_YES;
        }
        return p.getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkModeEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, DEFAULT_EXPIRY_ALERTS);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }
}
