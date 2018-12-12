package com.codetmen.app.boxxmedia.db_app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.codetmen.app.boxxmedia.video_package.MovieUrl;

import java.util.ArrayList;

import static android.provider.BaseColumns._ID;

public class VideoUrlHelper {

    private static String DATABASE_TABLE = DbMediaContract.TABLE_URL_VIDEO;
    private Context context;
    private DbMediaHelper dbMediaHelper;

    private SQLiteDatabase database;

    public VideoUrlHelper(Context context) {
        this.context = context;
    }

    //method opens and reads database
    public VideoUrlHelper open() throws SQLException {
        dbMediaHelper = new DbMediaHelper(context);
        database = dbMediaHelper.getWritableDatabase();
        return this;
    }

    // method close database
    public void close(){
        dbMediaHelper.close();
    }

    // Use this method to capture all existing notes
    // Automatically parsed into Note model
    // @return query result is an array of note models
    public ArrayList<MovieUrl> query(){
        ArrayList<MovieUrl> arrayList = new ArrayList<MovieUrl>();
        Cursor cursor = database.query(DATABASE_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        MovieUrl movieUrl;
        if (cursor.getCount() > 0){
            do {
                movieUrl = new MovieUrl();
                movieUrl.setId(cursor.getInt(cursor.getColumnIndexOrThrow(_ID)));
                movieUrl.setTitleMovie(cursor.getString(cursor.getColumnIndexOrThrow(DbMediaContract.VideoColumns.TITLE_VIDEO)));
                movieUrl.setUrlMovie(cursor.getString(cursor.getColumnIndexOrThrow(DbMediaContract.VideoColumns.URL_VIDEO)));

                arrayList.add(movieUrl);
                cursor.moveToNext();
            } while (!cursor.isAfterLast());
        }
        cursor.close();
        return arrayList;
    }

    // Use this method to query insert
    // @param note model note to be inserted
    // @return id of the data you just entered
    public long insert(MovieUrl movieUrl){
        ContentValues initialValues = new ContentValues();
        initialValues.put(DbMediaContract.VideoColumns.TITLE_VIDEO, movieUrl.getTitleMovie());
        initialValues.put(DbMediaContract.VideoColumns.URL_VIDEO, movieUrl.getUrlMovie());
        return database.insert(DATABASE_TABLE, null, initialValues);
    }

    // Use this method for query updates
    // @param note the note model to be changed
    // @return int the number of updated rows, if nothing is updated then the value is 0
    public int update(MovieUrl movieUrl){
        ContentValues initialValues = new ContentValues();
        initialValues.put(DbMediaContract.VideoColumns.TITLE_VIDEO, movieUrl.getTitleMovie());
        initialValues.put(DbMediaContract.VideoColumns.URL_VIDEO, movieUrl.getUrlMovie());
        return database.update(DATABASE_TABLE, initialValues, _ID + "= '" + movieUrl.getId() + "'", null);
    }

    // Use this method to query delete
    // @param id id to be deleted
    // @return int the number of rows deleted
    public int delete(int id){
        return database.delete(DbMediaContract.TABLE_URL_VIDEO, _ID + " = '"+id+"'", null);
    }
}