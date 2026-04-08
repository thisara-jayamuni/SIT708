package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Spinner  spinnerCategory, spinnerSource, spinnerDestination;
    private EditText etInputValue;
    private Button   btnConvert;
    private TextView tvResult;
    private TextView badgeCurrency, badgeFuel, badgeTemp;

    // Categories
    private final List<String> categories = Arrays.asList(
            "💱  Currency",
            "⛽  Fuel & Distance",
            "🌡  Temperature"
    );

    // Units per category
    private final List<String> currencyUnits     = Arrays.asList("USD", "AUD", "EUR", "JPY", "GBP");
    private final List<String> fuelDistanceUnits = Arrays.asList("mpg", "km/L", "Gallon (US)", "Liter", "Nautical Mile", "Kilometer");
    private final List<String> temperatureUnits  = Arrays.asList("Celsius", "Fahrenheit", "Kelvin");

    private final List<String> positiveOnlyUnits = Arrays.asList(
            "mpg", "km/L", "Gallon (US)", "Liter", "Nautical Mile", "Kilometer"
    );

    // Currency rates — Fixed 2026
    private final Map<String, Double> toUSD = new HashMap<>() {{
        put("USD", 1.0);
        put("AUD", 1.0 / 1.55);
        put("EUR", 1.0 / 0.92);
        put("JPY", 1.0 / 148.50);
        put("GBP", 1.0 / 0.78);
    }};

    private final Map<String, Double> fromUSD = new HashMap<>() {{
        put("USD", 1.0);
        put("AUD", 1.55);
        put("EUR", 0.92);
        put("JPY", 148.50);
        put("GBP", 0.78);
    }};

    // Distance rates
    private final Map<String, Double> toKm = new HashMap<>() {{
        put("Kilometer",     1.0);
        put("Nautical Mile", 1.852);
    }};

    private final Map<String, Double> fromKm = new HashMap<>() {{
        put("Kilometer",     1.0);
        put("Nautical Mile", 1.0 / 1.852);
    }};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind views
        spinnerCategory    = findViewById(R.id.spinnerCategory);
        spinnerSource      = findViewById(R.id.sourceCategory);
        spinnerDestination = findViewById(R.id.destinationCategory);
        etInputValue       = findViewById(R.id.etInputValue);
        btnConvert         = findViewById(R.id.btnConvert);
        tvResult           = findViewById(R.id.tvResult);
        badgeCurrency      = findViewById(R.id.badgeCurrency);
        badgeFuel          = findViewById(R.id.badgeFuel);
        badgeTemp          = findViewById(R.id.badgeTemp);

        // Category spinner
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                switch (pos) {
                    case 0:
                        loadSpinners(currencyUnits);
                        highlightBadge(0);
                        break;
                    case 1:
                        loadSpinners(fuelDistanceUnits);
                        highlightBadge(1);
                        break;
                    case 2:
                        loadSpinners(temperatureUnits);
                        highlightBadge(2);
                        break;
                }
                resetResult();
                etInputValue.setError(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Convert button
        btnConvert.setOnClickListener(v -> handleConversion());

        // Default load
        loadSpinners(currencyUnits);
        highlightBadge(0);
    }

    // ── Badge highlight ───────────────────────────────────────────────────────

    private void highlightBadge(int index) {
        resetBadge(badgeCurrency);
        resetBadge(badgeFuel);
        resetBadge(badgeTemp);

        switch (index) {
            case 0: activateBadge(badgeCurrency); break;
            case 1: activateBadge(badgeFuel);     break;
            case 2: activateBadge(badgeTemp);     break;
        }
    }

    private void resetBadge(TextView badge) {
        badge.setBackgroundResource(R.drawable.bg_badge);
        badge.setTextColor(getColor(R.color.text_secondary));
        badge.setTypeface(null, Typeface.NORMAL);
    }

    private void activateBadge(TextView badge) {
        badge.setBackgroundResource(R.drawable.bg_badge_active);
        badge.setTextColor(getColor(R.color.white));
        badge.setTypeface(null, Typeface.BOLD);
    }

    // ── Spinners ──────────────────────────────────────────────────────────────

    private void loadSpinners(List<String> units) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, units);
        spinnerSource.setAdapter(adapter);
        spinnerDestination.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, units));
    }

    // ── Result reset ──────────────────────────────────────────────────────────

    private void resetResult() {
        tvResult.setText(R.string.result_placeholder);
    }

    // ── Conversion handler ────────────────────────────────────────────────────

    @SuppressLint("SetTextI18n")
    private void handleConversion() {

        String from     = spinnerSource.getSelectedItem().toString();
        String to       = spinnerDestination.getSelectedItem().toString();
        String inputStr = etInputValue.getText().toString().trim();

        // 1. Empty input
        if (inputStr.isEmpty()) {
            etInputValue.setError("Please enter a value");
            etInputValue.requestFocus();
            showToast("⚠️ Input cannot be empty");
            tvResult.setText("—");
            return;
        }

        // 2. Non-numeric input
        double inputValue;
        try {
            inputValue = Double.parseDouble(inputStr);
        } catch (NumberFormatException e) {
            etInputValue.setError("Must be a valid number");
            etInputValue.requestFocus();
            showToast("⚠️ Invalid number — please enter digits only");
            tvResult.setText("—");
            return;
        }

        // 3. NaN / Infinite
        if (Double.isNaN(inputValue) || Double.isInfinite(inputValue)) {
            etInputValue.setError("Value is out of range");
            showToast("⚠️ Value is out of range");
            tvResult.setText("—");
            return;
        }

        // 4. Negative check for Fuel & Distance
        if (positiveOnlyUnits.contains(from) && inputValue < 0) {
            etInputValue.setError("Fuel & Distance values cannot be negative");
            etInputValue.requestFocus();
            showToast("⚠️ " + from + " cannot be negative");
            tvResult.setText("—");
            return;
        }

        // 5. Zero fuel efficiency
        if ((from.equals("mpg") || from.equals("km/L")) && inputValue == 0) {
            etInputValue.setError("Fuel efficiency cannot be zero");
            etInputValue.requestFocus();
            showToast("⚠️ Fuel efficiency cannot be zero");
            tvResult.setText("—");
            return;
        }

        // 6. Kelvin below absolute zero
        if (from.equals("Kelvin") && inputValue < 0) {
            etInputValue.setError("Kelvin cannot be below 0 (absolute zero)");
            etInputValue.requestFocus();
            showToast("⚠️ Kelvin cannot be negative — absolute zero is 0 K");
            tvResult.setText("—");
            return;
        }

        // 7. Celsius below absolute zero
        if (from.equals("Celsius") && inputValue < -273.15) {
            etInputValue.setError("Below absolute zero (−273.15 °C)");
            etInputValue.requestFocus();
            showToast("⚠️ Celsius cannot be below −273.15 (absolute zero)");
            tvResult.setText("—");
            return;
        }

        // 8. Fahrenheit below absolute zero
        if (from.equals("Fahrenheit") && inputValue < -459.67) {
            etInputValue.setError("Below absolute zero (−459.67 °F)");
            etInputValue.requestFocus();
            showToast("⚠️ Fahrenheit cannot be below −459.67 (absolute zero)");
            tvResult.setText("—");
            return;
        }

        // 9. Identity conversion — same unit selected
        if (from.equals(to)) {
            showToast("ℹ️ Source and destination are the same unit");
            tvResult.setText(formatResult(inputValue) + " " + to);
            etInputValue.setError(null);
            return;
        }

        // 10. Clear errors and convert
        etInputValue.setError(null);
        Double result = convert(inputValue, from, to);

        if (result == null) {
            showToast("⚠️ This conversion is not supported");
            tvResult.setText("—");
            return;
        }

        // 11. Result overflow guard
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            showToast("⚠️ Result is out of range");
            tvResult.setText("—");
            return;
        }

        // ✅ All good — show result only in tvResult
        tvResult.setText(formatResult(result) + " " + to);
    }

    // ── Conversion logic ──────────────────────────────────────────────────────

    private Double convert(double value, String from, String to) {

        // Currency — via USD base
        if (toUSD.containsKey(from) && fromUSD.containsKey(to)) {
            double inUSD = value * toUSD.get(from);
            return inUSD * fromUSD.get(to);
        }

        // Fuel efficiency — 1 mpg = 0.425 km/L
        if (from.equals("mpg")  && to.equals("km/L")) return value * 0.425;
        if (from.equals("km/L") && to.equals("mpg"))  return value / 0.425;

        // Volume — 1 Gallon (US) = 3.785 Liters
        if (from.equals("Gallon (US)") && to.equals("Liter"))       return value * 3.785;
        if (from.equals("Liter")       && to.equals("Gallon (US)")) return value / 3.785;

        // Distance — 1 Nautical Mile = 1.852 Kilometers
        if (toKm.containsKey(from) && fromKm.containsKey(to)) {
            return value * toKm.get(from) * fromKm.get(to);
        }

        // Temperature
        if (from.equals("Celsius")    && to.equals("Fahrenheit")) return (value * 1.8) + 32;
        if (from.equals("Fahrenheit") && to.equals("Celsius"))    return (value - 32) / 1.8;
        if (from.equals("Celsius")    && to.equals("Kelvin"))     return value + 273.15;
        if (from.equals("Kelvin")     && to.equals("Celsius"))    return value - 273.15;
        if (from.equals("Fahrenheit") && to.equals("Kelvin"))     return ((value - 32) / 1.8) + 273.15;
        if (from.equals("Kelvin")     && to.equals("Fahrenheit")) return ((value - 273.15) * 1.8) + 32;

        return null;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ── Format result ─────────────────────────────────────────────────────────

    @SuppressLint("DefaultLocale")
    private String formatResult(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.4f", value)
                .replaceAll("0*$", "")
                .replaceAll("\\.$", "");
    }
}