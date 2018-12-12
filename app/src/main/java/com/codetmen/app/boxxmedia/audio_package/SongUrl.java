package com.codetmen.app.boxxmedia.audio_package;

import android.os.Parcel;
import android.os.Parcelable;

public class SongUrl implements Parcelable {

    private int id;
    private String urlSong, titleSong;

    public SongUrl() {
    }

    public SongUrl(int id, String urlSong, String titleSong) {
        this.id = id;
        this.urlSong = urlSong;
        this.titleSong = titleSong;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrlSong() {
        return urlSong;
    }

    public void setUrlSong(String urlSong) {
        this.urlSong = urlSong;
    }

    public String getTitleSong() {
        return titleSong;
    }

    public void setTitleSong(String titleSong) {
        this.titleSong = titleSong;
    }

    // Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.urlSong);
        dest.writeString(this.titleSong);
    }

    protected SongUrl(Parcel in) {
        this.id = in.readInt();
        this.urlSong = in.readString();
        this.titleSong = in.readString();
    }

    public static final Parcelable.Creator<SongUrl> CREATOR = new Parcelable.Creator<SongUrl>() {
        @Override
        public SongUrl createFromParcel(Parcel source) {
            return new SongUrl(source);
        }

        @Override
        public SongUrl[] newArray(int size) {
            return new SongUrl[size];
        }
    };

}
