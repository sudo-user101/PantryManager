package com.example.pantrybasic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid of tappable emoji cells for {@code IconPickerBottomSheet}. Single-select: tapping a
 * cell selects it (green outline, see bg_emoji_cell.xml) and reports the choice back via
 * {@link Listener#onEmojiSelected(String)} - the sheet's "Use This Icon" button reads the
 * current selection when confirmed, rather than dismissing immediately on tap, so the user can
 * change their mind before committing.
 */
public class EmojiGridAdapter extends RecyclerView.Adapter<EmojiGridAdapter.ViewHolder> {

    public interface Listener {
        void onEmojiSelected(String emoji);
    }

    private final List<String> emojis = new ArrayList<>();
    private final Listener listener;
    private String selectedEmoji;

    public EmojiGridAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<String> newEmojis, String currentlySelected) {
        emojis.clear();
        emojis.addAll(newEmojis);
        selectedEmoji = currentlySelected;
        notifyDataSetChanged();
    }

    public void setSelected(String emoji) {
        this.selectedEmoji = emoji;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emoji, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String emoji = emojis.get(position);
        holder.textView.setText(emoji);
        holder.textView.setSelected(emoji.equals(selectedEmoji));
        holder.textView.setOnClickListener(v -> listener.onEmojiSelected(emoji));
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }
}
