# Travel Companion App

A simple and intuitive Android application designed to help travelers with common unit conversions. Whether you're dealing with different currencies, calculating fuel efficiency for a road trip, or checking the weather in a different temperature scale, this app provides quick and accurate results.

## Features

- **Multi-Category Conversion**: Switch easily between three essential travel categories:
    - **💱 Currency**: Convert between USD, AUD, EUR, JPY, and GBP using fixed 2026 rates.
    - **⛽ Fuel & Distance**: Handle common automotive and travel units like mpg, km/L, Gallons, Liters, Nautical Miles, and Kilometers.
    - **🌡 Temperature**: Seamlessly convert between Celsius, Fahrenheit, and Kelvin.
- **Smart Input Validation**:
    - Prevents negative values for distance and fuel measurements.
    - Respects physical limits like Absolute Zero in temperature conversions.
    - Real-time error reporting for invalid or empty inputs.
- **Modern UI/UX**:
    - **Edge-to-Edge Display**: Utilizes the full screen for a modern look.
    - **Visual Feedback**: Category badges highlight the active mode.
    - **Toast Notifications**: Provides helpful tips and warnings (e.g., when converting the same units).

## Supported Conversions

### 💱 Currency (Fixed 2026 Rates)
- US Dollar (USD)
- Australian Dollar (AUD)
- Euro (EUR)
- Japanese Yen (JPY)
- British Pound (GBP)

### ⛽ Fuel & Distance
- **Fuel Efficiency**: mpg (US), km/L
- **Volume**: Gallons (US), Liters
- **Distance**: Nautical Miles, Kilometers

### 🌡 Temperature
- Celsius (°C)
- Fahrenheit (°F)
- Kelvin (K)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Task-2.1P_TravelCompanionApp.git
   ```
2. Open the project in **Android Studio**.
3. Build the project and run it on an emulator or a physical device (Android 8.0+ recommended).

## Technologies Used

- **Language**: Java
- **UI Framework**: Android XML (Material Components)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35

## Usage

1. Select a category from the top spinner (Currency, Fuel, or Temperature).
2. Choose the **Source** unit and **Destination** unit.
3. Enter the value you want to convert in the input field.
4. Tap the **Convert** button to see the result.

---
*Developed as part of Task 2.1P for SIT708.*
