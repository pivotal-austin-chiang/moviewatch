package com.example.moviewatch;

/**
 * Created by dx165-xl on 2014-01-06.
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WatchlistAdapter extends ArrayAdapter{
    private List<Movie> entries;
    private Activity activity;
    private WatchlistAdapter currentAdapter;

    private LayoutInflater inflater;

    private int layout;

    private List<Movie> movieModelList = new ArrayList<Movie>();

    private MovieDataSource dataSource;

    public WatchlistAdapter(Activity a, int textViewResourceId, List<Movie> entries, MovieDataSource dataSource) {
        super(a, textViewResourceId, entries);
        layout = textViewResourceId;
        this.entries = entries;
        this.activity = a;
        currentAdapter = this;
        inflater = LayoutInflater.from(a);
        this.dataSource = dataSource;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        TextView score;
        ImageView thumbnail;
        Button watch;
        Movie movie = this.entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = inflater.inflate(layout, parent, false);
        }

        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        thumbnail = (ImageView) v.findViewById(R.id.thumbnail);




        title.setText(movie.getMovieTitle());
        score.setText("Critics: " + movie.getMovieCritics() + "% | " + "Audience: " + movie.getMovieAudience() + "% | " + "Rated: " + movie.getMpaa());
        new LoadImageTask().execute(movie.getImageUrl(), thumbnail);

        return v;
    }

    public static Bitmap loadBitmap(String url) {
        try{
            URL newurl = new URL(url);
            Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
            return mIcon_val;
        }
        catch(Exception e) {
            Log.d("Bitmap", "Bitmap failed to load");
            return null;

        }
    }

    class LoadImageTask extends AsyncTask<Object, String, String> {

        private ImageView thumbnailTemp;
        private Bitmap thumbnailBitmap;

        @Override
        protected String doInBackground(Object... uri)
        {
            thumbnailTemp = (ImageView) uri[1];
            thumbnailBitmap = loadBitmap((String) uri[0]);
            return "";
        }
        @Override
        protected void onPostExecute(String response) {
            if (thumbnailBitmap != null) {
                thumbnailTemp.setImageBitmap(thumbnailBitmap);
            }
        }

    }

}