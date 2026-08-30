package com.example.pantrybasic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.R;
import com.example.pantrybasic.model.RecipeMatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of {@link RecipeMatchResult}s - used for both the strict "Ready to make"
 * list (where {@code missingIngredients} is always empty) and the "Almost There" list (where
 * it always holds exactly one entry). Each row shows the recipe name and its match status, so
 * both lists can share one adapter/layout.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final List<RecipeMatchResult> items = new ArrayList<>();

    public void setItems(List<RecipeMatchResult> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textStatus = itemView.findViewById(R.id.textStatus);
        }

        void bind(RecipeMatchResult result) {
            textName.setText(result.getRecipe().getName());

            if (result.isFullMatch()) {
                textStatus.setText(R.string.recipe_ready_now);
            } else {
                int missing = result.getMissingIngredients().size();
                textStatus.setText(missing == 1
                        ? itemView.getContext().getString(R.string.recipe_missing_one)
                        : itemView.getContext().getString(R.string.recipe_missing_count, missing));
            }
        }
    }
}
