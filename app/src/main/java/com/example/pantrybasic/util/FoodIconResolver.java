package com.example.pantrybasic.util;

import com.example.pantrybasic.R;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolves a sensible default avatar emoji and a subtle category background tint from a plain
 * ingredient name, using simple keyword matching - no ML, no external data, entirely offline
 * and instant.
 * <p>
 * Used in two places: (1) pre-filling the "Food Icon" field when a user starts adding a new
 * ingredient, so they see a reasonable icon before ever opening the picker; and (2) as the
 * fallback for pantry rows that have no stored {@code icon_emoji} yet (see
 * {@link com.example.pantrybasic.db.DatabaseHelper}).
 */
public final class FoodIconResolver {

    private FoodIconResolver() {
    }

    public static final String DEFAULT_EMOJI = "🥫"; // 🥫 canned food - generic pantry item

    /** Ordered ingredient-name keyword -> emoji. Checked top-to-bottom on the first substring
     * match, so more specific keywords are listed before generic ones. */
    private static final Map<String, String> INGREDIENT_EMOJI = new LinkedHashMap<>();
    static {
        INGREDIENT_EMOJI.put("olive oil", "🫒");
        INGREDIENT_EMOJI.put("soy sauce", "🍶");
        INGREDIENT_EMOJI.put("chicken stock", "🍗");
        INGREDIENT_EMOJI.put("baking powder", "🥄");
        INGREDIENT_EMOJI.put("bell pepper", "🫑");
        INGREDIENT_EMOJI.put("tomato", "🍅");
        INGREDIENT_EMOJI.put("apple", "🍎");
        INGREDIENT_EMOJI.put("banana", "🍌");
        INGREDIENT_EMOJI.put("carrot", "🥕");
        INGREDIENT_EMOJI.put("corn", "🌽");
        INGREDIENT_EMOJI.put("honey", "🍯");
        INGREDIENT_EMOJI.put("pepper", "🫑");
        INGREDIENT_EMOJI.put("spinach", "🥬");
        INGREDIENT_EMOJI.put("onion", "🧅");
        INGREDIENT_EMOJI.put("cucumber", "🥒");
        INGREDIENT_EMOJI.put("bean", "🫘");
        INGREDIENT_EMOJI.put("basil", "🌿");
        INGREDIENT_EMOJI.put("cinnamon", "🌿");
        INGREDIENT_EMOJI.put("garlic", "🧄");
        INGREDIENT_EMOJI.put("ginger", "🌿");
        INGREDIENT_EMOJI.put("milk", "🥛");
        INGREDIENT_EMOJI.put("cream", "🥛");
        INGREDIENT_EMOJI.put("yogurt", "🥣");
        INGREDIENT_EMOJI.put("oat", "🥣");
        INGREDIENT_EMOJI.put("bread", "🍞");
        INGREDIENT_EMOJI.put("flour", "🌾");
        INGREDIENT_EMOJI.put("pasta", "🍝");
        INGREDIENT_EMOJI.put("rice", "🍚");
        INGREDIENT_EMOJI.put("potato", "🥔");
        INGREDIENT_EMOJI.put("chicken", "🍗");
        INGREDIENT_EMOJI.put("bacon", "🥓");
        INGREDIENT_EMOJI.put("egg", "🥚");
        INGREDIENT_EMOJI.put("cheese", "🧀");
        INGREDIENT_EMOJI.put("butter", "🧈");
        INGREDIENT_EMOJI.put("tuna", "🐟");
        INGREDIENT_EMOJI.put("mushroom", "🍄");
        INGREDIENT_EMOJI.put("salt", "🧂");
    }

    /** Same keyword set, grouped into a small set of pastel category tints for the avatar
     * background (see values/colors.xml avatar_tint_* tokens). */
    private static final Map<String, Integer> INGREDIENT_TINT = new LinkedHashMap<>();
    static {
        INGREDIENT_TINT.put("tomato", R.color.avatar_tint_red);
        INGREDIENT_TINT.put("apple", R.color.avatar_tint_red);
        INGREDIENT_TINT.put("carrot", R.color.avatar_tint_orange);
        INGREDIENT_TINT.put("corn", R.color.avatar_tint_orange);
        INGREDIENT_TINT.put("banana", R.color.avatar_tint_orange);
        INGREDIENT_TINT.put("honey", R.color.avatar_tint_orange);
        INGREDIENT_TINT.put("pepper", R.color.avatar_tint_orange);
        INGREDIENT_TINT.put("spinach", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("onion", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("cucumber", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("bean", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("basil", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("garlic", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("olive oil", R.color.avatar_tint_green);
        INGREDIENT_TINT.put("milk", R.color.avatar_tint_blue);
        INGREDIENT_TINT.put("cream", R.color.avatar_tint_blue);
        INGREDIENT_TINT.put("yogurt", R.color.avatar_tint_blue);
        INGREDIENT_TINT.put("bread", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("flour", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("oat", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("pasta", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("rice", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("potato", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("baking powder", R.color.avatar_tint_beige);
        INGREDIENT_TINT.put("chicken", R.color.avatar_tint_pink);
        INGREDIENT_TINT.put("bacon", R.color.avatar_tint_pink);
        INGREDIENT_TINT.put("egg", R.color.avatar_tint_pink);
        INGREDIENT_TINT.put("cheese", R.color.avatar_tint_pink);
        INGREDIENT_TINT.put("butter", R.color.avatar_tint_pink);
        INGREDIENT_TINT.put("tuna", R.color.avatar_tint_pink);
    }

    /** Fixed dish emoji for each of the 19 seeded recipes, by exact name - unlike the
     * ingredient avatar, this is not user-customizable (no picker), so a simple lookup is
     * enough. */
    private static final Map<String, String> RECIPE_EMOJI = new LinkedHashMap<>();
    static {
        RECIPE_EMOJI.put("Scrambled Eggs on Toast", "🥚");
        RECIPE_EMOJI.put("Tomato & Onion Omelette", "🥚");
        RECIPE_EMOJI.put("Spinach & Cheese Omelette", "🥚");
        RECIPE_EMOJI.put("Garlic Butter Pasta", "🍝");
        RECIPE_EMOJI.put("Simple Tomato Pasta", "🍝");
        RECIPE_EMOJI.put("Creamy Mushroom Pasta", "🍝");
        RECIPE_EMOJI.put("Chicken & Rice Bowl", "🍛");
        RECIPE_EMOJI.put("Chicken Stir Fry", "🍲");
        RECIPE_EMOJI.put("Bacon & Egg Fried Rice", "🍚");
        RECIPE_EMOJI.put("Garlic Fried Rice", "🍚");
        RECIPE_EMOJI.put("Vegetable Soup", "🍲");
        RECIPE_EMOJI.put("Mashed Potato", "🥔");
        RECIPE_EMOJI.put("Cheesy Baked Potato", "🥔");
        RECIPE_EMOJI.put("Grilled Cheese Sandwich", "🥪");
        RECIPE_EMOJI.put("Tuna Salad", "🥗");
        RECIPE_EMOJI.put("Bean & Corn Salad", "🥗");
        RECIPE_EMOJI.put("Banana Pancakes", "🥞");
        RECIPE_EMOJI.put("Honey Yogurt Bowl", "🥣");
        RECIPE_EMOJI.put("Apple Cinnamon Oats", "🥣");
    }

    /** Dish emoji for one of the seeded recipes, by exact name; a generic plate for anything
     * else (e.g. if the recipe collection is ever extended). */
    public static String emojiForRecipe(String recipeName) {
        String emoji = RECIPE_EMOJI.get(recipeName);
        return emoji != null ? emoji : "🍽️";
    }

    /** Best-guess default emoji for a freshly-typed ingredient name. Never returns null; falls
     * back to {@link #DEFAULT_EMOJI} when nothing matches (there is no Settings > Default Icon
     * preference in this project to fall back to instead). */
    public static String defaultEmojiFor(String ingredientName) {
        String match = matchByKeyword(ingredientName);
        return match != null ? match : DEFAULT_EMOJI;
    }

    private static String matchByKeyword(String ingredientName) {
        String key = normalize(ingredientName);
        for (Map.Entry<String, String> entry : INGREDIENT_EMOJI.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** A subtle background colour resource for the avatar behind the emoji. */
    public static int tintColorRes(String ingredientName) {
        String key = normalize(ingredientName);
        for (Map.Entry<String, Integer> entry : INGREDIENT_TINT.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return R.color.avatar_tint_neutral;
    }

    private static String normalize(String raw) {
        return raw == null ? "" : raw.toLowerCase(Locale.ROOT).trim();
    }
}
