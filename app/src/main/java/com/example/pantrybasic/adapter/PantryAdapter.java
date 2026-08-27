package com.example.pantrybasic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.R;
import com.example.pantrybasic.model.PantryItem;

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

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
        }

        void bind(PantryItem item) {
            textName.setText(item.getName());
            textQuantity.setText(formatQuantity(item.getQuantity()) + " " + item.getUnit());
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        private String formatQuantity(double quantity) {
            if (quantity == Math.floor(quantity)) {
                return String.valueOf((long) quantity);
            }
            return String.valueOf(quantity);
        }
    }
}
