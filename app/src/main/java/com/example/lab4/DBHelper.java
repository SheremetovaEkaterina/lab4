package com.example.lab4;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "songsDB";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE songs (" +
                "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "TrackTitle TEXT" +
                ");");
        insertInitialData(db);
    }
    private void insertInitialData(SQLiteDatabase db) {
        // Вставляем 5 записей в обновленную таблицу
        ContentValues values = new ContentValues();
        values.put("TrackTitle", "Трек");
        db.insert("songs", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS songs");
        onCreate(db);
        insertInitialData(db);
    }

    public void addSong(String trackTitle) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TrackTitle", trackTitle);
        db.insert("songs", null, values);
    }



    // public Cursor getAllSongs() {
    //    SQLiteDatabase db = this.getReadableDatabase();
    //    return db.rawQuery("SELECT * FROM songs ORDER BY ID DESC", null);
    //}
}

