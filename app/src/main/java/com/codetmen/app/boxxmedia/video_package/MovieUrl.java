package com.codetmen.app.boxxmedia.video_package;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieUrl implements Parcelable {

    private int id;
    private String urlMovie, titleMovie;

    public MovieUrl() {
    }

    public MovieUrl(int id, String urlMovie, String titleMovie) {
        this.id = id;
        this.urlMovie = urlMovie;
        this.titleMovie = titleMovie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrlMovie() {
        return urlMovie;
    }

    public void setUrlMovie(String urlMovie) {
        this.urlMovie = urlMovie;
    }

    public String getTitleMovie() {
        return titleMovie;
    }

    public void setTitleMovie(String titleMovie) {
        this.titleMovie = titleMovie;
    }

    // parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.urlMovie);
        dest.writeString(this.titleMovie);
    }

    protected MovieUrl(Parcel in) {
        this.id = in.readInt();
        this.urlMovie = in.readString();
        this.titleMovie = in.readString();
    }

    public static final Parcelable.Creator<MovieUrl> CREATOR = new Parcelable.Creator<MovieUrl>() {
        @Override
        public MovieUrl createFromParcel(Parcel source) {
            return new MovieUrl(source);
        }

        @Override
        public MovieUrl[] newArray(int size) {
            return new MovieUrl[size];
        }
    };
}
