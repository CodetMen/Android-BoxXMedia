package com.codetmen.app.boxxmedia.video_package;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.activities.EditUrlMovieActivity;
import com.codetmen.app.boxxmedia.activities.PlayerVideoActivity;

import java.util.LinkedList;

public class MovieUrlAdapter extends RecyclerView.Adapter<ViewHolderUrl> {

    private LinkedList<MovieUrl> movieUrls;
    private Activity activity;

    public MovieUrlAdapter(Activity activity) {
        this.activity = activity;
    }

    public LinkedList<MovieUrl> getMovieUrls() {
        return movieUrls;
    }

    public void setMovieUrls(LinkedList<MovieUrl> movieUrls) {
        this.movieUrls = movieUrls;
    }

    @NonNull
    @Override
    public ViewHolderUrl onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_url_video, parent, false);
        ViewHolderUrl holder = new ViewHolderUrl(layout);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderUrl holder, final int position) {
        // ToDO data stil not binding
        MovieUrl itemMovie = getMovieUrls().get(position);
        holder.titleMovie.setText(itemMovie.getTitleMovie());
        holder.urlMovie.setText(itemMovie.getUrlMovie());

        // btn for paying
        holder.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playing = new Intent(activity, PlayerVideoActivity.class);
                playing.putExtra(PlayerVideoActivity.EXTRA_MOVIE, getMovieUrls().get(position));
                activity.startActivity(playing);
            }
        });

        // btn for editing
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, EditUrlMovieActivity.class);
                intent.putExtra(EditUrlMovieActivity.EXTRA_POSITION, position);
                intent.putExtra(EditUrlMovieActivity.EXTRA_VIDEO, getMovieUrls().get(position));
                activity.startActivityForResult(intent, EditUrlMovieActivity.REQUEST_UPDATE);
            }
        });

        // btn for sharing
        holder.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "Hello world"+ movieUrls.get(position).getTitleMovie());
                activity.startActivity(Intent.createChooser(share, "Share using"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return getMovieUrls().size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolderUrl extends RecyclerView.ViewHolder{

    TextView titleMovie, urlMovie;
    Button btnPlay, btnEdit, btnShare;

    public ViewHolderUrl(View view){
        super(view);
        titleMovie = view.findViewById(R.id.tv_titleVideo);
        urlMovie = view.findViewById(R.id.tv_urlVideo);
        btnPlay = view.findViewById(R.id.btn_VideoPlay);
        btnEdit = view.findViewById(R.id.btn_VideoEdit);
        btnShare = view.findViewById(R.id.btn_VideoShare);
    }
}