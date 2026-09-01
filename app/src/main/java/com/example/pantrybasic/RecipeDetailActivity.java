package com.example.pantrybasic;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeIngredient;
import com.example.pantrybasic.model.RecipeMatchResult;
import com.example.pantrybasic.util.AppPreferences;
import com.example.pantrybasic.util.IngredientMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Shows the full ingredient list and method for one recipe, reached via an Intent extra
 * ({@link #EXTRA_RECIPE_ID}) from either the Suggested or Almost There list. Re-runs
 * {@link IngredientMatcher} for this single recipe so each ingredient line can be marked as
 * already in the pantry (✓) or still missing (✗), and the header pill restates whether the
 * recipe is fully ready to cook right now.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    /** Splits the stored method paragraph into individual sentences for the numbered-step
     * display, without needing a schema change - {@code recipes.steps} stays one TEXT column. */
    private static final Pattern SENTENCE_SPLIT = Pattern.compile("(?<=[.!?])\\s+");

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        databaseHelper = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> navigateBack());

        View buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setVisibility(AppPreferences.isTutorialModeEnabled(this) ? View.VISIBLE : View.GONE);
        buttonHelp.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.help_title_recipe_detail)
                        .setMessage(R.string.help_body_recipe_detail)
                        .setPositiveButton(R.string.action_got_it, null)
                        .show());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = recipeId != -1 ? databaseHelper.getRecipeWithIngredients(recipeId) : null;

        if (recipe == null) {
            Toast.makeText(this, R.string.title_recipe_detail, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        renderRecipe(recipe);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void navigateBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void renderRecipe(Recipe recipe) {
        List<PantryItem> pantry = databaseHelper.getAllItems();
        RecipeMatchResult matchResult = IngredientMatcher.match(recipe, pantry);

        Set<Long> missingIds = new HashSet<>();
        for (RecipeIngredient missing : matchResult.getMissingIngredients()) {
            missingIds.add(missing.getId());
        }

        TextView textName = findViewById(R.id.textRecipeName);
        textName.setText(recipe.getName());

        bindReadyBadge(matchResult);
        bindIngredients(recipe, missingIds);
        bindSteps(recipe.getSteps());
    }

    private void bindReadyBadge(RecipeMatchResult matchResult) {
        TextView textBadge = findViewById(R.id.textReadyBadge);
        boolean ready = matchResult.isFullMatch();

        textBadge.setBackgroundResource(ready ? R.drawable.bg_pill_success : R.drawable.bg_pill_warning);
        int colorRes = ready ? R.color.success : R.color.warning;
        textBadge.setTextColor(ContextCompat.getColor(this, colorRes));

        Drawable icon = ContextCompat.getDrawable(this, ready ? R.drawable.ic_check_24 : R.drawable.ic_close_24);
        if (icon != null) {
            icon = icon.mutate();
            icon.setTint(ContextCompat.getColor(this, colorRes));
            int px = Math.round(14 * getResources().getDisplayMetrics().density);
            icon.setBounds(0, 0, px, px);
        }
        textBadge.setCompoundDrawables(icon, null, null, null);

        if (ready) {
            textBadge.setText(R.string.recipe_ready_now);
        } else {
            int missingCount = matchResult.getMissingIngredients().size();
            textBadge.setText(missingCount == 1
                    ? getString(R.string.recipe_missing_one)
                    : getString(R.string.recipe_missing_count, missingCount));
        }
    }

    private void bindIngredients(Recipe recipe, Set<Long> missingIds) {
        LinearLayout container = findViewById(R.id.containerIngredients);
        container.removeAllViews();

        List<RecipeIngredient> ingredients = recipe.getIngredients();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < ingredients.size(); i++) {
            RecipeIngredient ingredient = ingredients.get(i);
            boolean missing = missingIds.contains(ingredient.getId());

            View row = inflater.inflate(R.layout.item_ingredient_row, container, false);
            ImageView imageStatus = row.findViewById(R.id.imageStatus);
            TextView textLine = row.findViewById(R.id.textIngredientLine);

            textLine.setText(ingredient.toDisplayString());
            int colorRes = missing ? R.color.error : R.color.success;
            imageStatus.setImageResource(missing ? R.drawable.ic_close_24 : R.drawable.ic_check_24);
            imageStatus.setColorFilter(ContextCompat.getColor(this, colorRes));

            container.addView(row);
            if (i < ingredients.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) divider.getLayoutParams();
                params.setMarginStart(Math.round(16 * getResources().getDisplayMetrics().density));
                divider.setLayoutParams(params);
                container.addView(divider);
            }
        }
    }

    private void bindSteps(String stepsParagraph) {
        LinearLayout container = findViewById(R.id.containerSteps);
        container.removeAllViews();

        List<String> steps = new ArrayList<>();
        if (stepsParagraph != null) {
            for (String sentence : SENTENCE_SPLIT.split(stepsParagraph.trim())) {
                String trimmed = sentence.trim();
                if (!trimmed.isEmpty()) {
                    steps.add(trimmed);
                }
            }
        }
        // Fall back to the whole paragraph as a single "step" if splitting produced nothing
        // (e.g. a steps string with no terminal punctuation) rather than showing a blank list.
        if (steps.isEmpty() && stepsParagraph != null && !stepsParagraph.trim().isEmpty()) {
            steps.add(stepsParagraph.trim());
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_step_row, container, false);
            TextView textNumber = row.findViewById(R.id.textStepNumber);
            TextView textBody = row.findViewById(R.id.textStepBody);
            textNumber.setText(String.format(Locale.getDefault(), "%02d", i + 1));
            textBody.setText(steps.get(i));
            container.addView(row);
        }
    }
}
