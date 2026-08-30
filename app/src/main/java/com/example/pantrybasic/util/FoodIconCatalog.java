package com.example.pantrybasic.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The fixed emoji catalogue shown in {@code IconPickerBottomSheet}, grouped into categories
 * (Recent/Food/Fruits/Vegetables/Dairy/Meat/Bakery/Drinks/Grains/Cooking), plus a small
 * persisted "recently used" list.
 * <p>
 * The recent list is stored in its own small SharedPreferences file rather than through
 * {@code androidx.preference}'s default-shared-preferences helper, since nothing else in this
 * project uses that library yet.
 */
public final class FoodIconCatalog {

    private FoodIconCatalog() {
    }

    private static final String PREFS_NAME = "pantry_basic_prefs";
    private static final String KEY_RECENT_ICONS = "recent_icon_emojis";
    private static final int MAX_RECENT = 12;

    public static final String CATEGORY_RECENT = "Recent";

    public static final Map<String, List<String>> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("Food", Arrays.asList(
                "🍽️", "🥫", "🍕", "🍔", "🌮", "🥙", "🍜", "🍲", "🥘", "🍱", "🧆", "🥟"));
        CATEGORIES.put("Fruits", Arrays.asList(
                "🍎", "🍌", "🍊", "🍋", "🍇", "🍓", "🍒", "🍑", "🥝", "🍍", "🥭", "🍐"));
        CATEGORIES.put("Vegetables", Arrays.asList(
                "🍅", "🥦", "🥕", "🥬", "🧅", "🧄", "🫑", "🥒", "🥔", "🌽", "🍄", "🫘"));
        CATEGORIES.put("Dairy", Arrays.asList(
                "🥛", "🧀", "🧈", "🍦", "🥚", "🍶"));
        CATEGORIES.put("Meat", Arrays.asList(
                "🍗", "🥩", "🥓", "🍖", "🌭", "🐟", "🍤", "🦐"));
        CATEGORIES.put("Bakery", Arrays.asList(
                "🍞", "🥐", "🥖", "🧁", "🍰", "🥯", "🥞", "🍪"));
        CATEGORIES.put("Drinks", Arrays.asList(
                "☕", "🍵", "🧃", "🥤", "🍷", "🍺", "🥂", "🧉"));
        CATEGORIES.put("Grains", Arrays.asList(
                "🍚", "🍝", "🌾", "🥣", "🫓", "🧇"));
        CATEGORIES.put("Cooking", Arrays.asList(
                "🫒", "🧂", "🍯", "🌿", "🫙", "🥄"));
    }

    /** Which catalogue category (if any) an emoji belongs to, for display purposes on the
     * Add/Edit "Food Icon" row - e.g. "🥕" -> "Vegetables". Returns null for an emoji that
     * isn't in the fixed catalogue (a resolver default not covered above, or a stray value). */
    public static String categoryOf(String emoji) {
        if (emoji == null) return null;
        for (Map.Entry<String, List<String>> entry : CATEGORIES.entrySet()) {
            if (entry.getValue().contains(emoji)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static List<String> getRecent(Context context) {
        SharedPreferences prefs = prefs(context);
        String raw = prefs.getString(KEY_RECENT_ICONS, "");
        List<String> result = new ArrayList<>();
        if (raw != null && !raw.trim().isEmpty()) {
            result.addAll(Arrays.asList(raw.split(",")));
        }
        return result;
    }

    /** Moves (or adds) an emoji to the front of the recent list, capped at {@link #MAX_RECENT}. */
    public static void recordUsed(Context context, String emoji) {
        if (emoji == null || emoji.isEmpty()) return;
        LinkedList<String> recent = new LinkedList<>(getRecent(context));
        recent.remove(emoji);
        recent.addFirst(emoji);
        while (recent.size() > MAX_RECENT) {
            recent.removeLast();
        }
        prefs(context).edit().putString(KEY_RECENT_ICONS, String.join(",", recent)).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
