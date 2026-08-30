package com.example.pantrybasic;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.example.pantrybasic.adapter.EmojiGridAdapter;
import com.example.pantrybasic.util.FoodIconCatalog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Icon picker sheet: category tabs (Recent + the fixed food catalogue) above a grid of emoji,
 * single-select with a green outline, confirmed via "Use This Icon" rather than closing on
 * first tap so a choice can be reconsidered. Returns the chosen emoji to the caller via the
 * AndroidX Fragment Result API ({@link #REQUEST_KEY} / {@link #RESULT_EMOJI}) - decoupled from
 * whichever screen launched it, and survives configuration changes/process death better than a
 * raw callback interface would.
 */
public class IconPickerBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_KEY = "icon_picker_request";
    public static final String RESULT_EMOJI = "result_emoji";
    private static final String ARG_CURRENT_EMOJI = "arg_current_emoji";

    private final Map<String, TextView> categoryTabs = new LinkedHashMap<>();
    private EmojiGridAdapter gridAdapter;
    private String selectedEmoji;

    public static IconPickerBottomSheet newInstance(@Nullable String currentEmoji) {
        IconPickerBottomSheet sheet = new IconPickerBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENT_EMOJI, currentEmoji);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_icon_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedEmoji = getArguments() != null ? getArguments().getString(ARG_CURRENT_EMOJI) : null;

        view.findViewById(R.id.buttonCloseSheet).setOnClickListener(v -> dismiss());

        RecyclerView recyclerEmojis = view.findViewById(R.id.recyclerEmojis);
        recyclerEmojis.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        gridAdapter = new EmojiGridAdapter(emoji -> {
            selectedEmoji = emoji;
            gridAdapter.setSelected(emoji);
        });
        recyclerEmojis.setAdapter(gridAdapter);

        buildCategoryTabs(view.findViewById(R.id.rowCategories));

        List<String> recent = FoodIconCatalog.getRecent(requireContext());
        String startCategory = !recent.isEmpty() ? FoodIconCatalog.CATEGORY_RECENT
                : FoodIconCatalog.CATEGORIES.keySet().iterator().next();
        selectCategory(startCategory);

        view.findViewById(R.id.buttonUseIcon).setOnClickListener(v -> {
            String finalEmoji = selectedEmoji != null ? selectedEmoji
                    : (getArguments() != null ? getArguments().getString(ARG_CURRENT_EMOJI) : null);
            if (finalEmoji != null) {
                FoodIconCatalog.recordUsed(requireContext(), finalEmoji);
                Bundle result = new Bundle();
                result.putString(RESULT_EMOJI, finalEmoji);
                getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            }
            dismiss();
        });
    }

    private void buildCategoryTabs(LinearLayout row) {
        List<String> recent = FoodIconCatalog.getRecent(requireContext());
        if (!recent.isEmpty()) {
            addTab(row, FoodIconCatalog.CATEGORY_RECENT);
        }
        for (String category : FoodIconCatalog.CATEGORIES.keySet()) {
            addTab(row, category);
        }
    }

    private void addTab(LinearLayout row, String category) {
        TextView tab = new TextView(requireContext());
        tab.setText(category);
        tab.setTextSize(14f);
        tab.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dp(6));
        tab.setLayoutParams(params);
        tab.setOnClickListener(v -> selectCategory(category));
        row.addView(tab);
        categoryTabs.put(category, tab);
    }

    private void selectCategory(String category) {
        for (Map.Entry<String, TextView> entry : categoryTabs.entrySet()) {
            boolean active = entry.getKey().equals(category);
            entry.getValue().setTextColor(ContextCompat.getColor(requireContext(),
                    active ? R.color.success : R.color.text_secondary));
            entry.getValue().setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
        }

        List<String> emojis = FoodIconCatalog.CATEGORY_RECENT.equals(category)
                ? FoodIconCatalog.getRecent(requireContext())
                : new ArrayList<>(FoodIconCatalog.CATEGORIES.get(category));
        gridAdapter.submitList(emojis, selectedEmoji);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Convenience for callers: shows the sheet and wires the result listener in one call. */
    public static void show(FragmentManager fragmentManager, String currentEmoji,
                             androidx.lifecycle.LifecycleOwner lifecycleOwner,
                             OnIconChosen callback) {
        fragmentManager.setFragmentResultListener(REQUEST_KEY, lifecycleOwner, (requestKey, bundle) -> {
            String emoji = bundle.getString(RESULT_EMOJI);
            if (emoji != null) callback.onIconChosen(emoji);
        });
        newInstance(currentEmoji).show(fragmentManager, "icon_picker");
    }

    public interface OnIconChosen {
        void onIconChosen(String emoji);
    }
}
