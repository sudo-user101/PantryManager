package com.example.pantrybasic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.model.Recipe;
import com.example.pantrybasic.model.RecipeIngredient;
import com.example.pantrybasic.util.FoodIconResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLiteOpenHelper for the app's data. Two areas so far:
 * <ul>
 *     <li>{@code pantry_items} - the user's own ingredients (full CRUD)</li>
 *     <li>{@code recipes} / {@code recipe_ingredients} - a seeded recipe collection.
 *     Data foundation only for now: nothing in the app reads these back yet.</li>
 * </ul>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry_basic.db";
    private static final int DB_VERSION = 3;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_ICON = "icon_emoji";

    // recipes columns
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "name";
    public static final String COL_RECIPE_STEPS = "steps";

    // recipe_ingredients columns
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "name";
    public static final String COL_RI_QUANTITY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_ICON + " TEXT)");

        createRecipeTables(db);
        RecipeSeeder.seed(db);
    }

    /**
     * Migrates in place rather than dropping and recreating, so a version bump can never wipe
     * the user's own pantry data.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecipeTables(db);
            RecipeSeeder.seed(db);
        }
        if (oldVersion < 3) {
            // Existing rows get a NULL icon_emoji; fromCursor() resolves a fallback for those
            // at read time rather than backfilling every row here.
            db.execSQL("ALTER TABLE " + TABLE_PANTRY + " ADD COLUMN " + COL_ICON + " TEXT");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void createRecipeTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_NAME + " TEXT NOT NULL, "
                + COL_RECIPE_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RI_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_RI_NAME + " TEXT NOT NULL, "
                + COL_RI_QUANTITY + " REAL NOT NULL, "
                + COL_RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_RECIPE_ID + "))");
    }

    public long insertItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLE_PANTRY, null, toValues(item));
    }

    public int updateItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_PANTRY, toValues(item),
                COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public void deleteItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public PantryItem getItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PANTRY, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
            return null;
        }
    }

    public List<PantryItem> getAllItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    // ----------------------------------------------------------------
    // Recipes (read-only from the app - the collection is seeded once)
    // ----------------------------------------------------------------

    /** All recipes with their ingredient lists populated, ordered by name. */
    public List<Recipe> getAllRecipesWithIngredients() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_RECIPES, null, null, null,
                null, null, COL_RECIPE_NAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));
                recipes.add(new Recipe(id, name, steps));
            }
        }
        for (Recipe recipe : recipes) {
            recipe.setIngredients(getIngredientsForRecipe(recipe.getId()));
        }
        return recipes;
    }

    /** A single recipe, with its ingredient list populated, or null if it doesn't exist. */
    public Recipe getRecipeWithIngredients(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_RECIPES, null,
                COL_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, null)) {
            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_NAME));
                String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_RECIPE_STEPS));
                Recipe recipe = new Recipe(id, name, steps);
                recipe.setIngredients(getIngredientsForRecipe(id));
                return recipe;
            }
            return null;
        }
    }

    private List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null,
                COL_RI_RECIPE_ID + " = ?", new String[]{String.valueOf(recipeId)},
                null, null, COL_RI_ID + " ASC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RI_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_RI_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_RI_UNIT));
                ingredients.add(new RecipeIngredient(id, recipeId, name, quantity, unit));
            }
        }
        return ingredients;
    }

    /**
     * Wipes and re-seeds the recipe collection only (used by "Reset sample recipes" in
     * Settings). The user's pantry is untouched.
     */
    public void resetRecipes() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_RECIPE_INGREDIENTS, null, null);
            db.delete(TABLE_RECIPES, null, null);
            RecipeSeeder.seed(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_ICON, item.getIconEmoji());
        return values;
    }

    private PantryItem fromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT));
        String icon = cursor.getString(cursor.getColumnIndexOrThrow(COL_ICON));
        if (icon == null || icon.isEmpty()) {
            icon = FoodIconResolver.defaultEmojiFor(name);
        }
        return new PantryItem(id, name, quantity, unit, icon);
    }
}
