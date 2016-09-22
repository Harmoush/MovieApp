package com.mal.android.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by ahmed on 8/13/2016.
 */
public class GridViewAdapter extends CursorAdapter {


    public GridViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layoutId = R.layout.grid_item_layout;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185/" + cursor.getString(MainFragment.COL_MOVIE_POSTER_PATH)).into(viewHolder.image);

    }

    static class ViewHolder {
        ImageView image;

        public ViewHolder(View view) {
            this.image = (ImageView) view.findViewById(R.id.image);
        }
    }
}
