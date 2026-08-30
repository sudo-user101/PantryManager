package com.example.pantrybasic;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pantrybasic.db.DatabaseHelper;

/**
 * Minimal Settings screen - only the rows that correspond to functionality Pantry Basic
 * actually has right now (the recipe collection). Dark mode, expiry alerts, unit system, and
 * default icon all belong to later migrations and are not stubbed here.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.rowResetRecipes).setOnClickListener(v -> {
            DatabaseHelper.getInstance(this).resetRecipes();
            Toast.makeText(this, R.string.toast_recipes_reset, Toast.LENGTH_SHORT).show();
        });

        bindVersion();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
