package com.example.pantrybasic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe from the built-in collection: a name, its required ingredients, and simple
 * preparation steps. Ingredients are attached separately so a fully-populated Recipe can be
 * passed around once something actually reads them back (not yet - this migration only seeds
 * the data; nothing in the app queries it until the Suggested Recipes screen is migrated).
 */
public class Recipe {

    private long id;
    private String name;
    private String steps;
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }
}
