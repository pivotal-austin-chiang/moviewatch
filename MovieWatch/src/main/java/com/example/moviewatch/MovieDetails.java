package com.example.moviewatch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.io.IOException;

import javax.xml.datatype.Duration;

public class MovieDetails extends ActionBarActivity {

    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    // the number of movies you want to get in a single request to their web server
    private static final int MOVIE_PAGE_LIMIT = 10;


    private RequestTask requestTask = new RequestTask();
    private TextView title;
    private TextView mpaa;
    private TextView runtime;
    private TextView c_rating;
    private TextView a_rating;
    private TextView consensus;
    private TextView sypnosis;
    private TextView movie_cast;
    private ImageView poster;
    private ImageView c_icon;
    private ImageView a_icon;
    private String rotten= "@drawable/rotten";
    private String fresh= "@drawable/fresh";
    private String certified= "@drawable/certified";
    private String dislike= "@drawable/dislike";
    private String like= "@drawable/like";
    private int rotten_id;
    private int fresh_id;
    private int certified_id;
    private int dislike_id;
    private int like_id;

    String castList = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        title = (TextView) findViewById(R.id.title);
        mpaa = (TextView) findViewById(R.id.mpaa);
        runtime = (TextView) findViewById(R.id.runtime);
        c_rating = (TextView) findViewById(R.id.c_rating);
        a_rating = (TextView) findViewById(R.id.a_rating);
        consensus = (TextView) findViewById(R.id.consensus);
        sypnosis = (TextView) findViewById(R.id.sypnosis);
        movie_cast = (TextView) findViewById(R.id.cast);
        poster = (ImageView) findViewById(R.id.thumbnail);
        c_icon = (ImageView) findViewById(R.id.c_icon);
        a_icon = (ImageView) findViewById(R.id.a_icon);

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("MOVIE_ID");

        rotten_id = this.getResources().getIdentifier(rotten, null, this.getPackageName());
        fresh_id = this.getResources().getIdentifier(fresh, null, this.getPackageName());
        certified_id = this.getResources().getIdentifier(certified, null, this.getPackageName());
        like_id = this.getResources().getIdentifier(like, null, this.getPackageName());
        dislike_id = this.getResources().getIdentifier(dislike, null, this.getPackageName());

        requestTask.execute("http://api.rottentomatoes.com/api/public/v1.0/movies/" + id + ".json?apikey=vxwjzfe4gaczt2qpurr33cyj");


    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
        JSONObject movieObject;
        // make a request to the specified url
        @Override
        protected String doInBackground(String... uri)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;

            boolean isGzip = false;

            try
            {
                // make a HTTP request
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (response.getFirstHeader("Content-Encoding") != null) {
                    isGzip = true;
                    Log.d("GZIP", "Stream is gzipped");
                }
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    // request successful - read the response and close the connection
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    if (isGzip) {
                        responseString = decompress(out.toByteArray());
                    } else {
                        responseString = out.toString();
                    }
                }
                else
                {
                    // request failed - close the connection
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch (Exception e)
            {
                Log.d("Test", "Couldn't make a successful request! Exception is: " + e.getMessage() + e.getClass().toString());
            }
            return responseString;
        }

        // if the request above completed successfully, this method will
        // automatically run so you can do something with the response
        @Override
        protected void onPostExecute(String response)
        {
            super.onPostExecute(response);

            if (response != null)
            {
                try
                {
                    // convert the String response to a JSON object,
                    // because JSON is the response format Rotten Tomatoes uses
                    movieObject = new JSONObject(response);
                    title.setText(requestTask.movieObject.getString("title"));
                    mpaa.setText(Html.fromHtml("<b>Rated: </b>"+ requestTask.movieObject.getString("mpaa_rating")));
                    runtime.setText(Html.fromHtml("<b>Duration: </b>"+ requestTask.movieObject.getString("runtime")));
                    c_rating.setText(Html.fromHtml("<b>Critics: </b>"+ requestTask.movieObject.getJSONObject("ratings").getString("critics_score")+"%"));
                    a_rating.setText(Html.fromHtml("<b>Audience: </b>"+ requestTask.movieObject.getJSONObject("ratings").getString("audience_score")+"%"));
                    consensus.setText(Html.fromHtml("<b>Description: </b>"+ requestTask.movieObject.getString("critics_consensus")));
                    sypnosis.setText(Html.fromHtml("<b>Synopsis: </b>"+ requestTask.movieObject.getString("synopsis")));
                    String critics_rating=requestTask.movieObject.getJSONObject("ratings").getString("critics_rating");
                    String audience_rating=requestTask.movieObject.getJSONObject("ratings").getString("audience_rating");
                    if(critics_rating.equals("Rotten")){
                        c_icon.setImageResource(rotten_id);
                    } else if(critics_rating.equals("Fresh")){
                        c_icon.setImageResource(fresh_id);
                    } else{
                        c_icon.setImageResource(certified_id);
                    }

                    if(audience_rating.equals("Spilled")){
                        a_icon.setImageResource(dislike_id);
                    } else {
                        a_icon.setImageResource(like_id);
                    }

                    JSONArray cast = requestTask.movieObject.getJSONArray("abridged_cast");
                    if(cast != null){
                        castList += cast.getJSONObject(0).getString("name");
                        for(int i=1;i<cast.length();i++){
                            castList+=", ";
                            castList+=cast.getJSONObject(i).getString("name");
                        }
                    }

                    movie_cast.setText(Html.fromHtml("<b>Cast: </b> " + castList));
                    new LoadImageTask().execute(movieObject.getJSONObject("posters").getString("detailed"));



                }
                catch (JSONException e)
                {
                    Log.d("Test", "Failed to parse the JSON response!");
                }
            }
        }

        public String decompress(byte[] compressed) throws IOException {
            final int BUFFER_SIZE = 32;
            ByteArrayInputStream is = new ByteArrayInputStream(compressed);
            GZIPInputStream gis = new GZIPInputStream(is, BUFFER_SIZE);
            StringBuilder string = new StringBuilder();
            byte[] data = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = gis.read(data)) != -1) {
                string.append(new String(data, 0, bytesRead));
            }
            gis.close();
            is.close();
            return string.toString();
        }

    }

    public static Bitmap loadBitmap(String url) {
        try{
            URL newurl = new URL(url);
            Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
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
        protected String doInBackground(Object... objects)
        {
            thumbnailTemp = poster;
            thumbnailBitmap = loadBitmap((String) objects[0]);
            return "";
        }
        @Override
        protected void onPostExecute(String response) {
            if (thumbnailBitmap != null) {
                thumbnailTemp.setImageBitmap(thumbnailBitmap);
                title.setVisibility(View.VISIBLE);
                mpaa.setVisibility(View.VISIBLE);
                runtime.setVisibility(View.VISIBLE);
                c_rating.setVisibility(View.VISIBLE);
                a_rating.setVisibility(View.VISIBLE);
                consensus.setVisibility(View.VISIBLE);
                sypnosis.setVisibility(View.VISIBLE);
                c_icon.setVisibility(View.VISIBLE);
                a_icon.setVisibility(View.VISIBLE);
                movie_cast.setVisibility(View.VISIBLE);
                poster.setVisibility(View.VISIBLE);
            }
        }

    }




}
