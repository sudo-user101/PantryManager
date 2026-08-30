package com.example.pantrybasic;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * The most important test in this suite: proves the strict recipe-matching rule end-to-end
 * against the real on-device database and the real {@code SuggestedRecipesActivity} screen -
 * not the {@code IngredientMatcherTest} JUnit unit test, which only exercises the matcher
 * class in isolation with hand-built objects and never touches SQLite, the seeded recipe
 * collection, or the actual screen the marker will see.
 * <p>
 * Each test drives the on-device pantry into a specific, known state via the real
 * {@link DatabaseHelper} (the same class the app itself uses to persist data), launches the
 * real {@link SuggestedRecipesActivity}, and asserts a named recipe landed in the correct
 * bucket - "Ready to make" ({@code recyclerSuggested}) or "Almost there"
 * ({@code recyclerAlmostThere}) - or in neither, exactly as the strict rule requires.
 * <p>
 * The user's pantry is snapshotted in {@link #setUp()} and restored in {@link #tearDown()} so
 * running this suite never leaves the demo data altered.
 */
@RunWith(AndroidJUnit4.class)
public class StrictMatchingInstrumentedTest {

    /** "Grilled Cheese Sandwich" needs bread 2 slice, butter 10 g, cheese 60 g - see
     * RecipeSeeder. Chosen because it does not share any ingredient with the other recipe
     * used below, so the two tests cannot influence each other's pantry state. */
    private static final String GRILLED_CHEESE = "Grilled Cheese Sandwich";

    private DatabaseHelper dbHelper;
    private List<PantryItem> originalPantry;

    @Before
    public void setUp() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(appContext);
        originalPantry = dbHelper.getAllItems();
        for (PantryItem item : originalPantry) {
            dbHelper.deleteItem(item.getId());
        }
    }

    @After
    public void tearDown() {
        for (PantryItem item : dbHelper.getAllItems()) {
            dbHelper.deleteItem(item.getId());
        }
        for (PantryItem item : originalPantry) {
            // Re-insert with the original id preserved isn't necessary for the app to behave
            // correctly (ids are surrogate keys); restoring name/quantity/unit is what matters
            // for the demo pantry to look exactly as it did before this test ran.
            dbHelper.insertItem(new PantryItem(item.getName(), item.getQuantity(), item.getUnit()));
        }
    }

    @Test
    public void fullMatch_recipeAppearsInSuggested_notAlmostThere() {
        insert("bread", 2, "slice");
        insert("butter", 10, "g");
        insert("cheese", 60, "g");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerSuggested))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
            onView(withId(R.id.recyclerAlmostThere))
                    .check(doesNotHaveDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void missingOneIngredient_recipeMovesToAlmostThere_notSuggested() {
        // Same as the full-match case but cheese is left out entirely.
        insert("bread", 2, "slice");
        insert("butter", 10, "g");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerAlmostThere))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
            onView(withId(R.id.recyclerSuggested))
                    .check(doesNotHaveDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void insufficientQuantity_treatedSameAsMissing() {
        // Cheese present but below the 60 g the recipe requires - must NOT count as satisfied.
        insert("bread", 2, "slice");
        insert("butter", 10, "g");
        insert("cheese", 30, "g");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerAlmostThere))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
            onView(withId(R.id.recyclerSuggested))
                    .check(doesNotHaveDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void twoMissingIngredients_excludedFromBothLists() {
        // Only bread present; butter AND cheese both missing.
        insert("bread", 2, "slice");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerSuggested))
                    .check(doesNotHaveDescendant(withText(GRILLED_CHEESE)));
            onView(withId(R.id.recyclerAlmostThere))
                    .check(doesNotHaveDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void extraUnrelatedIngredient_doesNotAffectMatch() {
        insert("bread", 2, "slice");
        insert("butter", 10, "g");
        insert("cheese", 60, "g");
        insert("EspressoUnrelatedExtraItem", 1, "pcs");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerSuggested))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void pluralPantryName_stillMatchesSingularRecipeIngredient() {
        // Recipe requires "cheese" (singular); pantry holds "Cheeses" (plural, capitalised).
        insert("bread", 2, "slice");
        insert("butter", 10, "g");
        insert("Cheeses", 60, "g");

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerSuggested))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
        }
    }

    @Test
    public void unitFamilyConversion_gramsRequirementSatisfiedFromKilograms() {
        // Recipe needs cheese in grams; pantry holds the equivalent amount in kilograms.
        insert("bread", 2, "slice");
        insert("butter", 10, "g");
        insert("cheese", 0.5, "kg"); // 500 g, well above the 60 g required.

        try (ActivityScenario<SuggestedRecipesActivity> scenario =
                     ActivityScenario.launch(SuggestedRecipesActivity.class)) {
            onView(withId(R.id.recyclerSuggested))
                    .check(matchesDescendant(withText(GRILLED_CHEESE)));
        }
    }

    private void insert(String name, double quantity, String unit) {
        dbHelper.insertItem(new PantryItem(name, quantity, unit));
    }

    /** Small helper so the intent of each assertion above reads as "this RecyclerView has a
     * row matching X" rather than a raw Espresso matcher expression. */
    private static androidx.test.espresso.ViewAssertion matchesDescendant(
            org.hamcrest.Matcher<android.view.View> descendantMatcher) {
        return androidx.test.espresso.assertion.ViewAssertions.matches(
                hasDescendant(descendantMatcher));
    }

    /** The correct way to assert "no row matches X" - negating the whole hasDescendant check,
     * not the inner matcher. {@code hasDescendant(not(X))} is true as soon as any descendant
     * merely isn't X, and is trivially false when the RecyclerView has zero items (nothing to
     * find any match in), so it is not a safe way to express "list does not contain X". */
    private static androidx.test.espresso.ViewAssertion doesNotHaveDescendant(
            org.hamcrest.Matcher<android.view.View> descendantMatcher) {
        return androidx.test.espresso.assertion.ViewAssertions.matches(
                not(hasDescendant(descendantMatcher)));
    }
}
