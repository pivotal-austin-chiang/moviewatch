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
import java.sql.SQLException;
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

public class CustomAdapter extends ArrayAdapter{
    private ArrayList<Movie> entries;
    private Activity activity;
    private CustomAdapter currentAdapter;

    private LayoutInflater inflater;

    private int layout;

    private MovieDataSource dataSource;
    private String thumb_grey= "@drawable/ic_action_good";
    private String thumb_blue= "@drawable/ic_action_good_blue";
    private int grey;
    private int blue;
    public CustomAdapter(Activity a, int textViewResourceId, ArrayList<Movie> entries, MovieDataSource dataSource) {
        super(a, textViewResourceId, entries);
        layout = textViewResourceId;
        this.entries = entries;
        this.activity = a;
        currentAdapter = this;
        inflater = LayoutInflater.from(a);
        this.dataSource = dataSource;
        grey = a.getResources().getIdentifier(thumb_grey, null, a.getPackageName());
        blue = a.getResources().getIdentifier(thumb_blue, null, a.getPackageName());
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        TextView score;
        ImageView thumbnail;
        ImageView watch;
        Movie movie = entries.get(position);
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = inflater.inflate(layout, parent, false);
        }

        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
        watch = (ImageView) v.findViewById(R.id.watch);


        try {

            if (dataSource.verification(movie.getMovieId())) {
                watch.setImageResource(blue);
                watch.setEnabled(false);
            }
            else {
                watch.setImageResource(grey);
                watch.setEnabled(true);
            }

            title.setText(movie.getMovieTitle());
            score.setText("Critics: " + movie.getMovieCritics() + "% | " + "Audience: " + movie.getMovieAudience() + "% | " + "Rated: " + movie.getMpaa());
            new LoadImageTask().execute(movie.getImageUrl(), thumbnail);

        } catch (SQLException e) {
            Log.d("SQL Error", "Checking if movie is in database failed");
        }

        final int tempPosition = position;

        watch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Movie returnValue = dataSource.createMovie(entries.get(tempPosition));
                if (returnValue != null) {
                    view.setEnabled(false);
                    ImageView v = (ImageView) view;
                    v.setImageResource(blue);
                }
            }
        });

//        Object content = null;
//        try{
//            URL url = new URL(this.entries.get(position).getJSONObject("posters").getString("thumbnail"));
//            content = url.getContent();
//        }
//        catch(Exception ex)
//        {
//            Log.d("IMAGE FETCH", "EXCEPTION FETCHING IMAGE ");
//            ex.printStackTrace();
//        }
//        InputStream is = (InputStream)content;
//        Drawable image = Drawable.createFromStream(is, "src");
//        thumbnail.setImageDrawable(image);

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