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
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

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
    private ArrayList<JSONObject> entries;
    private Activity activity;
    private CustomAdapter currentAdapter;

    private LayoutInflater inflater;

    private int layout;

    public CustomAdapter(Activity a, int textViewResourceId, ArrayList<JSONObject> entries) {
        super(a, textViewResourceId, entries);
        layout = textViewResourceId;
        this.entries = entries;
        this.activity = a;
        currentAdapter = this;
        inflater = LayoutInflater.from(a);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView title;
        TextView score;
        ImageView thumbnail;
        View v = null;
        if (convertView != null) {
            v = convertView;
        } else {
            v = inflater.inflate(layout, parent, false);
        }

        title = (TextView) v.findViewById(R.id.title);
        score = (TextView) v.findViewById(R.id.score);
        thumbnail = (ImageView) v.findViewById(R.id.thumbnail);
        try {
            title.setText(this.entries.get(position).getString("title"));
            score.setText("Critics: " + this.entries.get(position).getJSONObject("ratings").getString("critics_score") + "% | " + "Audience: " + this.entries.get(position).getJSONObject("ratings").getString("audience_score") + "% | " + "Rated: " + this.entries.get(position).getString("mpaa_rating"));
            new LoadImageTask().execute(this.entries.get(position).getJSONObject("posters").getString("thumbnail"), thumbnail);

        } catch (JSONException e) {
            Log.d("Test", "Failed to parse the JSON response!");
        }

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