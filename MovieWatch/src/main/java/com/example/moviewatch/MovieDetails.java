package com.example.moviewatch;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.zip.GZIPInputStream;
import java.io.IOException;

public class MovieDetails extends ActionBarActivity {

    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    // the number of movies you want to get in a single request to their web server
    private static final int MOVIE_PAGE_LIMIT = 10;


    private RequestTask requestTask = new RequestTask();
    private TextView title;
    private TextView mpaa;
    private TextView runtime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        title = (TextView) findViewById(R.id.title);
        mpaa = (TextView) findViewById(R.id.mpaa);
        runtime = (TextView) findViewById(R.id.runtime);

        Bundle extras = getIntent().getExtras();
        String id = extras.getString("MOVIE_ID");

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
                    mpaa.setText(requestTask.movieObject.getString("mpaa_rating"));
                    runtime.setText(requestTask.movieObject.getString("runtime"));
//                    Toast.makeText(getApplicationContext(), movieObject.getString("title"),Toast.LENGTH_SHORT).show();


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




}
