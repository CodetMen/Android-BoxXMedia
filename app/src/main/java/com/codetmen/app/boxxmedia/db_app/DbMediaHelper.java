package com.codetmen.app.boxxmedia.db_app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbMediaHelper extends SQLiteOpenHelper {

    private static String DATABASE_MEDIA = "dbMedia";
    private static final int DATABASE_VERSION = 1;

    // initial creates table for url song
    public static final String CREATE_TABLE_URL_SONG = String.format("CREATE TABLE %s"
                    + " (%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL)",
            DbMediaContract.TABLE_URL_SONG,
            DbMediaContract.SongColumns._ID,
            DbMediaContract.SongColumns.TITLE_SONG,
            DbMediaContract.SongColumns.URL_SONG
    );

    // initial creates table for url video
    public static final String CREATE_TABLE_URL_VIDEO = String.format("CREATE TABLE %s"
                    + " (%s INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " %s TEXT NOT NULL," +
                    " %s TEXT NOT NULL)",
            DbMediaContract.TABLE_URL_VIDEO,
            DbMediaContract.VideoColumns._ID,
            DbMediaContract.VideoColumns.TITLE_VIDEO,
            DbMediaContract.VideoColumns.URL_VIDEO
    );

    public DbMediaHelper(Context context) {
        super(context, DATABASE_MEDIA, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_URL_SONG);
        db.execSQL(CREATE_TABLE_URL_VIDEO);
    }

    // method onUpgrade will work if db have different version or upgrade, this usefull for migration data
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DbMediaContract.TABLE_URL_SONG);
        db.execSQL("DROP TABLE IF EXISTS " + DbMediaContract.TABLE_URL_VIDEO);
        onCreate(db);
    }

}
