package com.example.lostandfound;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ItemDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "extra_item";

    private LostFoundItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_item_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        item = (LostFoundItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        if (item != null) {
            populateViews(item);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(v -> removeItem());
    }

    private void populateViews(LostFoundItem item) {
        byte[] bytes = item.getImageBytes();
        if (bytes != null) {
            ImageView ivPhoto = findViewById(R.id.ivItemPhoto);
            ivPhoto.setImageBitmap(ImageHelper.bytesToBitmap(bytes));
            ivPhoto.setVisibility(View.VISIBLE);
            findViewById(R.id.ivItemIcon).setVisibility(View.GONE);
        }

        TextView tvBadge = findViewById(R.id.tvBadge);
        if ("Found".equals(item.getPostType())) {
            tvBadge.setText(R.string.label_found);
            tvBadge.setTextColor(getColor(R.color.badge_found_text));
            tvBadge.setBackgroundResource(R.drawable.bg_badge_found);
        } else {
            tvBadge.setText(R.string.label_lost);
            tvBadge.setTextColor(getColor(R.color.badge_lost_text));
            tvBadge.setBackgroundResource(R.drawable.bg_badge_lost);
        }

        ((TextView) findViewById(R.id.tvTitle)).setText(item.getTitle());
        ((TextView) findViewById(R.id.tvCategory)).setText(item.getCategory());
        ((TextView) findViewById(R.id.tvLocation)).setText(item.getLocation());
        ((TextView) findViewById(R.id.tvDescription)).setText(item.getDescription());
        ((TextView) findViewById(R.id.tvContactName)).setText(item.getName());
        ((TextView) findViewById(R.id.tvContactPhone)).setText(item.getPhone());
        ((TextView) findViewById(R.id.tvTimestamp)).setText(item.getTimestamp());
    }

    private void removeItem() {
        new DatabaseHelper(this).deleteItem(item.getId());
        finish();
    }
}