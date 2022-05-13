package com.hindbyte.velocity.home;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.TelephonyManager;

import java.util.ArrayList;
import java.util.List;

public class HomeData extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_TITLE = "HOME_DATA";
    private static final String TABLE_USER = "item";

    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";

    Context context;

    public HomeData(Context context) {
        super(context, DATABASE_TITLE, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_TITLE + " TEXT," + KEY_URL + " TEXT"+ ")";
        db.execSQL(CREATE_LOGIN_TABLE);
        db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Facebook','https://m.facebook.com/')");
        db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Youtube','https://m.youtube.com/')");
        db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Twitter','https://mobile.twitter.com/')");
        db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Instagram','https://www.instagram.com/')");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        assert tm != null;
        switch (tm.getSimCountryIso()) {
            case "in":
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Amazon','https://www.amazon.in/')");
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Flipkart','https://www.flipkart.com/')");
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Cricbuzz','http://m.cricbuzz.com/')");
                break;
            case "de":
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Amazon','https://www.amazon.de/')");
                break;
            case "gb":
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Amazon','https://www.amazon.co.uk/')");
                break;
            case "it":
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Amazon','https://www.amazon.it/')");
                break;
            default:
                db.execSQL("insert into " + TABLE_USER + "(" + KEY_TITLE + "," + KEY_URL + ") values('Amazon','https://www.amazon.com/')");
                break;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    public void addItem(String title, String url) {
        if (!title.isEmpty() && !url.isEmpty()) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_TITLE, title);
            values.put(KEY_URL, url);
            db.insert(TABLE_USER, null, values);
            db.close();
        }
    }

    public List<HomeModel> getItemDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query(TABLE_USER, new String[]{KEY_ID, KEY_TITLE, KEY_URL}, KEY_ID, null,null,null,KEY_ID + " COLLATE NOCASE ASC");
        List<HomeModel> itemList = new ArrayList<>();
        int idColumnIndex = cursor.getColumnIndex(KEY_ID);
        while (cursor.moveToNext()) {
            HomeModel item = new HomeModel(cursor.getInt(idColumnIndex),cursor.getString(1), cursor.getString(2));
            itemList.add(item);
        }
        db.close();
        return itemList;
    }

    public void updateItem(HomeModel item) {
        if (!item.getTitle().isEmpty() && !item.getUrl().isEmpty()) {
            SQLiteDatabase db = this.getReadableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(KEY_TITLE, item.getTitle());
            contentValues.put(KEY_URL, item.getUrl());
            db.update(TABLE_USER, contentValues, KEY_ID + " = ? ", new String[]{String.valueOf(item.getId())});
            db.close();
        }
     }

    public void deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER,KEY_ID + " = ? ", new String[]{String.valueOf(id)});
        db.close();
    }
}