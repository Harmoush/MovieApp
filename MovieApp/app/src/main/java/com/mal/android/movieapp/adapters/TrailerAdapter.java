package com.mal.android.movieapp.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mal.android.movieapp.R;

/**
 * Created by ahmed on 9/21/2016.
 */

public class TrailerAdapter extends ArrayAdapter {
    Context context;
    int layoutResourceId;
    String[] names;
    String[] keys;

    public TrailerAdapter(Context context, int layoutResourceId, String[] names, String[] keys) {
        super(context, layoutResourceId, names);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.names = names;
        this.keys = keys;
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
            holder.videoIcon = (ImageView) row.findViewById(R.id.list_item_icon);
            holder.txtName = (TextView) row.findViewById(R.id.list_item_trailer_name);

            row.setTag(holder);
        } else {
            holder = (Holder) row.getTag();
        }


        holder.txtName.setText(names[position]);
        holder.videoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + keys[position])));
            }
        });

        return row;
    }


    static class Holder {
        ImageView videoIcon;
        TextView txtName;
    }
}
