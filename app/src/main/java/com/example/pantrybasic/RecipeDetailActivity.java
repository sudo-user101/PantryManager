package com.example.pantrybasic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeIngredient;
import com.example.pantrybasic.model.RecipeMatchResult;
import com.example.pantrybasic.util.IngredientMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shows the full ingredient list and method for one recipe, reached via an Intent extra
 * ({@link #EXTRA_RECIPE_ID}) from either the Suggested or Almost There list. Re-runs
 * {@link IngredientMatcher} for this single recipe so each ingredient line can be marked as
 * already in the pantry or still missing.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        databaseHelper = DatabaseHelper.getInstance(this);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = recipeId != -1 ? databaseHelper.getRecipeWithIngredients(recipeId) : null;

        if (recipe == null) {
            Toast.makeText(this, R.string.title_recipe_detail, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getName());
        renderRecipe(recipe);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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

        TextView textSteps = findViewById(R.id.textSteps);
        textSteps.setText(recipe.getSteps());
    }

    private void bindReadyBadge(RecipeMatchResult matchResult) {
        TextView textBadge = findViewById(R.id.textReadyBadge);
        boolean ready = matchResult.isFullMatch();
        int colorRes = ready ? R.color.success : R.color.error;
        textBadge.setTextColor(ContextCompat.getColor(this, colorRes));

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
            TextView textLine = row.findViewById(R.id.textIngredientLine);
            textLine.setText(ingredient.toDisplayString());
            textLine.setTextColor(ContextCompat.getColor(this, missing ? R.color.error : R.color.success));

            container.addView(row);
            if (i < ingredients.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
                container.addView(divider);
            }
        }
    }
}
