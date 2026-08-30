package com.example.pantrybasic;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

/**
 * Common base for the three top-level, floating-nav-hosted screens (Pantry List, Suggested
 * Recipes, Settings). Each of those Activities is a separate destination reached by an
 * explicit {@link Intent} - kept simple and easy to explain for the module's Intent-navigation
 * requirement, rather than a single-Activity/Fragment host.
 * <p>
 * Screens that are pushed on top of a tab (Add/Edit Ingredient, Recipe Detail) are not part of
 * the floating navigation and use ordinary back-stack navigation instead.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final int NAV_PANTRY = 0;
    public static final int NAV_RECIPES = 1;
    public static final int NAV_SETTINGS = 2;

    /**
     * Wires up the custom floating "Liquid Glass" navigation bar included via
     * {@code R.layout.floating_bottom_nav} in each top-level screen's layout.
     *
     * @param current which destination this screen represents (NAV_PANTRY/RECIPES/SETTINGS)
     */
    protected void setupFloatingNav(int current) {
        bindItem(R.id.navItemPantry, R.id.navIconPantry, R.id.navLabelPantry, R.id.navDotPantry,
                current == NAV_PANTRY, () -> {
                    Intent intent = new Intent(this, PantryListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                });

        bindItem(R.id.navItemRecipes, R.id.navIconRecipes, R.id.navLabelRecipes, R.id.navDotRecipes,
                current == NAV_RECIPES, () -> {
                    Intent intent = new Intent(this, SuggestedRecipesActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                });

        bindItem(R.id.navItemSettings, R.id.navIconSettings, R.id.navLabelSettings, R.id.navDotSettings,
                current == NAV_SETTINGS, () -> {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                });
    }

    private interface Navigate {
        void go();
    }

    private void bindItem(int containerId, int iconId, int labelId, int dotId,
                           boolean selected, Navigate navigate) {
        View container = findViewById(containerId);
        ImageView icon = findViewById(iconId);
        TextView label = findViewById(labelId);
        View dot = findViewById(dotId);
        if (container == null) return; // defensive - layout always includes all three

        applySelectedState(container, icon, label, dot, selected);

        container.setOnClickListener(v -> {
            if (selected) return; // already here
            // A quick, subtle "physically connected" pulse on tap before the screen changes -
            // the destination Activity redraws its own nav already in the selected state, so
            // this local pulse is what actually sells the transition as one continuous motion.
            icon.animate().scaleX(1.15f).scaleY(1.15f).setDuration(90)
                    .withEndAction(() -> icon.animate().scaleX(1f).scaleY(1f).setDuration(140).start())
                    .start();
            container.postDelayed(navigate::go, 70);
        });
    }

    private void applySelectedState(View container, ImageView icon, TextView label, View dot, boolean selected) {
        int colorRes = selected ? R.color.accent : R.color.text_tertiary;
        int color = ContextCompat.getColor(this, colorRes);
        icon.setColorFilter(color);
        label.setTextColor(color);
        dot.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        ViewCompat.setBackgroundTintList(dot, ColorStateList.valueOf(color));
        if (selected) {
            // Only override the background for the selected (non-clickable) item - the other
            // two keep their XML-default ?attr/selectableItemBackground ripple, since they're
            // the ones a tap actually needs to give feedback on.
            container.setBackgroundResource(R.drawable.bg_nav_selected_capsule);
        }
    }
}
