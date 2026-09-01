package com.example.pantrybasic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.util.AppPreferences;

/**
 * Minimal Settings screen - only the rows that correspond to functionality Pantry Basic
 * actually has right now (dark mode, expiry alerts, Tutorial Mode, and preferred unit system +
 * the recipe collection). A default icon preference belongs to a later migration and is not
 * stubbed here.
 */
public class SettingsActivity extends BaseActivity {

    private TextView textUnitSystemValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupDarkModeRow();
        setupExpiryAlertsRow();
        setupUnitSystemRow();
        setupTutorialModeRow();

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

    private void setupUnitSystemRow() {
        textUnitSystemValue = findViewById(R.id.textUnitSystemValue);
        refreshUnitSystemLabel();

        findViewById(R.id.rowUnitSystem).setOnClickListener(v -> {
            String[] entries = getResources().getStringArray(R.array.unit_system_entries);
            String[] values = getResources().getStringArray(R.array.unit_system_values);
            String current = AppPreferences.getUnitSystem(this);
            int checkedIndex = 0;
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(current)) {
                    checkedIndex = i;
                    break;
                }
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.pref_units_title)
                    .setSingleChoiceItems(entries, checkedIndex, (dialog, which) -> {
                        AppPreferences.setUnitSystem(this, values[which]);
                        refreshUnitSystemLabel();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });
    }

    private void refreshUnitSystemLabel() {
        String[] entries = getResources().getStringArray(R.array.unit_system_entries);
        String[] values = getResources().getStringArray(R.array.unit_system_values);
        String current = AppPreferences.getUnitSystem(this);
        String label = entries[0];
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                label = entries[i];
                break;
            }
        }
        // Show just the short form (before the parenthetical) in the row's trailing value.
        int parenIndex = label.indexOf(" (");
        textUnitSystemValue.setText(parenIndex > 0 ? label.substring(0, parenIndex) : label);
    }

    private void setupTutorialModeRow() {
        SwitchCompat switchTutorialMode = findViewById(R.id.switchTutorialMode);
        switchTutorialMode.setChecked(AppPreferences.isTutorialModeEnabled(this));
        switchTutorialMode.setOnCheckedChangeListener((button, checked) -> {
            if (checked == AppPreferences.isTutorialModeEnabled(this)) return;
            AppPreferences.setTutorialModeEnabled(this, checked);
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
            if (checked) {
                databaseHelper.insertDemoItems();
            } else {
                databaseHelper.clearDemoItems();
            }
        });
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
