package com.example.pantrybasic.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Normalizes ingredient names and units so the strict-matching engine is robust to simple
 * real-world messiness - "Tomatoes" vs "tomato", "grams" vs "g", extra whitespace, mixed case,
 * and so on - without needing a full NLP/stemming library.
 * <p>
 * Handles both metric (g/kg, ml/l) and imperial (oz/lb, fl oz/cup/pint/gallon) mass/volume
 * units - oz and lb are just two more entries in the same "mass" family as g/kg, not a second
 * conversion system, so {@link #sameFamily} and {@link #convert} (used by the strict-matching
 * engine itself) already understand them once added here. {@link #toPreferredUnit} builds on
 * the same conversion tables purely for on-screen display (Settings > Preferences > Preferred
 * unit system) - it never affects what is actually stored.
 */
public final class UnitUtils {

    private UnitUtils() {
    }

    // Canonical unit -> synonyms that should map onto it.
    private static final Map<String, String> UNIT_SYNONYMS = new HashMap<>();

    static {
        put("g", "g", "gram", "grams", "gr");
        put("kg", "kg", "kilogram", "kilograms", "kgs");
        put("ml", "ml", "milliliter", "milliliters", "millilitre", "millilitres");
        put("l", "l", "liter", "liters", "litre", "litres");
        put("tsp", "tsp", "teaspoon", "teaspoons");
        put("tbsp", "tbsp", "tablespoon", "tablespoons");
        put("cup", "cup", "cups");
        put("pcs", "pcs", "pc", "piece", "pieces", "unit", "units", "whole");
        put("clove", "clove", "cloves");
        put("pinch", "pinch", "pinches");
        put("can", "can", "cans", "tin", "tins");
        put("slice", "slice", "slices");
        put("oz", "oz", "ounce", "ounces");
        put("lb", "lb", "lbs", "pound", "pounds");
        put("fl oz", "fl oz", "floz", "fluid ounce", "fluid ounces");
        put("pint", "pint", "pints", "pt");
        put("gallon", "gallon", "gallons", "gal");
    }

    private static void put(String canonical, String... synonyms) {
        for (String s : synonyms) {
            UNIT_SYNONYMS.put(s, canonical);
        }
    }

    /**
     * Grams-per-canonical-unit for units that belong to the "mass" or "volume" family, so
     * quantities can be compared across e.g. g/kg or ml/l. Units outside these families (pcs,
     * clove, pinch, ...) are compared directly, unit-for-unit.
     */
    private static final Map<String, Double> MASS_TO_GRAMS = new HashMap<>();
    private static final Map<String, Double> VOLUME_TO_ML = new HashMap<>();

    static {
        MASS_TO_GRAMS.put("g", 1.0);
        MASS_TO_GRAMS.put("kg", 1000.0);
        MASS_TO_GRAMS.put("oz", 28.349523125);
        MASS_TO_GRAMS.put("lb", 453.59237);

        VOLUME_TO_ML.put("ml", 1.0);
        VOLUME_TO_ML.put("l", 1000.0);
        VOLUME_TO_ML.put("fl oz", 29.5735295625);
        VOLUME_TO_ML.put("cup", 236.5882365);
        VOLUME_TO_ML.put("pint", 473.176473);
        VOLUME_TO_ML.put("gallon", 3785.411784);
    }

    private static final java.util.Set<String> IMPERIAL_MASS_UNITS =
            new java.util.HashSet<>(java.util.Arrays.asList("oz", "lb"));
    private static final java.util.Set<String> IMPERIAL_VOLUME_UNITS =
            new java.util.HashSet<>(java.util.Arrays.asList("fl oz", "cup", "pint", "gallon"));

    /** Lower-cases, trims, strips punctuation and collapses whitespace. */
    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        // Locale.ROOT: this is ASCII ingredient-name normalization, not user-facing text, so
        // it must behave identically regardless of the device's locale (e.g. avoiding the
        // Turkish "i" mapping issue).
        String s = rawName.toLowerCase(Locale.ROOT).trim();
        s = s.replaceAll("[^a-z0-9\\s-]", "");
        s = s.replaceAll("\\s+", " ").trim();
        return singularize(s);
    }

    /**
     * A small set of rules that cover the common English pluralization patterns seen in
     * ingredient names, e.g. "tomatoes" -> "tomato", "berries" -> "berry", "onions" -> "onion".
     */
    private static String singularize(String s) {
        if (s.isEmpty()) return s;

        // Multi-word names: only singularize the final word
        // (e.g. "chicken breasts" -> "chicken breast").
        int lastSpace = s.lastIndexOf(' ');
        String prefix = lastSpace >= 0 ? s.substring(0, lastSpace + 1) : "";
        String last = lastSpace >= 0 ? s.substring(lastSpace + 1) : s;

        if (last.length() > 3 && last.endsWith("ies")) {
            last = last.substring(0, last.length() - 3) + "y"; // berries -> berry
        } else if (last.endsWith("oes") && last.length() > 4) {
            last = last.substring(0, last.length() - 2); // tomatoes -> tomato, potatoes -> potato
        } else if (last.endsWith("ches") || last.endsWith("shes") || last.endsWith("xes")) {
            last = last.substring(0, last.length() - 2); // dishes -> dish
        } else if (last.endsWith("ss")) {
            // leave as-is (e.g. "molasses")
        } else if (last.endsWith("s") && last.length() > 3) {
            last = last.substring(0, last.length() - 1); // onions -> onion, eggs -> egg
        }

        return prefix + last;
    }

    /** Maps a free-text unit (e.g. "Grams", "TSP", "pieces") to its canonical short form. */
    public static String normalizeUnit(String rawUnit) {
        if (rawUnit == null) return "";
        String key = rawUnit.toLowerCase(Locale.ROOT).trim();
        String canonical = UNIT_SYNONYMS.get(key);
        return canonical != null ? canonical : key;
    }

    /**
     * True if the two (already-normalized) units belong to the same convertible family (both
     * mass, or both volume), meaning quantities can be safely compared/converted between them.
     */
    public static boolean sameFamily(String unitA, String unitB) {
        if (unitA.equals(unitB)) return true;
        boolean bothMass = MASS_TO_GRAMS.containsKey(unitA) && MASS_TO_GRAMS.containsKey(unitB);
        boolean bothVolume = VOLUME_TO_ML.containsKey(unitA) && VOLUME_TO_ML.containsKey(unitB);
        return bothMass || bothVolume;
    }

    /**
     * Converts a quantity from one unit into another within the same family. Returns the
     * original quantity unchanged if the units are already equal or are not part of a known
     * convertible family.
     */
    public static double convert(double quantity, String fromUnit, String toUnit) {
        if (fromUnit.equals(toUnit)) return quantity;

        if (MASS_TO_GRAMS.containsKey(fromUnit) && MASS_TO_GRAMS.containsKey(toUnit)) {
            double grams = quantity * MASS_TO_GRAMS.get(fromUnit);
            return grams / MASS_TO_GRAMS.get(toUnit);
        }
        if (VOLUME_TO_ML.containsKey(fromUnit) && VOLUME_TO_ML.containsKey(toUnit)) {
            double ml = quantity * VOLUME_TO_ML.get(fromUnit);
            return ml / VOLUME_TO_ML.get(toUnit);
        }
        return quantity;
    }

    /**
     * A (quantity, unit) pair meant purely for on-screen display under the user's "Preferred
     * unit system" (Settings > Preferences) - see {@link #toPreferredUnit}. Never write this
     * back to the database; the canonical stored value is whatever {@code PantryItem}/
     * {@code RecipeIngredient} already holds.
     */
    public static final class DisplayQuantity {
        public final double quantity;
        public final String unit;

        DisplayQuantity(double quantity, String unit) {
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    /**
     * Converts a canonical, stored (quantity, unit) pair into the pair that should be shown to
     * the user for the given {@code unitSystem} ("metric" or "imperial", see
     * {@link AppPreferences#getUnitSystem}) - a pure, read-only conversion for display that
     * never touches what is actually persisted.
     * <p>
     * Count-based units (pcs, clove, pinch, tsp, tbsp, slice, can, ...) are never converted -
     * "2 pcs egg" stays "2 pcs egg" regardless of preference, since converting a count into a
     * mass/volume unit would be meaningless. A mass/volume unit that is already in the family
     * the preference calls for is returned completely unchanged - not even re-rounded - so
     * repeatedly viewing (or re-saving without editing, see AddEditIngredientActivity) the same
     * stored value can never drift. Only a genuine cross-family conversion (metric stock viewed
     * under Imperial, or vice versa) rounds the result, to one decimal place, for a readable
     * display - e.g. 500 g -> 17.6 oz, not 17.63698... oz.
     * <p>
     * Within a family, the target unit is chosen by magnitude so the display reads naturally
     * (500 g -> oz, 1 kg -> lb; 250 ml -> fl oz, 1 l -> cup) rather than always picking the same
     * single unit regardless of size.
     */
    public static DisplayQuantity toPreferredUnit(double quantity, String unit, String unitSystem) {
        String normalizedUnit = normalizeUnit(unit);
        boolean wantImperial = "imperial".equals(unitSystem);

        if (MASS_TO_GRAMS.containsKey(normalizedUnit)) {
            boolean isImperialUnit = IMPERIAL_MASS_UNITS.contains(normalizedUnit);
            if (isImperialUnit == wantImperial) {
                return new DisplayQuantity(quantity, unit);
            }
            double grams = quantity * MASS_TO_GRAMS.get(normalizedUnit);
            String targetUnit = wantImperial
                    ? (grams < 1000 ? "oz" : "lb")
                    : (grams < 1000 ? "g" : "kg");
            return new DisplayQuantity(
                    roundToOneDecimal(grams / MASS_TO_GRAMS.get(targetUnit)), targetUnit);
        }

        if (VOLUME_TO_ML.containsKey(normalizedUnit)) {
            boolean isImperialUnit = IMPERIAL_VOLUME_UNITS.contains(normalizedUnit);
            if (isImperialUnit == wantImperial) {
                return new DisplayQuantity(quantity, unit);
            }
            double ml = quantity * VOLUME_TO_ML.get(normalizedUnit);
            String targetUnit = wantImperial
                    ? (ml < 1000 ? "fl oz" : "cup")
                    : (ml < 1000 ? "ml" : "l");
            return new DisplayQuantity(
                    roundToOneDecimal(ml / VOLUME_TO_ML.get(targetUnit)), targetUnit);
        }

        // Count-based or unrecognized unit - never converted.
        return new DisplayQuantity(quantity, unit);
    }

    private static double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    /** Formats a (possibly display-converted) quantity the same way the rest of the app already
     * formats quantities - no trailing ".0" for whole numbers - so a converted value reads
     * "18", not "18.0". */
    public static String formatDisplayQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
