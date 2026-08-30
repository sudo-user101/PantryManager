package com.example.pantrybasic.util;

import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeIngredient;
import com.example.pantrybasic.model.RecipeMatchResult;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the strict-matching engine. Run on the JVM with {@code ./gradlew test} - no
 * emulator needed.
 */
public class IngredientMatcherTest {

    private Recipe recipeNeeding(String... nameQtyUnitTriples) {
        Recipe recipe = new Recipe(1L, "Test Recipe", "Mix everything.");
        List<RecipeIngredient> ingredients = new ArrayList<>();
        for (int i = 0; i < nameQtyUnitTriples.length; i += 3) {
            String name = nameQtyUnitTriples[i];
            double qty = Double.parseDouble(nameQtyUnitTriples[i + 1]);
            String unit = nameQtyUnitTriples[i + 2];
            ingredients.add(new RecipeIngredient(name, qty, unit));
        }
        recipe.setIngredients(ingredients);
        return recipe;
    }

    private PantryItem stock(String name, double qty, String unit) {
        return new PantryItem(name, qty, unit);
    }

    @Test
    public void fullMatch_whenEveryIngredientPresentInSufficientQuantity() {
        Recipe recipe = recipeNeeding(
                "tomato", "2", "pcs",
                "onion", "1", "pcs",
                "salt", "1", "pinch");

        List<PantryItem> pantry = Arrays.asList(
                stock("tomato", 3, "pcs"),
                stock("onion", 1, "pcs"),
                stock("salt", 2, "pinch"));

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertTrue("Recipe should be a full strict match", result.isFullMatch());
        assertEquals(0, result.getMissingIngredients().size());
    }

    @Test
    public void excluded_whenOneOfFiveIngredientsMissing() {
        Recipe recipe = recipeNeeding(
                "chicken breast", "500", "g",
                "onion", "1", "pcs",
                "garlic", "2", "clove",
                "tomato", "2", "pcs",
                "rice", "200", "g");

        List<PantryItem> pantry = Arrays.asList(
                stock("chicken breast", 500, "g"),
                stock("onion", 1, "pcs"),
                stock("garlic", 2, "clove"),
                stock("tomato", 2, "pcs"));
        // rice is missing entirely

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertFalse("Recipe missing 1 of 5 ingredients must not be a full match", result.isFullMatch());
        assertEquals(1, result.getMissingIngredients().size());
        assertTrue(result.isAlmostThere());
    }

    @Test
    public void notEvenAlmostThere_whenTwoIngredientsMissing() {
        Recipe recipe = recipeNeeding(
                "egg", "2", "pcs",
                "flour", "200", "g",
                "milk", "250", "ml");

        List<PantryItem> pantry = Arrays.asList(stock("egg", 2, "pcs"));
        // flour and milk both missing

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertFalse(result.isFullMatch());
        assertFalse(result.isAlmostThere());
        assertEquals(2, result.getMissingIngredients().size());
    }

    @Test
    public void insufficientQuantity_isTreatedAsMissing() {
        Recipe recipe = recipeNeeding("flour", "500", "g");
        List<PantryItem> pantry = Arrays.asList(stock("flour", 100, "g"));

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertFalse("100g in stock should not satisfy a 500g requirement", result.isFullMatch());
        assertEquals(1, result.getMissingIngredients().size());
    }

    @Test
    public void nameMatching_isRobustToPluralAndCase() {
        Recipe recipe = recipeNeeding("tomato", "2", "pcs");
        List<PantryItem> pantry = Arrays.asList(stock("Tomatoes", 5, "pcs"));

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertTrue("'Tomatoes' in the pantry should satisfy a recipe that needs 'tomato'", result.isFullMatch());
    }

    @Test
    public void quantityMatching_convertsAcrossCompatibleUnits() {
        Recipe recipe = recipeNeeding("flour", "500", "g");
        List<PantryItem> pantry = Arrays.asList(stock("flour", 1, "kg")); // 1kg = 1000g >= 500g

        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertTrue("1kg of flour should satisfy a 500g requirement", result.isFullMatch());
    }

    @Test
    public void emptyPantry_matchesNoRecipes() {
        Recipe recipe = recipeNeeding("egg", "2", "pcs");
        RecipeMatchResult result = IngredientMatcher.match(recipe, new ArrayList<PantryItem>());
        assertFalse(result.isFullMatch());
        assertEquals(1, result.getMissingIngredients().size());
    }

    @Test
    public void countBasedIngredient_matchesExactly() {
        Recipe recipe = recipeNeeding("egg", "2", "pcs");
        List<PantryItem> pantry = Arrays.asList(stock("egg", 2, "pcs"));
        RecipeMatchResult result = IngredientMatcher.match(recipe, pantry);
        assertTrue("2 pcs egg should satisfy a 2 pcs egg requirement", result.isFullMatch());
    }
}
