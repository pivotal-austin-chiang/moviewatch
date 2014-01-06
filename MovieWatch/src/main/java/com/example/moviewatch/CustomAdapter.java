package com.example.moviewatch;

/**
 * Created by dx165-xl on 2014-01-06.
 */
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CustomAdapter extends ArrayAdapter{
    private ArrayList<JSONObject> entries;
    private Activity activity;

    private LayoutInflater inflater;

    private int layout;

    public CustomAdapter(Activity a, int textViewResourceId, ArrayList<JSONObject> entries) {
        super(a, textViewResourceId, entries);
        layout = textViewResourceId;
        this.entries = entries;
        this.activity = a;
        inflater = LayoutInflater.from(a);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;
        TextView item1;
        TextView item2;
        if (convertView != null) {
            v = convertView;
        } else {
            v = inflater.inflate(layout, parent, false);
        }

        item1 = (TextView) v.findViewById(R.id.title);
        item2 = (TextView) v.findViewById(R.id.score);
        try {
            item1.setText(this.entries.get(position).getString("title"));
            item2.setText("Critics: " + this.entries.get(position).getJSONObject("ratings").getString("critics_score") + "% | " + "Audience: " + this.entries.get(position).getJSONObject("ratings").getString("audience_score") + "% | " + "Rated: " + this.entries.get(position).getString("mpaa_rating"));
        } catch (JSONException e) {
            Log.d("Test", "Failed to parse the JSON response!");
        }

        return v;
    }

}