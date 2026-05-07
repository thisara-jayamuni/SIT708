package com.example.lostandfound;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class ItemListActivity extends AppCompatActivity {

    private ItemAdapter adapter;
    private List<LostFoundItem> allItems = new ArrayList<>();
    private String activeCategory = "All";
    private String searchQuery    = "";

    private final ActivityResultLauncher<Intent> detailsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Item may have been removed — list refreshes automatically in onResume
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupRecyclerView();
        setupSearch();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItemsFromDatabase();
    }

    private void loadItemsFromDatabase() {
        allItems = new DatabaseHelper(this).getAllItems();
        rebuildChips();
        applyFilters();
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new ItemAdapter();
        adapter.setOnItemClickListener(item -> {
            Intent intent = new Intent(this, ItemDetailsActivity.class);
            intent.putExtra(ItemDetailsActivity.EXTRA_ITEM, item);
            detailsLauncher.launch(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void rebuildChips() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupCategories);
        chipGroup.removeAllViews();

        List<String> categories = new ArrayList<>();
        categories.add("All");
        for (LostFoundItem item : allItems) {
            if (!categories.contains(item.getCategory())) {
                categories.add(item.getCategory());
            }
        }

        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChecked(category.equals(activeCategory));
            styleChip(chip, category.equals(activeCategory));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    activeCategory = category;
                    for (int i = 0; i < chipGroup.getChildCount(); i++) {
                        Chip c = (Chip) chipGroup.getChildAt(i);
                        styleChip(c, c == chip);
                        if (c != chip) c.setChecked(false);
                    }
                    applyFilters();
                }
            });
            chipGroup.addView(chip);
        }
    }

    private void styleChip(Chip chip, boolean selected) {
        if (selected) {
            chip.setChipBackgroundColorResource(R.color.chip_selected_bg);
            chip.setTextColor(getColor(R.color.chip_selected_text));
            chip.setChipStrokeColorResource(R.color.chip_selected_stroke);
        } else {
            chip.setChipBackgroundColorResource(R.color.chip_unselected_bg);
            chip.setTextColor(getColor(R.color.chip_unselected_text));
            chip.setChipStrokeColorResource(R.color.chip_unselected_stroke);
        }
        chip.setChipStrokeWidth(1.5f);
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void applyFilters() {
        List<LostFoundItem> filtered = new ArrayList<>();
        for (LostFoundItem item : allItems) {
            boolean matchesCategory = activeCategory.equals("All") || item.getCategory().equals(activeCategory);
            boolean matchesSearch   = searchQuery.isEmpty()
                    || item.getTitle().toLowerCase().contains(searchQuery)
                    || item.getDescription().toLowerCase().contains(searchQuery)
                    || item.getLocation().toLowerCase().contains(searchQuery);
            if (matchesCategory && matchesSearch) filtered.add(item);
        }

        adapter.setItems(filtered);

        ((TextView) findViewById(R.id.tvResultCount)).setText(
                filtered.size() + (filtered.size() == 1 ? " item" : " items") + " found");

        boolean empty = filtered.isEmpty();
        findViewById(R.id.recyclerView).setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.layoutEmpty).setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}