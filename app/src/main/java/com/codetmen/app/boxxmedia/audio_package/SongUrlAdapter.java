package com.codetmen.app.boxxmedia.audio_package;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.activities.EditUrlSongActivity;
import com.codetmen.app.boxxmedia.activities.PlayerAudioActivity;

import java.util.LinkedList;

public class SongUrlAdapter extends RecyclerView.Adapter<ViewHolderUrl> {

    private LinkedList<SongUrl> songUrls;
    private Activity activity;

    public SongUrlAdapter(Activity activity) {
        this.activity = activity;
    }

    public LinkedList<SongUrl> getSongUrls() {
        return songUrls;
    }

    public void setSongUrls(LinkedList<SongUrl> songUrls) {
        this.songUrls = songUrls;
    }

    @NonNull
    @Override
    public ViewHolderUrl onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url_song, parent, false);
        ViewHolderUrl holder = new ViewHolderUrl(itemLayout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderUrl holder, final int position) {
        // ToDO data stil not binding
        SongUrl itemSong = getSongUrls().get(position);
        holder.titleSong.setText(itemSong.getTitleSong());
        holder.urlSong.setText(itemSong.getUrlSong());

        // btn for playing
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playing = new Intent(activity, PlayerAudioActivity.class);
                playing.putExtra(PlayerAudioActivity.EXTRA_AUDIO, getSongUrls().get(position));
                activity.startActivity(playing);
            }
        });

        // btn for editing
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, EditUrlSongActivity.class);
                intent.putExtra(EditUrlSongActivity.EXTRA_POSITION, position);
                intent.putExtra(EditUrlSongActivity.EXTRA_SONG, getSongUrls().get(position));
                activity.startActivityForResult(intent, EditUrlSongActivity.REQUEST_UPDATE);
            }
        });

        // btn for sharing
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Hello world"+ songUrls.get(position).getTitleSong());
                activity.startActivity(Intent.createChooser(share, "Share using"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return getSongUrls().size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolderUrl extends RecyclerView.ViewHolder {

    TextView urlSong, titleSong;
    CardView itemSong;
    Button btnPlay, btnShare, btnEdit;

    ViewHolderUrl(View view) {
        super(view);
        titleSong = view.findViewById(R.id.tv_titleSong);
        urlSong = view.findViewById(R.id.tv_urlSong);
        itemSong = view.findViewById(R.id.item_url_song);
        btnPlay = view.findViewById(R.id.btn_SongPlay);
        btnShare = view.findViewById(R.id.btn_SongShare);
        btnEdit = view.findViewById(R.id.btn_SongEdit);
    }
}