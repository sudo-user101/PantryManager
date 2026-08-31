package com.example.pantrybasic;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.pantrybasic.util.AppPreferences;

/**
 * Applies the user's saved dark-mode preference (Settings > Appearance) before any Activity is
 * created, so the correct theme is active from the very first frame rather than flashing
 * light-then-dark (or vice versa).
 */
public class PantryBasicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        boolean darkMode = AppPreferences.isDarkModeEnabled(this);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
