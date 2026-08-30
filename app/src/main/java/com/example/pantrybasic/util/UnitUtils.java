package com.example.pantrybasic.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Normalizes ingredient names and units so the strict-matching engine is robust to simple
 * real-world messiness - "Tomatoes" vs "tomato", "grams" vs "g", extra whitespace, mixed case,
 * and so on - without needing a full NLP/stemming library.
 * <p>
 * Metric-only for now: mass (g/kg) and volume (ml/l) conversion. Imperial units and a
 * preferred-unit-system display conversion are a separate, later feature.
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

        VOLUME_TO_ML.put("ml", 1.0);
        VOLUME_TO_ML.put("l", 1000.0);
    }

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
}
