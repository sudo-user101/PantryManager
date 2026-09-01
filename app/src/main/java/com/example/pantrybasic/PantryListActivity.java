package com.example.pantrybasic;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.pantrybasic.adapter.PantryAdapter;
import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.util.AppPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Launcher screen: shows every pantry item in a RecyclerView, backed by SQLite. Tapping a row
 * opens it for editing; the FAB opens a blank form to add a new one. Also owns simple
 * client-side search (by name substring) over whatever the database currently holds - it
 * operates on an in-memory copy, so it never touches persistence - and a left-swipe-to-delete
 * gesture as a second way to remove a row (alongside the Delete button inside Edit).
 */
public class PantryListActivity extends BaseActivity implements PantryAdapter.Listener {

    private DatabaseHelper databaseHelper;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStatePantry;
    private View emptyStateNoResults;
    private EditText editSearch;
    private View buttonClearSearch;

    private final List<PantryItem> allItems = new ArrayList<>();
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        databaseHelper = DatabaseHelper.getInstance(this);
        emptyStatePantry = findViewById(R.id.emptyStatePantry);
        emptyStateNoResults = findViewById(R.id.emptyStateNoResults);

        recyclerView = findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        View.OnClickListener openAddScreen = v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class));
        fab.setOnClickListener(openAddScreen);
        findViewById(R.id.buttonEmptyAdd).setOnClickListener(openAddScreen);

        findViewById(R.id.buttonHelp).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.help_title_pantry_list)
                        .setMessage(R.string.help_body_pantry_list)
                        .setPositiveButton(R.string.action_got_it, null)
                        .show());

        setupSearch();
        setupFloatingNav(NAV_PANTRY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time the screen becomes visible again, so an add/edit/delete made on
        // the other screen is always reflected here.
        boolean wasEmpty = adapter.getItemCount() == 0;
        loadItems();
        if (wasEmpty) {
            recyclerView.scheduleLayoutAnimation();
        }
        findViewById(R.id.buttonHelp).setVisibility(
                AppPreferences.isTutorialModeEnabled(this) ? View.VISIBLE : View.GONE);
    }

    private void setupSearch() {
        editSearch = findViewById(R.id.editSearch);
        buttonClearSearch = findViewById(R.id.buttonClearSearch);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                buttonClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        buttonClearSearch.setOnClickListener(v -> editSearch.setText(""));
    }

    private void loadItems() {
        allItems.clear();
        allItems.addAll(databaseHelper.getAllItems());
        adapter.setExpiryAlertsEnabled(AppPreferences.isExpiryAlertsEnabled(this));
        applyFilter();
    }

    private void applyFilter() {
        List<PantryItem> filtered = new ArrayList<>();
        String query = searchQuery.toLowerCase(Locale.ROOT);
        for (PantryItem item : allItems) {
            if (query.isEmpty() || item.getName().toLowerCase(Locale.ROOT).contains(query)) {
                filtered.add(item);
            }
        }
        // Already alphabetical from the DB query - re-sort defensively in case that ever changes.
        Collections.sort(filtered, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        adapter.setItems(filtered);

        boolean searching = !searchQuery.isEmpty();
        emptyStatePantry.setVisibility(!searching && allItems.isEmpty() ? View.VISIBLE : View.GONE);
        emptyStateNoResults.setVisibility(searching && filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Wires a left-swipe-to-delete gesture onto the pantry RecyclerView. Swiping only reveals
     * a red background + trash icon and triggers a confirmation dialog - cancelling snaps the
     * row back into place via notifyItemChanged, since the underlying data was never actually
     * removed until the dialog is confirmed.
     */
    private void attachSwipeToDelete() {
        float density = getResources().getDisplayMetrics().density;
        Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_24);
        if (deleteIcon != null) {
            deleteIcon = deleteIcon.mutate();
            deleteIcon.setTint(ContextCompat.getColor(this, R.color.white));
        }
        Drawable finalDeleteIcon = deleteIcon;
        int cornerRadiusPx = Math.round(14 * density);
        int iconSizePx = Math.round(24 * density);
        int iconMarginPx = Math.round(20 * density);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                   @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;
                PantryItem item = adapter.getItemAt(position);

                new AlertDialog.Builder(PantryListActivity.this)
                        .setTitle(R.string.delete_confirm_title)
                        .setMessage(R.string.delete_confirm_message)
                        .setNegativeButton(R.string.action_cancel, (dialog, which) ->
                                adapter.notifyItemChanged(position))
                        .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                        .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                            databaseHelper.deleteItem(item.getId());
                            loadItems();
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                     float dX, float dY, int actionState,
                                     boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setColor(ContextCompat.getColor(PantryListActivity.this, R.color.error));

                    RectF background = new RectF(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    c.drawRoundRect(background, cornerRadiusPx, cornerRadiusPx, paint);

                    if (finalDeleteIcon != null) {
                        int iconTop = itemView.getTop() + (itemView.getHeight() - iconSizePx) / 2;
                        int iconRight = itemView.getRight() - iconMarginPx;
                        int iconLeft = iconRight - iconSizePx;
                        if (iconLeft > background.left) {
                            finalDeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconTop + iconSizePx);
                            finalDeleteIcon.draw(c);
                        }
                    }
                }
                super.onChildDraw(c, rv, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }
}
