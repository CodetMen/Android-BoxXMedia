package com.codetmen.app.boxxmedia.video_package;

import android.graphics.Bitmap;

public class Movie {
    private long id;
    private String titile;
    private Bitmap thumbMovie;

    public Movie(long id, String titile, Bitmap thumbMovie) {
        this.id = id;
        this.titile = titile;
        this.thumbMovie = thumbMovie;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitile() {
        return titile;
    }

    public void setTitile(String titile) {
        this.titile = titile;
    }

    public Bitmap getThumbMovie() {
        return thumbMovie;
    }

    public void setThumbMovie(Bitmap thumbMovie) {
        this.thumbMovie = thumbMovie;
    }
}
