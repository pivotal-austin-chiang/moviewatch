package com.example.moviewatch;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends Activity
{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    // the number of movies you want to get in a single request to their web server
    private static final int MOVIE_PAGE_LIMIT = 10;

    private EditText searchBox;
    private Button searchButton;
    private ListView moviesList;
    private RequestTask requestTask = new RequestTask();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesList = (ListView) findViewById(R.id.list_movies);
        requestTask.execute("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=15&page=1&country=us&apikey=vxwjzfe4gaczt2qpurr33cyj");
        moviesList.setClickable(true);
        moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String movie_id = "";
                try {
                    movie_id = requestTask.movieArray.getJSONObject(position).getString("id");
                    Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                    intent.putExtra("MOVIE_ID", movie_id);
                    startActivity(intent);
                } catch (Exception e){
                    Log.d("Error", "Incorrect JSON format");
                }

//                Toast.makeText(getApplicationContext(), synopsis,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshMoviesList(ArrayList<JSONObject> movieTitles)
    {

        moviesList.setAdapter(new CustomAdapter(this, R.layout.listview_layout, movieTitles));


    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
        JSONArray movieArray;
        // make a request to the specified url
        @Override
        protected String doInBackground(String... uri)
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try
            {
                // make a HTTP request
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK)
                {
                    // request successful - read the response and close the connection
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
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
                Log.d("Test", "Couldn't make a successful request!");
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
                    JSONObject jsonResponse = new JSONObject(response);

                    // fetch the array of movies in the response
                    JSONArray movies = jsonResponse.getJSONArray("movies");

                    movieArray = movies;

                    // add each movie's title to an array
                    ArrayList movieTitles = new ArrayList();
                    for (int i = 0; i < movies.length(); i++)
                    {
                        JSONObject movie = movies.getJSONObject(i);
                        movieTitles.add (movie);
                    }


                    // update the UI
                    refreshMoviesList(movieTitles);
                }
                catch (JSONException e)
                {
                    Log.d("Test", "Failed to parse the JSON response!");
                }
            }
        }
    }
}

