package com.codetmen.app.boxxmedia.db_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.codetmen.app.boxxmedia.audio_package.SongUrl;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;

public class SongUrlHelper {

    private static String DATABASE_TABLE = DbMediaContract.TABLE_URL_SONG;
    private Context context;
    private DbMediaHelper dbMediaHelper;

    private SQLiteDatabase database;

    public SongUrlHelper(Context context) {
        this.context = context;
    }

    // method opens and reads database
    public SongUrlHelper open() throws SQLException {
        dbMediaHelper = new DbMediaHelper(context);
        database = dbMediaHelper.getWritableDatabase();
        return this;
    }

    // method close database
    public void close() {
        dbMediaHelper.close();
    }

    // Use this method to capture all existing notes
    // Automatically parsed into Note model
    // @return query result is an array of note models
    public ArrayList<SongUrl> query(){
        ArrayList<SongUrl> arrayList = new ArrayList<SongUrl>();
        Cursor cursor = database.query(DATABASE_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        SongUrl songUrl;
        if (cursor.getCount() > 0){
            do {
                songUrl = new SongUrl();
                songUrl.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                songUrl.setTitleSong(cursor.getString(cursor.getColumnIndexOrThrow(DbMediaContract.SongColumns.TITLE_SONG)));
                songUrl.setUrlSong(cursor.getString(cursor.getColumnIndexOrThrow(DbMediaContract.SongColumns.URL_SONG)));

                arrayList.add(songUrl);
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();
        return arrayList;
    }

    // Use this method to query insert
    // @param note model note to be inserted
    // @return id of the data you just entered
    public long insert(SongUrl songUrl){
        ContentValues initialValues = new ContentValues();
        initialValues.put(DbMediaContract.SongColumns.TITLE_SONG, songUrl.getTitleSong());
        initialValues.put(DbMediaContract.SongColumns.URL_SONG, songUrl.getUrlSong());
        return database.insert(DATABASE_TABLE, null, initialValues);
    }

    // Use this method for query updates
    // @param note the note model to be changed
    // @return int the number of updated rows, if nothing is updated then the value is 0
    public int update(SongUrl songUrl){
        ContentValues initialValues = new ContentValues();
        initialValues.put(DbMediaContract.SongColumns.TITLE_SONG, songUrl.getTitleSong());
        initialValues.put(DbMediaContract.SongColumns.URL_SONG, songUrl.getUrlSong());
        return database.update(DATABASE_TABLE, initialValues, _ID + "= '" + songUrl.getId() + "'", null);
    }

    // Use this method to query delete
    // @param id id to be deleted
    // @return int the number of rows deleted
    public int delete(int id){
        return database.delete(DbMediaContract.TABLE_URL_SONG, _ID + " = '"+id+"'", null);
    }
}