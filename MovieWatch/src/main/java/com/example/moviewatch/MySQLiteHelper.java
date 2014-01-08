package com.example.moviewatch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by dx165-xl on 2014-01-08.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_WATCHLIST = "watchlist";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_MOVIE_ID = "movieid";
    public static final String COLUMN_MOVIE_TITLE = "movietitle";
    public static final String COLUMN_CRITICS = "moviecritics";
    public static final String COLUMN_AUDIENCE = "movieaudience";
    public static final String COLUMN_MPAA = "moviempaa";
    public static final String COLUMN_IMAGE_URL = "movieimage";
    private static final String DATABASE_NAME = "watchlist.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    public static final String DATABASE_CREATE = "create table "
            + TABLE_WATCHLIST + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MOVIE_ID + " text not null, "
            + COLUMN_MOVIE_TITLE + " text not null, "
            + COLUMN_CRITICS + " integer not null, "
            + COLUMN_AUDIENCE + " integer not null, "
            + COLUMN_MPAA + " text not null,"
            + COLUMN_IMAGE_URL + " text not null);";
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATCHLIST);
        onCreate(db);
    }
}
