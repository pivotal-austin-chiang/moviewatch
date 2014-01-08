package com.example.moviewatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dx165-xl on 2014-01-08.
 */
public class MovieDataSource {

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_MOVIE_ID, MySQLiteHelper.COLUMN_MOVIE_TITLE, MySQLiteHelper.COLUMN_CRITICS, MySQLiteHelper.COLUMN_AUDIENCE, MySQLiteHelper.COLUMN_MPAA, MySQLiteHelper.COLUMN_IMAGE_URL };

    public MovieDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createTable() {
        database.execSQL(MySQLiteHelper.DATABASE_CREATE);
    }

    public void deleteTable() {
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_WATCHLIST);
    }

    public Movie createMovie(Movie movie) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MOVIE_ID, movie.getMovieId());
        values.put(MySQLiteHelper.COLUMN_MOVIE_TITLE, movie.getMovieTitle());
        values.put(MySQLiteHelper.COLUMN_CRITICS, movie.getMovieCritics());
        values.put(MySQLiteHelper.COLUMN_AUDIENCE, movie.getMovieAudience());
        values.put(MySQLiteHelper.COLUMN_MPAA, movie.getMpaa());
        values.put(MySQLiteHelper.COLUMN_IMAGE_URL, movie.getImageUrl());
        long insertId = database.insert(MySQLiteHelper.TABLE_WATCHLIST, null,
                values);

        if (insertId != -1) {
            Cursor cursor = database.query(MySQLiteHelper.TABLE_WATCHLIST,
                    allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                    null, null, null);

            cursor.moveToFirst();
            Movie newMovie = cursorToMovie(cursor);
            cursor.close();
            return newMovie;
        } else {
            return null;
        }


    }

    public void deleteMovie(Movie movie) {
        long movieId = movie.getMovieId();
        database.delete(MySQLiteHelper.TABLE_WATCHLIST, MySQLiteHelper.COLUMN_MOVIE_ID
                + " = " + movieId, null);
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<Movie>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_WATCHLIST,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Movie movie = cursorToMovie(cursor);
            movies.add(movie);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return movies;
    }

    public boolean verification(int id) throws SQLException {
        int count = -1;
        Cursor c = null;
        String query = "SELECT COUNT(*) FROM "
                + MySQLiteHelper.TABLE_WATCHLIST + " WHERE movieid = " + id;
        c = database.rawQuery(query, null);
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count > 0;
    }

    public void logColumnNames () {
        Cursor ti = database.rawQuery("PRAGMA table_info("+MySQLiteHelper.TABLE_WATCHLIST+")", null);
        if ( ti.moveToFirst() ) {
            do {
                Log.d("DATABASE TABLE NAME", "col: " + ti.getString(1));
            } while (ti.moveToNext());
        }

        ti.close();
    }

    private Movie cursorToMovie(Cursor cursor) {
        Movie movie = new Movie();
        movie.setId(cursor.getLong(0));
        movie.setMovieId(cursor.getInt(1));
        movie.setMovieTitle(cursor.getString(2));
        movie.setMovieCritics(cursor.getInt(3));
        movie.setMovieAudience(cursor.getInt(4));
        movie.setMpaa(cursor.getString(5));
        movie.setImageUrl(cursor.getString(6));
        return movie;
    }
}
