package com.example.pantrybasic.model;

/**
 * A single ingredient requirement that belongs to a {@link Recipe}, e.g. "2 cups flour".
 * Stored as its own row (rather than a flattened string) so a future matching engine can
 * compare quantities and units precisely.
 * <p>
 * Pure data only at this stage - no display/formatting helpers, since nothing in the app
 * reads these back yet.
 */
public class RecipeIngredient {

    private long id;
    private long recipeId;
    private String name;
    private double quantity;
    private String unit;

    public RecipeIngredient() {
    }

    public RecipeIngredient(long id, long recipeId, String name, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    /** Convenience constructor used by the seed data, where the id/recipeId are assigned by the DB. */
    public RecipeIngredient(String name, double quantity, String unit) {
        this(0L, 0L, name, quantity, unit);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(long recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    /** Human readable form, e.g. "200 g flour", used on the Recipe Detail screen. */
    public String toDisplayString() {
        String qty = (quantity == Math.floor(quantity))
                ? String.valueOf((long) quantity)
                : String.valueOf(quantity);
        return qty + " " + unit + " " + name;
    }
}
