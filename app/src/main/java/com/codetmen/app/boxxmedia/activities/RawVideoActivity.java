package com.codetmen.app.boxxmedia.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.additional_package.ItemClickSupport;
import com.codetmen.app.boxxmedia.video_package.Movie;
import com.codetmen.app.boxxmedia.video_package.MovieRawAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RawVideoActivity extends AppCompatActivity {

    @BindView(R.id.rv_rawVideo)
    RecyclerView rvRawVideo;

    private ArrayList<Movie> listMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_video);
        ButterKnife.bind(this);

        // back btn bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // get list data movie
        listMovies = new ArrayList<Movie>();
        getListMovie();

        // sort movies
        Collections.sort(listMovies, new Comparator<Movie>() {
            @Override
            public int compare(Movie o1, Movie o2) {
                return o1.getTitile().compareTo(o2.getTitile());
            }
        });

        // set recyclerview
        initRecyclerView();
    }

    private void initRecyclerView() {
        if (listMovies.size() > 0){
            // set view
            if (listMovies.size() > 0){
                MovieRawAdapter adapter = new MovieRawAdapter(listMovies, getApplication());
                rvRawVideo.setAdapter(adapter);
                rvRawVideo.setLayoutManager(new LinearLayoutManager(this));

                ItemClickSupport.addTo(rvRawVideo).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Movie idMovie = listMovies.get(position);
                        Intent intent = new Intent(RawVideoActivity.this, PlayerVideoActivity.class);
                        intent.putExtra(PlayerVideoActivity.EXTRA_ID_MOVIE, idMovie.getId());
                        startActivity(intent);
                    }
                });
            }
        }
    }

    public void getListMovie() {

        // set data movies from device
        ContentResolver movieResolver = getContentResolver();
        Uri movieUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor movieCursor = movieResolver.query(movieUri, null, null, null, null);

        if (movieCursor != null && movieCursor.moveToFirst()){
            // get columns
            int titileColumn = movieCursor.getColumnIndex(MediaStore.Video.Media.TITLE);
            int idColumn =  movieCursor.getColumnIndex(MediaStore.Video.Media._ID);
            // add movies to list
            do {
                long thisId = movieCursor.getLong(idColumn);
                String thisTitle = movieCursor.getString(titileColumn);

                // get preview image video
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap thumbMovie = MediaStore.Video.Thumbnails.getThumbnail(movieResolver, thisId, MediaStore.Video.Thumbnails.MICRO_KIND, options);

                listMovies.add(new Movie(thisId, thisTitle, thumbMovie));
            } while (movieCursor.moveToNext());
        } else {
            Log.e("Error_URI", "Data kosong");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            // add btn back action
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
