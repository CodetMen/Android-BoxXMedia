package com.codetmen.app.boxxmedia.audio_package;

public class Song {

    private long id;
    private String title, artist;

    public Song(long id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

}
