package com.codetmen.app.boxxmedia.video_package;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codetmen.app.boxxmedia.R;

import java.util.ArrayList;

public class MovieRawAdapter extends RecyclerView.Adapter<ViewHolder> {

    private ArrayList<Movie> movies;
    Context context;

    public MovieRawAdapter(ArrayList<Movie> movies, Context context) {
        this.movies = movies;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.movieTitle.setText(movies.get(position).getTitile());
        holder.thumbMovie.setImageBitmap(movies.get(position).getThumbMovie());
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}

class ViewHolder extends RecyclerView.ViewHolder{

    TextView movieTitle;
    ImageView thumbMovie;

    public ViewHolder(View itemView) {
        super(itemView);
        movieTitle = itemView.findViewById(R.id.tv_titleMovie);
        thumbMovie = itemView.findViewById(R.id.iv_imgMovie);
    }
}