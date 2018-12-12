package com.codetmen.app.boxxmedia.audio_package;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codetmen.app.boxxmedia.R;

import java.util.ArrayList;

public class SongRawAdapter extends RecyclerView.Adapter<ViewHolderRaw> {

    private ArrayList<Song> songs;
    Context context;

    public SongRawAdapter(ArrayList<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolderRaw onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View songLayout =  LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        ViewHolderRaw holder = new ViewHolderRaw(songLayout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRaw holder, int position) {
        holder.songTitle.setText(songs.get(position).getTitle());
        holder.songArtist.setText(songs.get(position).getArtist());
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolderRaw extends RecyclerView.ViewHolder{

    public TextView songTitle, songArtist;

    ViewHolderRaw(View view) {
        super(view);
        songTitle = view.findViewById(R.id.song_title);
        songArtist = view.findViewById(R.id.song_artist);
    }
}