package com.example.moviewatch;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.android.volley.*;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class MainActivity extends Activity
{
    // the Rotten Tomatoes API key of your application! get this from their website
    private static final String API_KEY = "vxwjzfe4gaczt2qpurr33cyj";

    // the number of movies you want to get in a single request to their web server
    private static final int MOVIE_PAGE_LIMIT = 10;

    private EditText searchBox;
    private Button searchButton;
    private ListView moviesListView;
    private ArrayList<Movie> moviesList = new ArrayList();
    private ArrayList<Movie> tempList;
    private MovieDataSource dataSource;

    private CustomAdapter adapter = null;
    private ImageView thumb = null;
    private boolean loadingMore = false;
    private int itemsPerPage = 15;
    private int PAGE_CAP;
    private int currentPage = 1;
    private String currentQuery = "";
    private View footerView;
    private Mode currentMode = Mode.IN_THEATRES;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private Context context = this;

    enum Mode {
        SEARCH,
        IN_THEATRES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            dataSource = new MovieDataSource(this);
            dataSource.open();
        } catch (SQLException e) {
            Log.d("OPENING DATABASE EXCEPTION", "");
            e.printStackTrace();
        }

        moviesListView = (ListView)findViewById(R.id.list_movies);
        //add the footer before adding the adapter, else the footer will not load!
        footerView = ((LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_main, null, false);
        moviesListView.addFooterView(footerView);
        moviesListView.setClickable(true);


        //Here is where the magic happens
        moviesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            //useless here, skip!
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            //dumdumdum
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                //what is the bottom iten that is visible
                int lastInScreen = firstVisibleItem + visibleItemCount;
                //is the bottom item visible & not loading more already ? Load more !
                if (currentPage <= PAGE_CAP - 1) {
                    if ((lastInScreen == totalItemCount) && !(loadingMore)) {
                        Thread thread = new Thread(null, loadMoreListItems);
                        thread.start();
                        footerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    footerView.setVisibility(View.GONE);
                }
            }
        });

        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        new RequestTask().execute("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=15&page=1&country=us&apikey=vxwjzfe4gaczt2qpurr33cyj");

    }

    private void setFooterViewInvisible() {
        ViewGroup.LayoutParams params = footerView.getLayoutParams();
        if (params != null) {
            Toast.makeText(getApplicationContext(), "toast",Toast.LENGTH_SHORT).show();
            Log.d("PARAMS", params.height +" "+ params.width);
            params.height = 0;
            footerView.setLayoutParams(params);
        }
    }

    private void setFooterViewVisible() {
        ViewGroup.LayoutParams params = footerView.getLayoutParams();
        if (params != null) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            footerView.setLayoutParams(params);
        }
    }

    //Runnable to load the items
    private Runnable loadMoreListItems = new Runnable() {
        @Override
        public void run() {
            if (adapter != null) {
                //Set flag so we cant load new items 2 at the same time
                loadingMore = true;
                //Reset the array that holds the new items
                tempList = new ArrayList<Movie>();
                //Simulate a delay, delete this on a production environment!
                try { Thread.sleep(1000);
                } catch (InterruptedException e) {}

                if (currentPage <= PAGE_CAP - 1) {
                    String response = "";
                    if (currentMode == Mode.IN_THEATRES) response = launchHTTPRequest("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit="+itemsPerPage+"&page="+(++currentPage)+"&country=us&apikey=vxwjzfe4gaczt2qpurr33cyj");
                    else response = launchHTTPRequest("http://api.rottentomatoes.com/api/public/v1.0/movies.json?q="+currentQuery+"&page_limit="+itemsPerPage+"&page="+(++currentPage)+"&apikey=vxwjzfe4gaczt2qpurr33cyj");
                    try {
                        tempList = processJSON(new JSONObject(response).getJSONArray("movies"));
                    } catch (JSONException e) {
                        Log.d("Test", "Failed to parse the JSON response!YOYLYOYOYOY");
                        e.printStackTrace();
                    }
                }

                //Done! now continue on the UI thread
                runOnUiThread(returnRes);
            }
        }
    };

    //Since we cant update our UI from a thread this Runnable takes care of that!
    private Runnable returnRes = new Runnable() {
        @Override
        public void run() {
            //Loop thru the new items and add them to the adapter
            if(tempList != null && tempList.size() > 0){
                for(int i=0;i < tempList.size();i++)
                    adapter.add(tempList.get(i));
            }
            tempList.clear();
            //Tell to the adapter that changes have been made, this will cause the list to refresh
            adapter.notifyDataSetChanged();
            //Done loading more.
            loadingMore = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            Log.d("Resume","Main activity resumed");
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new submitListener());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_watch:
                Intent intent = new Intent(getApplicationContext(), WatchList.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class submitListener implements SearchView.OnQueryTextListener, SearchView.OnCloseListener
    {
        @Override
        public boolean onQueryTextSubmit(String query) {
            query = query.replace(' ', '+');
            currentQuery = query;
            currentPage=0;
            Log.d("FORMATTED QUERY", query);
            adapter.clear();
            currentMode = Mode.SEARCH;
            setFooterViewVisible();
            new RequestTask().execute("http://api.rottentomatoes.com/api/public/v1.0/movies.json?q="+currentQuery+"&page_limit="+itemsPerPage+"&page="+(++currentPage)+"&apikey=vxwjzfe4gaczt2qpurr33cyj");
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
        adapter = new CustomAdapter(this, R.layout.listview_layout, moviesList, dataSource, mImageLoader);
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

    }

    private class RequestTask extends AsyncTask<String, String, String>
    {
        // make a request to the specified url
        @Override
        protected String doInBackground(String... uri)
        {
            return launchHTTPRequest(uri[0]);
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

                    int totalItems = Integer.parseInt(jsonResponse.getString("total"));

                    PAGE_CAP = (totalItems / itemsPerPage) + 1;

                    // fetch the array of movies in the response
                    JSONArray movies = jsonResponse.getJSONArray("movies");

                    moviesList.addAll(processJSON(movies));

                    // update the UI
                    refreshMoviesList();
                }
                catch (JSONException e)
                {
                    Log.d("Test", "Failed to parse the JSON response!");
                }
            }
        }
    }

    public String launchHTTPRequest (String uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;

        boolean isGzip = false;
        try
        {
            // make a HTTP request
            response = httpclient.execute(new HttpGet(uri));
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

    public ArrayList<Movie> processJSON (JSONArray movies) throws JSONException{
        Movie movieObject;
        ArrayList<Movie> returnList = new ArrayList<Movie>();
        for (int i = 0; i < movies.length(); i++)
        {
            JSONObject movie = movies.getJSONObject(i);
            movieObject = new Movie();
            movieObject.setMovieId(Integer.parseInt(movie.getString("id")));
            movieObject.setMovieTitle(movie.getString("title"));
            movieObject.setMovieCritics(Integer.parseInt(movie.getJSONObject("ratings").getString("critics_score")));
            movieObject.setMovieAudience(Integer.parseInt(movie.getJSONObject("ratings").getString("audience_score")));
            movieObject.setMpaa(movie.getString("mpaa_rating"));
            movieObject.setImageUrl(movie.getJSONObject("posters").getString("thumbnail"));
            returnList.add(movieObject);
        }
        return returnList;
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

