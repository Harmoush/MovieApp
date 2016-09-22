package com.mal.android.movieapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mal.android.movieapp.R;

/**
 * Created by ahmed on 9/21/2016.
 */

public class ReviewsAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    String[] authors;
    String[] contents;

    public ReviewsAdapter(Context context, int layoutResourceId, String[] authors, String[] contents) {
        super(context, layoutResourceId, authors);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.authors = authors;
        this.contents = contents;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new Holder();
            holder.txtAuthor = (TextView) row.findViewById(R.id.list_item_review_author);
            holder.txtContent = (TextView) row.findViewById(R.id.list_item_review_content);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }


        holder.txtContent.setText(contents[position]);
        holder.txtAuthor.setText(authors[position]);


        return row;
    }


    static class Holder {
        TextView txtAuthor;
        TextView txtContent;
    }
}