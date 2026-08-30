package com.example.pantrybasic;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.util.FoodIconCatalog;
import com.example.pantrybasic.util.FoodIconResolver;

/**
 * One form used for both Create and Update: launched with no extra it inserts a new
 * item; launched with EXTRA_ITEM_ID it loads that item and updates it instead.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final long NO_ID = -1L;

    private DatabaseHelper databaseHelper;
    private long editingItemId = NO_ID;

    private EditText editName;
    private EditText editQuantity;
    private EditText editUnit;
    private TextView textErrorName;
    private TextView textErrorQuantity;

    private FrameLayout avatarPreviewContainer;
    private TextView textAvatarPreview;
    private TextView textFoodIconEmoji;
    private TextView textFoodIconLabel;

    private String selectedIconEmoji = FoodIconResolver.DEFAULT_EMOJI;
    /** Once the user opens the picker and confirms a choice, typing in the Name field no
     * longer overrides the avatar - their explicit choice always wins. */
    private boolean iconManuallyChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        databaseHelper = DatabaseHelper.getInstance(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editUnit = findViewById(R.id.editUnit);
        textErrorName = findViewById(R.id.textErrorName);
        textErrorQuantity = findViewById(R.id.textErrorQuantity);
        avatarPreviewContainer = findViewById(R.id.avatarPreviewContainer);
        textAvatarPreview = findViewById(R.id.textAvatarPreview);
        textFoodIconEmoji = findViewById(R.id.textFoodIconEmoji);
        textFoodIconLabel = findViewById(R.id.textFoodIconLabel);

        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        buttonSave.setOnClickListener(v -> attemptSave());
        buttonDelete.setOnClickListener(v -> confirmDelete());

        View.OnClickListener openPicker = v -> IconPickerBottomSheet.show(
                getSupportFragmentManager(), selectedIconEmoji, this, emoji -> {
                    selectedIconEmoji = emoji;
                    iconManuallyChosen = true;
                    refreshIconDisplay();
                });
        avatarPreviewContainer.setOnClickListener(openPicker);
        findViewById(R.id.rowFoodIcon).setOnClickListener(openPicker);

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!iconManuallyChosen) {
                    selectedIconEmoji = FoodIconResolver.defaultEmojiFor(s.toString());
                    refreshIconDisplay();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ID);
        if (editingItemId != NO_ID) {
            setTitle(R.string.title_edit_ingredient);
            buttonDelete.setVisibility(View.VISIBLE);
            loadExistingItem(editingItemId);
        } else {
            setTitle(R.string.title_add_ingredient);
            buttonDelete.setVisibility(View.GONE);
            selectedIconEmoji = FoodIconResolver.defaultEmojiFor("");
        }

        refreshIconDisplay();
    }

    private void refreshIconDisplay() {
        textAvatarPreview.setText(selectedIconEmoji);
        textFoodIconEmoji.setText(selectedIconEmoji);

        String category = FoodIconCatalog.categoryOf(selectedIconEmoji);
        textFoodIconLabel.setText(category != null ? category : getString(R.string.label_tap_to_change));

        Drawable bg = ContextCompat.getDrawable(this, R.drawable.bg_avatar);
        if (bg != null) {
            bg = bg.mutate();
            bg.setTint(ContextCompat.getColor(this, FoodIconResolver.tintColorRes(
                    editName.getText() != null ? editName.getText().toString() : "")));
        }
        avatarPreviewContainer.setBackground(bg);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadExistingItem(long id) {
        PantryItem item = databaseHelper.getItem(id);
        if (item == null) {
            // Deleted elsewhere between opening the list and tapping this row.
            finish();
            return;
        }
        editName.setText(item.getName());
        editQuantity.setText(formatQuantity(item.getQuantity()));
        editUnit.setText(item.getUnit());

        selectedIconEmoji = item.getIconEmoji() != null && !item.getIconEmoji().isEmpty()
                ? item.getIconEmoji() : FoodIconResolver.defaultEmojiFor(item.getName());
        iconManuallyChosen = true; // don't let the name text-watcher override a saved choice
    }

    private void attemptSave() {
        textErrorName.setVisibility(View.GONE);
        textErrorQuantity.setVisibility(View.GONE);

        String name = editName.getText() != null ? editName.getText().toString().trim() : "";
        String quantityText = editQuantity.getText() != null ? editQuantity.getText().toString().trim() : "";
        String unit = editUnit.getText() != null ? editUnit.getText().toString().trim() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            showError(textErrorName, R.string.error_name_required);
            valid = false;
        }

        double quantity = 0;
        if (TextUtils.isEmpty(quantityText)) {
            showError(textErrorQuantity, R.string.error_quantity_required);
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (quantity <= 0) {
                    showError(textErrorQuantity, R.string.error_quantity_invalid);
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(textErrorQuantity, R.string.error_quantity_invalid);
                valid = false;
            }
        }

        if (!valid) {
            return;
        }

        if (editingItemId == NO_ID) {
            databaseHelper.insertItem(new PantryItem(name, quantity, unit, selectedIconEmoji));
        } else {
            databaseHelper.updateItem(new PantryItem(editingItemId, name, quantity, unit, selectedIconEmoji));
        }

        Toast.makeText(this, R.string.action_save, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    databaseHelper.deleteItem(editingItemId);
                    finish();
                })
                .show();
    }

    private void showError(TextView errorView, int stringRes) {
        errorView.setText(stringRes);
        errorView.setVisibility(View.VISIBLE);
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}
