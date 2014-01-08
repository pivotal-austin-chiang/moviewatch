package com.example.moviewatch;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchList extends ActionBarActivity {

    private MovieDataSource dataSource;
    private List<Movie> moviesList;
    private EnhancedListView moviesListView;
    WatchlistAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchlist_main);


        try {
            dataSource = new MovieDataSource(this);
            dataSource.open();
        } catch (SQLException e) {
            Log.d("OPENING DATABASE EXCEPTION", "");
            e.printStackTrace();
        }

        moviesListView = (EnhancedListView) findViewById(R.id.list_movies);


        Log.d("DATABASE TABLE SIZE", dataSource.getAllMovies().size() + "");
        moviesList = dataSource.getAllMovies();

        adapter = new WatchlistAdapter(this, R.layout.watchlist_layout, moviesList, dataSource);
        moviesListView.setAdapter(adapter);
        moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Log.d("CELLPRESS", "MOVIE IS PRESSED");
                try {
                    Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                    Log.d ("TEST ID", moviesList.get(position).getMovieId() + "");
                    intent.putExtra("MOVIE_ID", Integer.toString(moviesList.get(position).getMovieId()));
                    startActivity(intent);
                } catch (Exception e){
                    Log.d("Error", "Incorrect JSON format");
                }

//                Toast.makeText(getApplicationContext(), synopsis,Toast.LENGTH_SHORT).show();
            }
        });
        moviesListView.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {


                dataSource.deleteMovie((Movie) moviesList.get(position));
                moviesList.remove(position);

                adapter.notifyDataSetChanged();

                return null;
            }
        });

        moviesListView.enableSwipeToDismiss();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.watch_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
