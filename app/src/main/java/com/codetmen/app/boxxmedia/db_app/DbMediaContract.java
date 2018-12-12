package com.codetmen.app.boxxmedia.db_app;

import android.provider.BaseColumns;

public class DbMediaContract {

    public static String TABLE_URL_SONG = "table_song";
    public static String TABLE_URL_VIDEO = "table_video";

    public static final class SongColumns implements BaseColumns {
        public static String TITLE_SONG = "title_song";
        public static String URL_SONG = "url_song";
    }

    public static final class VideoColumns implements BaseColumns {
        public static String TITLE_VIDEO = "title_video";
        public static String URL_VIDEO = "url_song";
    }
}

