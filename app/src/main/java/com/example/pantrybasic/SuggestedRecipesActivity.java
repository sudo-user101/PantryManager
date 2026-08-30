package com.example.pantrybasic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.adapter.RecipeAdapter;
import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeMatchResult;
import com.example.pantrybasic.util.IngredientMatcher;

import java.util.List;

/**
 * Runs {@link IngredientMatcher} against the user's current pantry every time this screen is
 * shown, and renders two sections: recipes the user can make right now (a strict, complete
 * match), and an "Almost There" list of recipes missing exactly one ingredient. A recipe
 * missing two or more ingredients appears in neither list.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private DatabaseHelper databaseHelper;
    private RecipeAdapter suggestedAdapter;
    private RecipeAdapter almostThereAdapter;
    private RecyclerView recyclerSuggested;
    private View emptyStateSuggested;
    private View emptyStateAlmostThere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = DatabaseHelper.getInstance(this);

        recyclerSuggested = findViewById(R.id.recyclerSuggested);
        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));
        suggestedAdapter = new RecipeAdapter(this);
        recyclerSuggested.setAdapter(suggestedAdapter);

        RecyclerView recyclerAlmostThere = findViewById(R.id.recyclerAlmostThere);
        recyclerAlmostThere.setLayoutManager(new LinearLayoutManager(this));
        almostThereAdapter = new RecipeAdapter(this);
        recyclerAlmostThere.setAdapter(almostThereAdapter);

        emptyStateSuggested = findViewById(R.id.emptyStateSuggested);
        emptyStateAlmostThere = findViewById(R.id.emptyStateAlmostThere);

        setupFloatingNav(NAV_RECIPES);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recompute on every visit - the pantry may have changed since we were last shown.
        runMatchingEngine();
    }

    private void runMatchingEngine() {
        List<PantryItem> pantry = databaseHelper.getAllItems();
        List<Recipe> allRecipes = databaseHelper.getAllRecipesWithIngredients();

        List<List<RecipeMatchResult>> results = IngredientMatcher.matchAll(allRecipes, pantry);
        List<RecipeMatchResult> suggested = results.get(0);
        List<RecipeMatchResult> almostThere = results.get(1);

        suggestedAdapter.setItems(suggested);
        almostThereAdapter.setItems(almostThere);
        recyclerSuggested.scheduleLayoutAnimation();

        emptyStateSuggested.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
        emptyStateAlmostThere.setVisibility(almostThere.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onRecipeClick(RecipeMatchResult result) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, result.getRecipe().getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
