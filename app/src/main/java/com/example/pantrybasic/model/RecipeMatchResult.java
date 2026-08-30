package com.example.pantrybasic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The outcome of running the strict-matching engine (see {@code IngredientMatcher}) for one
 * recipe against the current pantry.
 * <p>
 * A recipe with an empty {@link #missingIngredients} list is a full, strict match and belongs
 * in the main "Suggested Recipes" list. A recipe missing exactly one ingredient belongs in the
 * optional "Almost There" list. Anything missing two or more ingredients is not shown at all.
 */
public class RecipeMatchResult {

    private final Recipe recipe;
    private final List<RecipeIngredient> missingIngredients;

    public RecipeMatchResult(Recipe recipe, List<RecipeIngredient> missingIngredients) {
        this.recipe = recipe;
        this.missingIngredients = missingIngredients != null ? missingIngredients : new ArrayList<>();
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<RecipeIngredient> getMissingIngredients() {
        return missingIngredients;
    }

    public boolean isFullMatch() {
        return missingIngredients.isEmpty();
    }

    public boolean isAlmostThere() {
        return missingIngredients.size() == 1;
    }

    /** Comma-separated names of the missing ingredients, for display. */
    public String missingIngredientNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingIngredients.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(missingIngredients.get(i).getName());
        }
        return sb.toString();
    }
}
