package com.example.lostandfound;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateAdvertActivity extends AppCompatActivity {

    private RadioButton rbLost;
    private Spinner     spinnerCategory;
    private EditText    etTitle, etDescription, etLocation, etName, etPhone;
    private ImageView   ivImagePreview;
    private Button      btnUploadImage;

    private byte[] imageBytes = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK
                                && result.getData() != null
                                && result.getData().getData() != null) {
                            handleImageResult(result.getData().getData());
                        }
                    });

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        if (granted) openGallery();
                        else Toast.makeText(this,
                                "Storage permission is needed to upload photos",
                                Toast.LENGTH_LONG).show();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_advert);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupCategorySpinner();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnUploadImage).setOnClickListener(v -> checkPermissionAndOpenGallery());
        findViewById(R.id.btnSave).setOnClickListener(v -> attemptSave());
    }

    private void initViews() {
        rbLost          = findViewById(R.id.rbLost);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etTitle         = findViewById(R.id.etTitle);
        etDescription   = findViewById(R.id.etDescription);
        etLocation      = findViewById(R.id.etLocation);
        etName          = findViewById(R.id.etName);
        etPhone         = findViewById(R.id.etPhone);
        ivImagePreview  = findViewById(R.id.ivImagePreview);
        btnUploadImage  = findViewById(R.id.btnUploadImage);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void checkPermissionAndOpenGallery() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            permissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageResult(Uri uri) {
        try {
            Bitmap bitmap = ImageHelper.uriToBitmap(this, uri);
            imageBytes = ImageHelper.bitmapToBytes(bitmap);
            ivImagePreview.setImageBitmap(bitmap);
            ivImagePreview.setVisibility(View.VISIBLE);
            btnUploadImage.setText(getString(R.string.btn_change_photo));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.toast_image_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private void attemptSave() {
        String title       = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();
        String name        = etName.getText().toString().trim();
        String phone       = etPhone.getText().toString().trim();
        String category    = spinnerCategory.getSelectedItem().toString();
        String postType    = rbLost.isChecked() ? "Lost" : "Found";

        if (title.isEmpty())       { etTitle.setError(getString(R.string.error_title)); etTitle.requestFocus(); return; }
        if (description.isEmpty()) { etDescription.setError(getString(R.string.error_description)); etDescription.requestFocus(); return; }
        if (location.isEmpty())    { etLocation.setError(getString(R.string.error_location)); etLocation.requestFocus(); return; }
        if (name.isEmpty())        { etName.setError(getString(R.string.error_name)); etName.requestFocus(); return; }
        if (phone.isEmpty())       { etPhone.setError(getString(R.string.error_phone)); etPhone.requestFocus(); return; }
        if (category.equals(getString(R.string.categories_hint))) {
            Toast.makeText(this, getString(R.string.error_category), Toast.LENGTH_SHORT).show(); return;
        }
        if (imageBytes == null) {
            Toast.makeText(this, getString(R.string.error_image), Toast.LENGTH_SHORT).show(); return;
        }

        String timestamp = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date());

        DatabaseHelper db = new DatabaseHelper(this);
        long rowId = db.insertItem(postType, category, title, description,
                location, name, phone, timestamp, imageBytes);

        if (rowId != -1) {
            Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, getString(R.string.toast_save_fail), Toast.LENGTH_SHORT).show();
        }
    }
}