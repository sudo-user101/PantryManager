package com.example.pantrybasic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.util.AppPreferences;

/**
 * Minimal Settings screen - only the rows that correspond to functionality Pantry Basic
 * actually has right now (dark mode + the recipe collection). Expiry alerts, unit system, and
 * a default icon preference all belong to later migrations and are not stubbed here.
 */
public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupDarkModeRow();
        setupExpiryAlertsRow();

        findViewById(R.id.rowResetRecipes).setOnClickListener(v -> {
            DatabaseHelper.getInstance(this).resetRecipes();
            Toast.makeText(this, R.string.toast_recipes_reset, Toast.LENGTH_SHORT).show();
        });

        bindVersion();

        setupFloatingNav(NAV_SETTINGS);
    }

    private void setupDarkModeRow() {
        SwitchCompat switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(AppPreferences.isDarkModeEnabled(this));
        switchDarkMode.setOnCheckedChangeListener((button, checked) -> {
            if (checked == AppPreferences.isDarkModeEnabled(this)) return;
            AppPreferences.setDarkModeEnabled(this, checked);
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    private void setupExpiryAlertsRow() {
        SwitchCompat switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        switchExpiryAlerts.setChecked(AppPreferences.isExpiryAlertsEnabled(this));
        switchExpiryAlerts.setOnCheckedChangeListener((button, checked) ->
                AppPreferences.setExpiryAlertsEnabled(this, checked));
    }

    private void bindVersion() {
        TextView textVersion = findViewById(R.id.textAppVersion);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            textVersion.setText(getString(R.string.settings_version_label, info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            textVersion.setText(getString(R.string.settings_version_label, "1.0"));
        }
    }
}
