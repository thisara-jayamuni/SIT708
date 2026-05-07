package com.example.eventplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditEventFragment extends Fragment {

    private EventViewModel viewModel;

    // Form fields
    private TextInputLayout tilTitle, tilDateTime;
    private TextInputEditText etTitle, etLocation, etDateTime;
    private ChipGroup chipGroupCategory;
    private MaterialButton btnSave;
    private ImageButton btnBack;
    private TextView tvEditBadge, tvError;
    private LinearLayout layoutError;

    // State
    private long selectedDateTime = 0;
    private int editingEventId = -1; // -1 = Add mode

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        tilTitle          = view.findViewById(R.id.tilTitle);
        tilDateTime       = view.findViewById(R.id.tilDateTime);
        etTitle           = view.findViewById(R.id.etTitle);
        etLocation        = view.findViewById(R.id.etLocation);
        etDateTime        = view.findViewById(R.id.etDateTime);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        btnSave           = view.findViewById(R.id.btnSave);
        btnBack           = view.findViewById(R.id.btnBack);
        tvEditBadge       = view.findViewById(R.id.tvEditBadge);
        tvError           = view.findViewById(R.id.tvError);
        layoutError       = view.findViewById(R.id.layoutError);

        viewModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);

        // Read the eventId argument from the navigation graph
        // Default is -1 (Add mode); a valid ID means Edit mode
        if (getArguments() != null) {
            editingEventId = getArguments().getInt("eventId", -1);
        }

        if (editingEventId != -1) {
            setupEditMode();
        }

        // Date + Time picker opens on field tap
        etDateTime.setOnClickListener(v -> showDateTimePicker());
        view.findViewById(R.id.tilDateTime).setEndIconOnClickListener(v -> showDateTimePicker());

        // Back button navigates up
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        // Save / Update button
        btnSave.setOnClickListener(v -> validateAndSave());
    }

    // -------------------------------------------------------------------------
    // Edit mode: show back arrow, badge, and pre-fill fields from the database
    // -------------------------------------------------------------------------
    private void setupEditMode() {
        btnBack.setVisibility(View.VISIBLE);
        tvEditBadge.setVisibility(View.VISIBLE);
        btnSave.setText("Update event");

        // Observe the specific event and pre-fill the form once
        viewModel.getEventById(editingEventId).observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;

            etTitle.setText(event.title);
            etLocation.setText(event.location);

            // Pre-select the correct category chip
            selectCategoryChip(event.category);

            // Pre-fill the date/time display
            selectedDateTime = event.dateTime;
            String formatted = new SimpleDateFormat(
                    "EEE dd MMM yyyy · hh:mm a", Locale.getDefault())
                    .format(selectedDateTime);
            etDateTime.setText(formatted);
        });
    }

    // -------------------------------------------------------------------------
    // Select the matching chip for a category string
    // -------------------------------------------------------------------------
    private void selectCategoryChip(String category) {
        int chipId;
        switch (category) {
            case "Social": chipId = R.id.chipSocial; break;
            case "Travel": chipId = R.id.chipTravel; break;
            case "Other":  chipId = R.id.chipOther;  break;
            default:       chipId = R.id.chipWork;   break;
        }
        chipGroupCategory.check(chipId);
    }

    // -------------------------------------------------------------------------
    // Get the selected category string from the ChipGroup
    // -------------------------------------------------------------------------
    private String getSelectedCategory() {
        int checkedId = chipGroupCategory.getCheckedChipId();
        if (checkedId == R.id.chipSocial) return "Social";
        if (checkedId == R.id.chipTravel) return "Travel";
        if (checkedId == R.id.chipOther)  return "Other";
        return "Work";
    }

    // -------------------------------------------------------------------------
    // Show DatePickerDialog then TimePickerDialog
    // -------------------------------------------------------------------------
    private void showDateTimePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                cal.set(year, month, day, hour, minute, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                selectedDateTime = cal.getTimeInMillis();

                String formatted = new SimpleDateFormat(
                        "EEE dd MMM yyyy · hh:mm a", Locale.getDefault())
                        .format(cal.getTime());
                etDateTime.setText(formatted);

                // Clear date error if it was showing
                layoutError.setVisibility(View.GONE);

            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // -------------------------------------------------------------------------
    // Validate inputs then Insert or Update
    // -------------------------------------------------------------------------
    private void validateAndSave() {
        String title    = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";

        // Reset errors
        tilTitle.setError(null);
        layoutError.setVisibility(View.GONE);

        // 1. Title must not be empty
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            return;
        }

        // 2. Date must be selected and not in the past
        if (selectedDateTime == 0) {
            tvError.setText("Please select a date and time");
            layoutError.setVisibility(View.VISIBLE);
            return;
        }
        if (selectedDateTime < System.currentTimeMillis()) {
            tvError.setText("Date cannot be in the past");
            layoutError.setVisibility(View.VISIBLE);
            return;
        }

        String category = getSelectedCategory();

        if (editingEventId == -1) {
            // ADD mode: insert new event
            EventEntity newEvent = new EventEntity(title, category, location, selectedDateTime);
            viewModel.insert(newEvent);
            Snackbar.make(requireView(), "Event saved!", Snackbar.LENGTH_SHORT).show();
        } else {
            // EDIT mode: update existing event preserving its ID
            EventEntity updated = new EventEntity(title, category, location, selectedDateTime);
            updated.id = editingEventId;
            viewModel.update(updated);
            Snackbar.make(requireView(), "Event updated!", Snackbar.LENGTH_SHORT).show();
        }

        // Navigate back to the event list
        NavHostFragment.findNavController(this).popBackStack();
    }
}
