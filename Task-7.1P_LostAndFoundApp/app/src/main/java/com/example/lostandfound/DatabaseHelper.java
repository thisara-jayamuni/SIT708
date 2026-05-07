package com.example.lostandfound;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "lost_found.db";
    private static final int    DB_VERSION = 1;

    public static final String TABLE_ITEMS   = "items";
    public static final String COL_ID        = "_id";
    public static final String COL_POST_TYPE = "post_type";
    public static final String COL_CATEGORY  = "category";
    public static final String COL_TITLE     = "title";
    public static final String COL_DESC      = "description";
    public static final String COL_LOCATION  = "location";
    public static final String COL_NAME      = "name";
    public static final String COL_PHONE     = "phone";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_IMAGE     = "image";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_POST_TYPE + " TEXT, " +
                COL_CATEGORY  + " TEXT, " +
                COL_TITLE     + " TEXT, " +
                COL_DESC      + " TEXT, " +
                COL_LOCATION  + " TEXT, " +
                COL_NAME      + " TEXT, " +
                COL_PHONE     + " TEXT, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_IMAGE     + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    public long insertItem(String postType, String category, String title,
                           String description, String location, String name,
                           String phone, String timestamp, byte[] image) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TYPE, postType);
        values.put(COL_CATEGORY,  category);
        values.put(COL_TITLE,     title);
        values.put(COL_DESC,      description);
        values.put(COL_LOCATION,  location);
        values.put(COL_NAME,      name);
        values.put(COL_PHONE,     phone);
        values.put(COL_TIMESTAMP, timestamp);
        values.put(COL_IMAGE,     image);
        long id = db.insert(TABLE_ITEMS, null, values);
        db.close();
        return id;
    }

    public List<LostFoundItem> getAllItems() {
        List<LostFoundItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ITEMS, null, null, null, null, null,
                COL_ID + " DESC");
        while (cursor.moveToNext()) {
            items.add(new LostFoundItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DESC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_POST_TYPE)),
                    cursor.getBlob(cursor.getColumnIndexOrThrow(COL_IMAGE))
            ));
        }
        cursor.close();
        db.close();
        return items;
    }

    public void deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ITEMS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}