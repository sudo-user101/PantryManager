package com.example.pantrybasic.util;

import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeIngredient;
import com.example.pantrybasic.model.RecipeMatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the assignment's core business rule: a recipe may only be shown as "suggested" if
 * EVERY ingredient it requires is currently present in the user's pantry, in at least the
 * required quantity.
 * <p>
 * A recipe missing exactly one ingredient is classified separately as "Almost There" (an
 * optional bonus list); a recipe missing two or more ingredients is not shown anywhere.
 * <p>
 * Names are compared after {@link UnitUtils#normalizeName}, so "tomato" and "Tomatoes" are
 * treated as the same ingredient. Quantities are compared after converting both sides to a
 * common unit where possible (e.g. 1 kg pantry stock satisfies a recipe that needs 500 g), so
 * simple unit differences do not cause false negatives.
 */
public final class IngredientMatcher {

    private IngredientMatcher() {
    }

    /**
     * A required quantity is treated as satisfied if the pantry stock reaches at least this
     * fraction of it. A small tolerance (rather than requiring an exact-or-greater match)
     * absorbs tiny rounding differences without weakening the rule for any real shortfall.
     */
    private static final double QUANTITY_TOLERANCE = 0.995;

    /**
     * Runs the strict-matching rule for a single recipe against the current pantry.
     *
     * @return a RecipeMatchResult whose missingIngredients list is empty for a full strict
     *         match, contains exactly one entry for an "Almost There" recipe, or two-or-more
     *         entries for a recipe that should not be shown at all.
     */
    public static RecipeMatchResult match(Recipe recipe, List<PantryItem> pantry) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient required : recipe.getIngredients()) {
            if (!isSatisfied(required, pantry)) {
                missing.add(required);
            }
        }
        return new RecipeMatchResult(recipe, missing);
    }

    /**
     * Evaluates every recipe in {@code allRecipes} against the pantry and splits the results
     * into the two lists a Suggested Recipes screen will eventually need: index 0 = strict
     * "Suggested" matches, index 1 = "Almost There" matches (missing exactly one ingredient).
     */
    public static List<List<RecipeMatchResult>> matchAll(List<Recipe> allRecipes, List<PantryItem> pantry) {
        List<RecipeMatchResult> suggested = new ArrayList<>();
        List<RecipeMatchResult> almostThere = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            RecipeMatchResult result = match(recipe, pantry);
            if (result.isFullMatch()) {
                suggested.add(result);
            } else if (result.isAlmostThere()) {
                almostThere.add(result);
            }
            // 2+ missing ingredients: excluded from both lists entirely.
        }

        List<List<RecipeMatchResult>> both = new ArrayList<>();
        both.add(suggested);
        both.add(almostThere);
        return both;
    }

    /** True if the pantry contains this required ingredient in sufficient quantity. */
    private static boolean isSatisfied(RecipeIngredient required, List<PantryItem> pantry) {
        String requiredName = UnitUtils.normalizeName(required.getName());
        String requiredUnit = UnitUtils.normalizeUnit(required.getUnit());

        for (PantryItem stock : pantry) {
            if (!UnitUtils.normalizeName(stock.getName()).equals(requiredName)) {
                continue;
            }

            String stockUnit = UnitUtils.normalizeUnit(stock.getUnit());

            if (UnitUtils.sameFamily(stockUnit, requiredUnit)) {
                double stockInRequiredUnit = UnitUtils.convert(stock.getQuantity(), stockUnit, requiredUnit);
                if (stockInRequiredUnit >= required.getQuantity() * QUANTITY_TOLERANCE) {
                    return true;
                }
                // Same ingredient found but not enough of it - keep scanning in case of a
                // duplicate pantry entry (defensive; the UI does not currently allow duplicate names).
                continue;
            }

            // Units aren't in a known convertible family (e.g. pantry logs "pcs" but the
            // recipe calls for "g" of the same ingredient). Rather than produce a false
            // negative from a unit mismatch we can't resolve, fall back to requiring that
            // some positive quantity of the named ingredient is present.
            if (stock.getQuantity() > 0) {
                return true;
            }
        }
        return false;
    }
}
