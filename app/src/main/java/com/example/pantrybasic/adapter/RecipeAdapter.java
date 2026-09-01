package com.example.pantrybasic.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.R;
import com.example.pantrybasic.model.RecipeMatchResult;
import com.example.pantrybasic.util.FoodIconResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a list of {@link RecipeMatchResult}s - used for both the strict "Ready to make"
 * list (where {@code missingIngredients} is always empty) and the "Almost There" list (where
 * it always holds exactly one entry). Each row shows the recipe name and its match status, so
 * both lists can share one adapter/layout.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClick(RecipeMatchResult result);
    }

    private final List<RecipeMatchResult> items = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

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
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textStatus;
        private final FrameLayout avatarContainer;
        private final TextView textAvatarEmoji;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRecipeName);
            textStatus = itemView.findViewById(R.id.textStatus);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            textAvatarEmoji = itemView.findViewById(R.id.textAvatarEmoji);
        }

        void bind(RecipeMatchResult result, Listener listener) {
            Context context = itemView.getContext();
            String recipeName = result.getRecipe().getName();
            textName.setText(recipeName);

            textAvatarEmoji.setText(FoodIconResolver.emojiForRecipe(recipeName));
            Drawable avatarBg = ContextCompat.getDrawable(context, R.drawable.bg_avatar);
            if (avatarBg != null) {
                avatarBg = avatarBg.mutate();
                avatarBg.setTint(ContextCompat.getColor(context, R.color.avatar_tint_neutral));
            }
            avatarContainer.setBackground(avatarBg);

            int colorRes;
            if (result.isFullMatch()) {
                textStatus.setText(R.string.recipe_ready_now);
                colorRes = R.color.success;
            } else {
                int missing = result.getMissingIngredients().size();
                textStatus.setText(missing == 1
                        ? context.getString(R.string.recipe_missing_one)
                        : context.getString(R.string.recipe_missing_count, missing));
                colorRes = R.color.warning;
            }
            textStatus.setTextColor(ContextCompat.getColor(context, colorRes));
            applyStatusDot(context, colorRes);

            itemView.setOnClickListener(v -> listener.onRecipeClick(result));
        }

        private void applyStatusDot(Context context, int colorRes) {
            Drawable dot = ContextCompat.getDrawable(context, R.drawable.ic_dot_24);
            if (dot != null) {
                dot = dot.mutate();
                dot.setTint(ContextCompat.getColor(context, colorRes));
                int px = Math.round(6 * context.getResources().getDisplayMetrics().density);
                dot.setBounds(0, 0, px, px);
            }
            textStatus.setCompoundDrawables(dot, null, null, null);
        }
    }
}
