package com.hindbyte.velocity.history;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryData extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_TITLE = "HISTORY_DATA";
    private final String TABLE_USER = "item";

    private final String KEY_ID = "id";
    private final String KEY_TITLE = "title";
    private final String KEY_LINK = "link";
    private final String KEY_DATE = "date";
   
    public HistoryData(Context context) {
        super(context, DATABASE_TITLE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_LINK + " TEXT,"
                + KEY_DATE + " TEXT"+ ")";
        db.execSQL(CREATE_LOGIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void addItem(String title, String count) {
        if (!title.isEmpty()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, title);
            values.put(KEY_LINK, count);
            long time = System.currentTimeMillis();
            values.put(KEY_DATE, time);
            db.insert(TABLE_USER, null, values);
            db.close();
        }
    }

    public List<HistoryModel> getItemDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_USER, new String[]{KEY_ID, KEY_TITLE, KEY_LINK, KEY_DATE}, KEY_ID, null, null, null, KEY_ID + " COLLATE NOCASE DESC");
        List<HistoryModel> itemList = new ArrayList<>();
        String dateString = "";
        while (cursor.moveToNext()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            Date date = new Date(Long.parseLong(cursor.getString(3)));
            if (!dateString.equals(sdf.format(date))) {
                dateString = sdf.format(date);
                HistoryModel item = new HistoryModel("", "", dateString, 2);
                itemList.add(item);
            }
            HistoryModel item = new HistoryModel(cursor.getString(1), cursor.getString(2), dateString, 1);
            itemList.add(item);
        }
        db.close();
        return itemList;
    }

    public void deleteItem(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_USER, KEY_ID + " = ? ", new String[]{id});
        db.close();
    }

    public void deleteData() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_USER, null, null);
        db.close();
    }
}