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
import com.example.pantrybasic.util.DateUtils;
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

    /** Whether the "expiring soon" / "expired" indicator should be shown at all (Settings toggle). */
    private boolean expiryAlertsEnabled = true;

    public PantryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        this.expiryAlertsEnabled = enabled;
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
        private final TextView textExpiry;
        private final FrameLayout avatarContainer;
        private final TextView textAvatarEmoji;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            textAvatarEmoji = itemView.findViewById(R.id.textAvatarEmoji);
        }

        void bind(PantryItem item) {
            textName.setText(item.getName());
            textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
            bindAvatar(item);
            bindExpiry(item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private void bindExpiry(PantryItem item) {
            if (!expiryAlertsEnabled || !item.hasExpiryDate()) {
                textExpiry.setVisibility(View.GONE);
                return;
            }

            Context context = itemView.getContext();
            String display = DateUtils.formatForDisplay(item.getExpiryDate());

            if (DateUtils.isExpired(item.getExpiryDate())) {
                textExpiry.setVisibility(View.VISIBLE);
                textExpiry.setText(context.getString(R.string.expired_label) + " · " + display);
                textExpiry.setTextColor(ContextCompat.getColor(context, R.color.error));
                setStatusDot(context, R.color.error);
            } else if (DateUtils.isExpiringSoon(item.getExpiryDate())) {
                textExpiry.setVisibility(View.VISIBLE);
                textExpiry.setText(context.getString(R.string.expiring_soon_label) + " · " + display);
                textExpiry.setTextColor(ContextCompat.getColor(context, R.color.warning));
                setStatusDot(context, R.color.warning);
            } else {
                // Has an expiry date, but it's neither soon nor past - shown as plain secondary
                // text with no status dot at all (subtle, not alarming).
                textExpiry.setVisibility(View.VISIBLE);
                textExpiry.setText(display);
                textExpiry.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
                textExpiry.setCompoundDrawables(null, null, null, null);
            }
        }

        /** (Re-)applies the small leading status dot with the given tint - views are recycled,
         * so this must be set explicitly on every bind rather than relying on the XML default. */
        private void setStatusDot(Context context, int colorRes) {
            Drawable dot = ContextCompat.getDrawable(context, R.drawable.ic_dot_24);
            if (dot != null) {
                dot = dot.mutate();
                dot.setTint(ContextCompat.getColor(context, colorRes));
                int px = Math.round(7 * context.getResources().getDisplayMetrics().density);
                dot.setBounds(0, 0, px, px);
            }
            textExpiry.setCompoundDrawables(dot, null, null, null);
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
