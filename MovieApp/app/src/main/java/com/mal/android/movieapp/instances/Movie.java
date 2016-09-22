package com.mal.android.movieapp.instances;

import java.io.Serializable;

/**
 * Created by ahmed on 8/13/2016.
 */

public class Movie implements Serializable {
    private String id;
    private String title;
    private String overView;
    private String url;
    private String rate;
    private String date;

    public Movie(String id, String date, String overView, String rate, String title, String url) {
        this.id = id;
        this.date = date;
        this.overView = overView;
        this.rate = rate;
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOverView() {
        return overView;
    }

    public void setOverView(String overView) {
        this.overView = overView;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
