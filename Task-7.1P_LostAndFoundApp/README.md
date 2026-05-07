# Lost & Found App

An Android application that helps community members report and discover lost or found items. Users can post adverts, browse listings, and contact item owners directly through the app.

---

## Features

- **Post adverts** — Report a lost or found item with a photo, category, location, and contact details
- **Browse listings** — Scroll through all posted items in a card-based list
- **Search** — Filter items by title, description, or location in real time
- **Category filter** — Narrow results by item category (Electronics, Pets, Keys, etc.)
- **Lost / Found badge** — Each card is colour-coded to distinguish lost items (red) from found items (green)
- **Item details** — View full information for any listing and remove it when resolved

---

## Screens

| Screen | Description |
|---|---|
| Home | Landing page with buttons to create an advert or view all items |
| Lost & Found Items | Searchable, filterable list of all postings |
| Item Details | Full view of a single item with contact info and remove option |
| Create Advert | Form to submit a new lost or found item |

---

## Tech Stack

| Component | Detail |
|---|---|
| Language | Java |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |
| UI | XML layouts, Material Components (Chips, Cards) |
| Database | SQLite via `SQLiteOpenHelper` |
| Image storage | BLOB in SQLite |

---

## Project Structure

```
app/src/main/java/com/example/lostandfound/
├── MainActivity.java          # Home screen
├── ItemListActivity.java      # Browse & filter listings
├── ItemDetailsActivity.java   # Full item view
├── CreateAdvertActivity.java  # Post a new advert
├── ItemAdapter.java           # RecyclerView adapter
├── LostFoundItem.java         # Data model
├── DatabaseHelper.java        # SQLite CRUD operations
└── ImageHelper.java           # Byte array ↔ Bitmap conversion

app/src/main/res/
├── layout/
│   ├── activity_main.xml
│   ├── activity_item_list.xml
│   ├── activity_item_details.xml
│   ├── activity_create_advert.xml
│   └── item_lost.xml          # RecyclerView card layout
└── drawable/
    ├── bg_badge_lost.xml
    └── bg_badge_found.xml
```

---

## Getting Started

1. Clone the repository and open the `Task-7.1P_LostAndFoundApp` folder in Android Studio.
2. Let Gradle sync finish.
3. Run on an emulator or physical device (API 24+).

No external API keys or configuration files are required — the app runs fully offline using a local SQLite database.

---
*Developed as part of Task 7.1P for SIT708.*