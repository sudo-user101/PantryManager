package com.example.pantrybasic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pantrybasic.adapter.PantryAdapter;
import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;

import java.util.List;

/**
 * Launcher screen: shows every pantry item in a RecyclerView, backed by SQLite.
 * Tapping a row opens it for editing; the FAB opens a blank form to add a new one.
 */
public class PantryListActivity extends AppCompatActivity implements PantryAdapter.Listener {

    private DatabaseHelper databaseHelper;
    private PantryAdapter adapter;
    private View emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        databaseHelper = DatabaseHelper.getInstance(this);
        emptyState = findViewById(R.id.emptyStatePantry);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(this);
        recyclerView.setAdapter(adapter);

        View.OnClickListener openAddScreen = v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class));
        findViewById(R.id.fabAdd).setOnClickListener(openAddScreen);
        findViewById(R.id.buttonEmptyAdd).setOnClickListener(openAddScreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time the screen becomes visible again, so an add/edit/delete
        // made on the other screen is always reflected here.
        loadItems();
    }

    private void loadItems() {
        List<PantryItem> items = databaseHelper.getAllItems();
        adapter.setItems(items);
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(PantryItem item) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }
}
