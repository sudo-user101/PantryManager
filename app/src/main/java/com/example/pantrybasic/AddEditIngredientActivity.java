package com.example.pantrybasic;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import com.example.pantrybasic.db.DatabaseHelper;
import com.example.pantrybasic.model.PantryItem;
import com.example.pantrybasic.util.AppPreferences;
import com.example.pantrybasic.util.DateUtils;
import com.example.pantrybasic.util.FoodIconCatalog;
import com.example.pantrybasic.util.FoodIconResolver;

import java.util.Calendar;

/**
 * One form used for both Create and Update: launched with no extra it inserts a new
 * item; launched with EXTRA_ITEM_ID it loads that item and updates it instead.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final long NO_ID = -1L;

    private DatabaseHelper databaseHelper;
    private long editingItemId = NO_ID;

    private TextView textFormTitle;
    private EditText editName;
    private EditText editQuantity;
    private EditText editUnit;
    private TextView textErrorName;
    private TextView textErrorQuantity;

    private FrameLayout avatarPreviewContainer;
    private TextView textAvatarPreview;
    private TextView textFoodIconEmoji;
    private TextView textFoodIconLabel;
    private TextView editExpiryDate;
    private TextView textClearDate;

    private String selectedIconEmoji = FoodIconResolver.DEFAULT_EMOJI;
    /** Once the user opens the picker and confirms a choice, typing in the Name field no
     * longer overrides the avatar - their explicit choice always wins. */
    private boolean iconManuallyChosen = false;

    /** Null while no expiry date has been chosen. */
    private String selectedExpiryIso = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        databaseHelper = DatabaseHelper.getInstance(this);

        findViewById(R.id.buttonBack).setOnClickListener(v -> navigateBack());

        textFormTitle = findViewById(R.id.textFormTitle);
        editName = findViewById(R.id.editName);
        editQuantity = findViewById(R.id.editQuantity);
        editUnit = findViewById(R.id.editUnit);
        textErrorName = findViewById(R.id.textErrorName);
        textErrorQuantity = findViewById(R.id.textErrorQuantity);
        avatarPreviewContainer = findViewById(R.id.avatarPreviewContainer);
        textAvatarPreview = findViewById(R.id.textAvatarPreview);
        textFoodIconEmoji = findViewById(R.id.textFoodIconEmoji);
        textFoodIconLabel = findViewById(R.id.textFoodIconLabel);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        textClearDate = findViewById(R.id.textClearDate);

        editExpiryDate.setOnClickListener(v -> showDatePicker());
        textClearDate.setOnClickListener(v -> clearExpiryDate());

        View buttonHelp = findViewById(R.id.buttonHelp);
        buttonHelp.setVisibility(AppPreferences.isTutorialModeEnabled(this) ? View.VISIBLE : View.GONE);
        buttonHelp.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(R.string.help_title_add_edit_ingredient)
                        .setMessage(R.string.help_body_add_edit_ingredient)
                        .setPositiveButton(R.string.action_got_it, null)
                        .show());

        View buttonSave = findViewById(R.id.buttonSave);
        MaterialButton buttonDelete = findViewById(R.id.buttonDelete);
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
            textFormTitle.setText(R.string.title_edit_ingredient);
            buttonDelete.setVisibility(View.VISIBLE);
            loadExistingItem(editingItemId);
        } else {
            textFormTitle.setText(R.string.title_add_ingredient);
            buttonDelete.setVisibility(View.GONE);
            selectedIconEmoji = FoodIconResolver.defaultEmojiFor("");
        }

        refreshIconDisplay();
    }

    private void navigateBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private void loadExistingItem(long id) {
        PantryItem item = databaseHelper.getItem(id);
        if (item == null) {
            // Deleted elsewhere between opening the list and tapping this row.
            navigateBack();
            return;
        }
        editName.setText(item.getName());
        editQuantity.setText(formatQuantity(item.getQuantity()));
        editUnit.setText(item.getUnit());

        selectedIconEmoji = item.getIconEmoji() != null && !item.getIconEmoji().isEmpty()
                ? item.getIconEmoji() : FoodIconResolver.defaultEmojiFor(item.getName());
        iconManuallyChosen = true; // don't let the name text-watcher override a saved choice

        if (item.hasExpiryDate()) {
            selectedExpiryIso = item.getExpiryDate();
            editExpiryDate.setText(DateUtils.formatForDisplay(selectedExpiryIso));
            editExpiryDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            textClearDate.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, dayOfMonth);
            selectedExpiryIso = DateUtils.formatForStorage(picked);
            editExpiryDate.setText(DateUtils.formatForDisplay(selectedExpiryIso));
            editExpiryDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            textClearDate.setVisibility(View.VISIBLE);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void clearExpiryDate() {
        selectedExpiryIso = null;
        editExpiryDate.setText(R.string.hint_pick_date);
        editExpiryDate.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary));
        textClearDate.setVisibility(View.GONE);
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
            databaseHelper.insertItem(new PantryItem(name, quantity, unit, selectedIconEmoji, selectedExpiryIso));
        } else {
            databaseHelper.updateItem(new PantryItem(editingItemId, name, quantity, unit, selectedIconEmoji, selectedExpiryIso));
        }

        Toast.makeText(this, R.string.action_save, Toast.LENGTH_SHORT).show();
        navigateBack();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    databaseHelper.deleteItem(editingItemId);
                    navigateBack();
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
