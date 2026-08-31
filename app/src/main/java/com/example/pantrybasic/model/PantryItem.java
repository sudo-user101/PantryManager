package com.example.pantrybasic.model;

/**
 * A single pantry ingredient: a name, a quantity, and a unit (e.g. "Rice", 2, "kg").
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;

    /** A single emoji chosen for this item's avatar (e.g. "🍅"). See
     * {@code DatabaseHelper.COL_ICON} - null for rows created through the constructors below
     * that don't take one; {@code DatabaseHelper} resolves a fallback via
     * {@link com.example.pantrybasic.util.FoodIconResolver#defaultEmojiFor(String)} at read
     * time, so it is never left null in practice once loaded back from the database. */
    private String iconEmoji;

    /** ISO-8601 "yyyy-MM-dd" string, or null if the user did not set one. */
    private String expiryDate;

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public PantryItem(long id, String name, double quantity, String unit, String iconEmoji) {
        this(id, name, quantity, unit);
        this.iconEmoji = iconEmoji;
    }

    public PantryItem(long id, String name, double quantity, String unit, String iconEmoji, String expiryDate) {
        this(id, name, quantity, unit, iconEmoji);
        this.expiryDate = expiryDate;
    }

    /** Convenience constructor for a new item that doesn't have an id yet. */
    public PantryItem(String name, double quantity, String unit) {
        this(0L, name, quantity, unit);
    }

    /** Convenience constructor for a new item that doesn't have an id yet, with an icon chosen. */
    public PantryItem(String name, double quantity, String unit, String iconEmoji) {
        this(0L, name, quantity, unit, iconEmoji);
    }

    /** Convenience constructor for a new item that doesn't have an id yet, with an icon and
     * expiry date chosen. */
    public PantryItem(String name, double quantity, String unit, String iconEmoji, String expiryDate) {
        this(0L, name, quantity, unit, iconEmoji, expiryDate);
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

    public String getIconEmoji() {
        return iconEmoji;
    }

    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.trim().isEmpty();
    }
}
