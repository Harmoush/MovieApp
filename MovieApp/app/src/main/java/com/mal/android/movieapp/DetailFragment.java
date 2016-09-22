/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mal.android.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mal.android.movieapp.adapters.ReviewsAdapter;
import com.mal.android.movieapp.adapters.TrailerAdapter;
import com.mal.android.movieapp.db.MovieContract;
import com.mal.android.movieapp.instances.Review;
import com.mal.android.movieapp.instances.Trailer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_TITLE = 1;
    static final int COL_MOVIE_OVERVIEW = 2;
    static final int COL_MOVIE_POSTER_PATH = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_MOVIE_ID = 5;
    static final int COL_MOVIE_VOTE_AVERAGE = 6;
    static final int COL_MOVIE_FAVOURITE = 7;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_FAVOURITE

    };
    TrailerAdapter trailAdapter;
    ReviewsAdapter revAdapter;
    TextView title;
    TextView rate;
    TextView overView;
    TextView date;
    ImageView img;
    ListView trailerList;
    ListView reviewsList;
    RatingBar ratingBar;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private String mMovie;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        title = (TextView) rootView.findViewById(R.id.title);
        rate = (TextView) rootView.findViewById(R.id.rate);
        overView = (TextView) rootView.findViewById(R.id.overView);
        date = (TextView) rootView.findViewById(R.id.date);
        img = (ImageView) rootView.findViewById(R.id.img);
        trailerList = (ListView) rootView.findViewById(R.id.trailsList);
        reviewsList = (ListView) rootView.findViewById(R.id.reviewsList);
        ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovie != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

        if (data != null && data.moveToFirst()) {
            title.setText(data.getString(COL_MOVIE_TITLE));
            rate.setText(data.getString(COL_MOVIE_VOTE_AVERAGE) + "/10");
            overView.setText(data.getString(COL_MOVIE_OVERVIEW));
            date.setText(data.getString(COL_MOVIE_RELEASE_DATE));
            Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + data.getString(COL_MOVIE_POSTER_PATH)).into(img);
            try {
                FetchTrailerTask trialerTask = new FetchTrailerTask(getActivity());
                ArrayList trailers = trialerTask.execute(data.getString(COL_MOVIE_MOVIE_ID)).get();
                String[] namesArr = new String[trailers.size()];
                String[] keysArr = new String[trailers.size()];

                for (int i = 0; i < trailers.size(); i++) {
                    Trailer trailer = (Trailer) trailers.get(i);
                    namesArr[i] = (trailer.getName());
                    keysArr[i] = (trailer.getKey());
                }
                mMovie = String.format("%s ", "http://www.youtube.com/watch?v=" + keysArr[0]);


                trailAdapter = new TrailerAdapter(getActivity(), R.layout.trailer_list_item, namesArr, keysArr);
                trailerList.setAdapter(trailAdapter);

                FetchReviewsTask reviewTask = new FetchReviewsTask(getActivity());
                ArrayList reviews = reviewTask.execute(data.getString(COL_MOVIE_MOVIE_ID)).get();
                String[] authArr = new String[reviews.size()];
                final String[] conArr = new String[reviews.size()];

                for (int i = 0; i < reviews.size(); i++) {
                    Review review = (Review) reviews.get(i);
                    authArr[i] = (review.getAuthor());
                    conArr[i] = (review.getContent());
                }
                revAdapter = new ReviewsAdapter(getActivity(), R.layout.review_list_item, authArr, conArr);
                reviewsList.setAdapter(revAdapter);
                reviewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String auth = ((TextView) view.findViewById(R.id.list_item_review_author)).getText().toString();
                        String con = ((TextView) view.findViewById(R.id.list_item_review_content)).getText().toString();
                        new AlertDialog.Builder(getActivity())
                                .setTitle(auth)
                                .setMessage(con)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue with delete
                                    }
                                })
                                .show();
                    }
                });
                ratingBar.setRating(Float.parseFloat(data.getString(COL_MOVIE_FAVOURITE)));
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                        ContentValues values = new ContentValues();
                        values.put(MovieContract.MovieEntry.COLUMN_FAVOURITE, v);
                        getActivity().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI, values, MovieContract.MovieEntry._ID + "=?", new String[]{data.getString((COL_MOVIE_ID))});
                    }
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onSortChanged(String sort) {

    }

    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }

    class FetchTrailerTask extends AsyncTask<Object, Object, ArrayList<Trailer>> {

        private final Context mContext;
        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        public FetchTrailerTask(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Trailer> doInBackground(Object... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailerJsonStr = null;

            String api_Key = "c684818ab17a3ee718b8b8ed28f80eb1";

            try {

                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos?";
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
                trailerJsonStr = buffer.toString();
                return getTrailerDataFromJson(trailerJsonStr);

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

        private ArrayList<Trailer> getTrailerDataFromJson(String trailerJsonStr) throws JSONException {
            try {
                final String VIDEO_ID = "id";
                final String VIDEO_Key = "key";
                final String VIDEO_NAME = "name";


                JSONObject videoJson = new JSONObject(trailerJsonStr);
                JSONArray trailerArray = videoJson.getJSONArray("results");
                ArrayList<Trailer> trailers = new ArrayList<>();
                for (int i = 0; i < trailerArray.length(); i++) {


                    JSONObject trialer = trailerArray.getJSONObject(i);
                    String videoId = trialer.getString(VIDEO_ID);
                    String videoKey = trialer.getString(VIDEO_Key);
                    String videoName = trialer.getString(VIDEO_NAME);

                    Trailer trailer = new Trailer(videoId, videoKey, videoName);
                    trailers.add(trailer);
                }
                return trailers;


            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }

    class FetchReviewsTask extends AsyncTask<Object, Object, ArrayList<Review>> {

        private final Context mContext;
        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        public FetchReviewsTask(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Review> doInBackground(Object... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewJsonStr = null;

            String api_Key = "c684818ab17a3ee718b8b8ed28f80eb1";

            try {

                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews?";
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
                reviewJsonStr = buffer.toString();
                return getReviewsDataFromJson(reviewJsonStr);

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

        private ArrayList<Review> getReviewsDataFromJson(String trailerJsonStr) throws JSONException {
            try {
                final String REVIEW_AUTHOR = "author";
                final String REVIEW_CONTENT = "content";


                JSONObject videoJson = new JSONObject(trailerJsonStr);
                JSONArray reviewArray = videoJson.getJSONArray("results");
                ArrayList<Review> reviews = new ArrayList<>();
                for (int i = 0; i < reviewArray.length(); i++) {


                    JSONObject trialer = reviewArray.getJSONObject(i);
                    String reviewAuthor = trialer.getString(REVIEW_AUTHOR);
                    String reviewContent = trialer.getString(REVIEW_CONTENT);
                    Review review = new Review(reviewAuthor, reviewContent);
                    reviews.add(review);
                }
                return reviews;


            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }
}