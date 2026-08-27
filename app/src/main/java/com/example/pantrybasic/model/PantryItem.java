package com.example.pantrybasic.model;

/**
 * A single pantry ingredient: a name, a quantity, and a unit (e.g. "Rice", 2, "kg").
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    /** Convenience constructor for a new item that doesn't have an id yet. */
    public PantryItem(String name, double quantity, String unit) {
        this(0L, name, quantity, unit);
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
}
