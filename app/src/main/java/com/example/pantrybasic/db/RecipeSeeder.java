package com.example.pantrybasic.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.pantrybasic.model.RecipeIngredient;

import java.util.Arrays;

/**
 * Provides the built-in recipe collection: nineteen recipes, each with its ingredient list
 * and preparation steps. Seeded once, when the recipe tables are first created.
 * <p>
 * Ingredient names/units deliberately reuse the same words across several recipes (e.g.
 * "egg", "onion", "cheese") so that editing a single pantry item can visibly affect several
 * recipes at once, once matching exists.
 */
final class RecipeSeeder {

    private RecipeSeeder() {
    }

    static void seed(SQLiteDatabase db) {
        insertRecipe(db,
                "Scrambled Eggs on Toast",
                "Beat eggs with a pinch of salt. Melt butter in a pan over low heat. "
                        + "Pour in the eggs and stir gently until softly set. Toast the bread and serve the eggs on top.",
                Arrays.asList(
                        new RecipeIngredient("egg", 2, "pcs"),
                        new RecipeIngredient("butter", 10, "g"),
                        new RecipeIngredient("bread", 2, "slice"),
                        new RecipeIngredient("salt", 1, "pinch")));

        insertRecipe(db,
                "Tomato & Onion Omelette",
                "Dice the tomato and onion. Heat oil in a pan and soften the onion. Add tomato and cook briefly. "
                        + "Beat eggs with salt, pour over the vegetables, and cook until set, folding in half to serve.",
                Arrays.asList(
                        new RecipeIngredient("egg", 3, "pcs"),
                        new RecipeIngredient("tomato", 1, "pcs"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("salt", 1, "pinch"),
                        new RecipeIngredient("olive oil", 1, "tbsp")));

        insertRecipe(db,
                "Garlic Butter Pasta",
                "Boil the pasta until al dente. Melt butter in a pan and gently fry crushed garlic until fragrant. "
                        + "Toss the drained pasta through the garlic butter, season with salt, and top with grated cheese.",
                Arrays.asList(
                        new RecipeIngredient("pasta", 200, "g"),
                        new RecipeIngredient("butter", 30, "g"),
                        new RecipeIngredient("garlic", 3, "clove"),
                        new RecipeIngredient("salt", 1, "pinch"),
                        new RecipeIngredient("cheese", 30, "g")));

        insertRecipe(db,
                "Simple Tomato Pasta",
                "Boil the pasta. Meanwhile, fry garlic in oil, add chopped tomato and simmer into a sauce. "
                        + "Season with basil and toss through the drained pasta.",
                Arrays.asList(
                        new RecipeIngredient("pasta", 200, "g"),
                        new RecipeIngredient("tomato", 4, "pcs"),
                        new RecipeIngredient("garlic", 2, "clove"),
                        new RecipeIngredient("olive oil", 2, "tbsp"),
                        new RecipeIngredient("basil", 1, "pinch")));

        insertRecipe(db,
                "Chicken & Rice Bowl",
                "Cook rice according to the packet instructions. Slice chicken and fry in oil with diced onion "
                        + "until cooked through. Stir in soy sauce and serve over the rice.",
                Arrays.asList(
                        new RecipeIngredient("chicken breast", 300, "g"),
                        new RecipeIngredient("rice", 200, "g"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("soy sauce", 2, "tbsp"),
                        new RecipeIngredient("olive oil", 1, "tbsp")));

        insertRecipe(db,
                "Chicken Stir Fry",
                "Slice the chicken and vegetables thinly. Stir-fry the chicken until browned, add the vegetables "
                        + "and grated ginger, then finish with soy sauce over high heat for 2-3 minutes.",
                Arrays.asList(
                        new RecipeIngredient("chicken breast", 300, "g"),
                        new RecipeIngredient("bell pepper", 1, "pcs"),
                        new RecipeIngredient("carrot", 1, "pcs"),
                        new RecipeIngredient("soy sauce", 2, "tbsp"),
                        new RecipeIngredient("ginger", 1, "clove")));

        insertRecipe(db,
                "Creamy Mushroom Pasta",
                "Boil the pasta. Fry sliced mushrooms and garlic in butter until golden. Stir in cream and simmer "
                        + "briefly. Toss through the drained pasta.",
                Arrays.asList(
                        new RecipeIngredient("pasta", 200, "g"),
                        new RecipeIngredient("mushroom", 150, "g"),
                        new RecipeIngredient("cream", 100, "ml"),
                        new RecipeIngredient("garlic", 2, "clove"),
                        new RecipeIngredient("butter", 20, "g")));

        insertRecipe(db,
                "Bacon & Egg Fried Rice",
                "Fry diced bacon until crisp, add chopped onion and cooked rice. Push the rice aside, scramble "
                        + "the eggs in the same pan, then mix everything together with soy sauce.",
                Arrays.asList(
                        new RecipeIngredient("rice", 300, "g"),
                        new RecipeIngredient("bacon", 100, "g"),
                        new RecipeIngredient("egg", 2, "pcs"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("soy sauce", 1, "tbsp")));

        insertRecipe(db,
                "Vegetable Soup",
                "Dice all the vegetables. Simmer in chicken stock for 20 minutes until soft. Season with salt "
                        + "and blend, or serve chunky.",
                Arrays.asList(
                        new RecipeIngredient("carrot", 2, "pcs"),
                        new RecipeIngredient("potato", 2, "pcs"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("chicken stock", 500, "ml"),
                        new RecipeIngredient("salt", 1, "pinch")));

        insertRecipe(db,
                "Mashed Potato",
                "Boil the peeled potatoes until soft. Drain and mash with butter, milk and salt until smooth.",
                Arrays.asList(
                        new RecipeIngredient("potato", 4, "pcs"),
                        new RecipeIngredient("butter", 30, "g"),
                        new RecipeIngredient("milk", 100, "ml"),
                        new RecipeIngredient("salt", 1, "pinch")));

        insertRecipe(db,
                "Cheesy Baked Potato",
                "Bake the potatoes until soft in the middle. Cut open, mash the inside slightly with butter and "
                        + "salt, top with grated cheese and return to the oven until melted.",
                Arrays.asList(
                        new RecipeIngredient("potato", 2, "pcs"),
                        new RecipeIngredient("cheese", 50, "g"),
                        new RecipeIngredient("butter", 20, "g"),
                        new RecipeIngredient("salt", 1, "pinch")));

        insertRecipe(db,
                "Grilled Cheese Sandwich",
                "Butter one side of each bread slice. Place cheese between the unbuttered sides and grill in a "
                        + "pan on medium heat until golden on both sides and the cheese has melted.",
                Arrays.asList(
                        new RecipeIngredient("bread", 2, "slice"),
                        new RecipeIngredient("cheese", 60, "g"),
                        new RecipeIngredient("butter", 10, "g")));

        insertRecipe(db,
                "Tuna Salad",
                "Drain the tuna and flake it into a bowl. Dice cucumber, tomato and onion and mix through. "
                        + "Dress with olive oil and serve chilled.",
                Arrays.asList(
                        new RecipeIngredient("tuna", 150, "g"),
                        new RecipeIngredient("cucumber", 1, "pcs"),
                        new RecipeIngredient("tomato", 1, "pcs"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("olive oil", 1, "tbsp")));

        insertRecipe(db,
                "Bean & Corn Salad",
                "Drain the beans and corn. Combine with diced tomato and onion in a bowl. Dress with olive oil "
                        + "and toss well before serving.",
                Arrays.asList(
                        new RecipeIngredient("beans", 200, "g"),
                        new RecipeIngredient("corn", 150, "g"),
                        new RecipeIngredient("tomato", 1, "pcs"),
                        new RecipeIngredient("onion", 1, "pcs"),
                        new RecipeIngredient("olive oil", 1, "tbsp")));

        insertRecipe(db,
                "Spinach & Cheese Omelette",
                "Wilt the spinach briefly in a buttered pan. Beat the eggs and pour over the spinach. Sprinkle "
                        + "cheese on top and cook until set, then fold and serve.",
                Arrays.asList(
                        new RecipeIngredient("egg", 3, "pcs"),
                        new RecipeIngredient("spinach", 50, "g"),
                        new RecipeIngredient("cheese", 40, "g"),
                        new RecipeIngredient("butter", 10, "g")));

        insertRecipe(db,
                "Banana Pancakes",
                "Mash the banana and whisk with egg and milk. Sift in flour and baking powder and mix into a "
                        + "smooth batter. Cook spoonfuls in a hot pan until bubbles form, then flip.",
                Arrays.asList(
                        new RecipeIngredient("flour", 200, "g"),
                        new RecipeIngredient("egg", 2, "pcs"),
                        new RecipeIngredient("milk", 250, "ml"),
                        new RecipeIngredient("banana", 1, "pcs"),
                        new RecipeIngredient("baking powder", 1, "tsp")));

        insertRecipe(db,
                "Honey Yogurt Bowl",
                "Spoon yogurt into a bowl. Slice banana over the top, sprinkle with oats and drizzle with honey.",
                Arrays.asList(
                        new RecipeIngredient("yogurt", 200, "g"),
                        new RecipeIngredient("honey", 2, "tbsp"),
                        new RecipeIngredient("banana", 1, "pcs"),
                        new RecipeIngredient("oats", 30, "g")));

        insertRecipe(db,
                "Apple Cinnamon Oats",
                "Simmer the oats in milk until thickened. Stir in diced apple and cinnamon. Top with a drizzle "
                        + "of honey before serving.",
                Arrays.asList(
                        new RecipeIngredient("oats", 60, "g"),
                        new RecipeIngredient("milk", 200, "ml"),
                        new RecipeIngredient("apple", 1, "pcs"),
                        new RecipeIngredient("cinnamon", 1, "pinch"),
                        new RecipeIngredient("honey", 1, "tbsp")));

        insertRecipe(db,
                "Garlic Fried Rice",
                "Fry crushed garlic in oil until golden and crisp. Add cooked rice and stir well. Push the rice "
                        + "aside, scramble the egg in the pan, then mix through with soy sauce.",
                Arrays.asList(
                        new RecipeIngredient("rice", 300, "g"),
                        new RecipeIngredient("garlic", 4, "clove"),
                        new RecipeIngredient("egg", 1, "pcs"),
                        new RecipeIngredient("soy sauce", 1, "tbsp"),
                        new RecipeIngredient("olive oil", 1, "tbsp")));
    }

    private static void insertRecipe(SQLiteDatabase db, String name, String steps, Iterable<RecipeIngredient> ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(DatabaseHelper.COL_RECIPE_NAME, name);
        recipeValues.put(DatabaseHelper.COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(DatabaseHelper.TABLE_RECIPES, null, recipeValues);

        for (RecipeIngredient ingredient : ingredients) {
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put(DatabaseHelper.COL_RI_RECIPE_ID, recipeId);
            ingredientValues.put(DatabaseHelper.COL_RI_NAME, ingredient.getName());
            ingredientValues.put(DatabaseHelper.COL_RI_QUANTITY, ingredient.getQuantity());
            ingredientValues.put(DatabaseHelper.COL_RI_UNIT, ingredient.getUnit());
            db.insert(DatabaseHelper.TABLE_RECIPE_INGREDIENTS, null, ingredientValues);
        }
    }
}
