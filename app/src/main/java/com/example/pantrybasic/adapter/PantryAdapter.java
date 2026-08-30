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
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.util.FoodIconResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic RecyclerView adapter for the pantry list. Tapping a row opens it for editing.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    public interface Listener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final Listener listener;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /** Used by the swipe-to-delete ItemTouchHelper to resolve a swiped row back to its data. */
    public PantryItem getItemAt(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
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

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final FrameLayout avatarContainer;
        private final TextView textAvatarEmoji;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            textAvatarEmoji = itemView.findViewById(R.id.textAvatarEmoji);
        }

        void bind(PantryItem item) {
            textName.setText(item.getName());
            textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
            bindAvatar(item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private void bindAvatar(PantryItem item) {
            Context context = itemView.getContext();
            String emoji = item.getIconEmoji();
            textAvatarEmoji.setText(emoji != null && !emoji.isEmpty()
                    ? emoji : FoodIconResolver.defaultEmojiFor(item.getName()));

            Drawable bg = ContextCompat.getDrawable(context, R.drawable.bg_avatar);
            if (bg != null) {
                bg = bg.mutate();
                bg.setTint(ContextCompat.getColor(context, FoodIconResolver.tintColorRes(item.getName())));
            }
            avatarContainer.setBackground(bg);
        }

        private String formatQuantity(double quantity) {
            if (quantity == Math.floor(quantity)) {
                return String.valueOf((long) quantity);
            }
            return String.valueOf(quantity);
        }
    }
}
