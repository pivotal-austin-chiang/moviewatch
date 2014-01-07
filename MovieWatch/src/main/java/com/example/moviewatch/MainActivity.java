package com.example.moviewatch;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class MainActivity extends Activity
{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    // the number of movies you want to get in a single request to their web server
    private static final int MOVIE_PAGE_LIMIT = 10;

    private EditText searchBox;
    private Button searchButton;
    private EnhancedListView moviesList;
    private ArrayList<JSONObject> movieTitles;

    private CustomAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moviesList = (EnhancedListView) findViewById(R.id.list_movies);
        moviesList.setClickable(true);

        new RequestTask().execute("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=15&page=1&country=us&apikey=vxwjzfe4gaczt2qpurr33cyj");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new submitListener());
        return true;
    }

    private class submitListener implements SearchView.OnQueryTextListener, SearchView.OnCloseListener
    {
        @Override
        public boolean onQueryTextSubmit(String query) {
            new RequestTask().execute("http://api.rottentomatoes.com/api/public/v1.0/movies.json?q="+query+"&page_limit=10&page=1&apikey=vxwjzfe4gaczt2qpurr33cyj");
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }


        @Override
        public boolean onClose() {
            return false;
        }

    }

    private void refreshMoviesList()
    {
        adapter = new CustomAdapter(this, R.layout.listview_layout, movieTitles);
        moviesList.setAdapter(adapter);
        moviesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String movie_id = "";
                try {
                    movie_id = movieTitles.get(position).getString("id");
                    Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                    intent.putExtra("MOVIE_ID", movie_id);
                    startActivity(intent);
                } catch (Exception e){
                    Log.d("Error", "Incorrect JSON format");
                }

//                Toast.makeText(getApplicationContext(), synopsis,Toast.LENGTH_SHORT).show();
            }
        });
        moviesList.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {

                movieTitles.remove(position);
                adapter.notifyDataSetChanged();

                return new EnhancedListView.Undoable() {
                    @Override
                    public void undo() {
                    }
                };
            }
        });

        moviesList.enableSwipeToDismiss();

    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
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

                    // add each movie's title to an array
                    movieTitles = new ArrayList();
                    for (int i = 0; i < movies.length(); i++)
                    {
                        JSONObject movie = movies.getJSONObject(i);
                        movieTitles.add (movie);
                    }


                    // update the UI
                    refreshMoviesList();
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

