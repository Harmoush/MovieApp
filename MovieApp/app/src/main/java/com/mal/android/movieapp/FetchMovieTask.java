package com.mal.android.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.mal.android.movieapp.db.MovieContract;
import com.mal.android.movieapp.db.MovieContract.MovieEntry;
import com.mal.android.movieapp.db.MovieDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by ahmed on 9/18/2016.
 */

class FetchMovieTask extends AsyncTask<String, Void, Void> {

    private final Context mContext;
    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    public FetchMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;

        String api_Key = "c684818ab17a3ee718b8b8ed28f80eb1";

        try {

            final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "?";
            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter("api_key", api_Key).build();
            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            Log.v(LOG_TAG, "Built URL :" + builtUri.toString());


            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    private void getMovieDataFromJson(String movieJsonStr) throws JSONException {
        try {
            final String POSTER_PATH = "poster_path";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String TITLE = "title";
            final String VOTE_AVERAGE = "vote_average";
            final String ID = "id";


            JSONObject fetchedJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = fetchedJson.getJSONArray("results");

            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {


                JSONObject movie = movieArray.getJSONObject(i);
                String url = movie.getString(POSTER_PATH);
                String title = movie.getString(TITLE);
                String overView = movie.getString(OVERVIEW);
                String rate = movie.getString(VOTE_AVERAGE);
                String date = movie.getString(RELEASE_DATE);
                String id = movie.getString(ID);


                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_OVERVIEW, overView);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, url);
                movieValues.put(MovieEntry.COLUMN_VOTE_AVERAGE, rate);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, date);
                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, id);
                movieValues.put(MovieEntry.COLUMN_FAVOURITE, "0");

                if (!exists(id)) {
                    cVVector.add(movieValues);
                }


            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);

                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + inserted + " Inserted");
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public boolean exists(String id) {
        MovieDBHelper mOpenHelper = new MovieDBHelper(mContext);
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME, new String[]{"1"}, MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=" + id, null, null, null, null);
        if (!c.equals(null))
            return c.moveToFirst();
        return false;
    }
}
