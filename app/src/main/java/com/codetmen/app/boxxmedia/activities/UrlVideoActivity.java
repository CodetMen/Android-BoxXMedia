package com.codetmen.app.boxxmedia.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.db_app.VideoUrlHelper;
import com.codetmen.app.boxxmedia.video_package.MovieUrl;
import com.codetmen.app.boxxmedia.video_package.MovieUrlAdapter;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.codetmen.app.boxxmedia.activities.EditUrlMovieActivity.REQUEST_ADD;

public class UrlVideoActivity extends AppCompatActivity {

    @BindView(R.id.rv_UrlVideo)
    RecyclerView rvUrlVideo;

    private LinkedList<MovieUrl> list;
    private MovieUrlAdapter adapter;
    private VideoUrlHelper videoUrlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_video);
        ButterKnife.bind(this);

        rvUrlVideo.setLayoutManager(new LinearLayoutManager(this));
        rvUrlVideo.setHasFixedSize(true);

        // open database
        videoUrlHelper = new VideoUrlHelper(this);
        videoUrlHelper.open();

        list = new LinkedList<>();

        // set recyclerview
        adapter = new MovieUrlAdapter(this);
        adapter.setMovieUrls(list);
        rvUrlVideo.setAdapter(adapter);

        new LoadUrlVideoAsync().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.option_menu_add, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_add_media:
                Intent intent = new Intent(UrlVideoActivity.this, EditUrlMovieActivity.class);
                startActivityForResult(intent, REQUEST_ADD);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoUrlHelper != null){
            videoUrlHelper.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if request add
        if (requestCode == REQUEST_ADD){
            if (resultCode == EditUrlMovieActivity.RESULT_ADD){
                new LoadUrlVideoAsync().execute();
                Snackbar.make(rvUrlVideo, "One item success added", Snackbar.LENGTH_LONG).show();
            }
        }
        // update and delete have same request code but different result code
        else if (resultCode == EditUrlMovieActivity.RESULT_UPDATE){
            new LoadUrlVideoAsync().execute();
            int position = data.getIntExtra(EditUrlMovieActivity.EXTRA_POSITION, 0);
            rvUrlVideo.getLayoutManager().smoothScrollToPosition(rvUrlVideo, new RecyclerView.State(), position);
        }
        else if (resultCode == EditUrlMovieActivity.RESULT_DELETE){
            int position = data.getIntExtra(EditUrlMovieActivity.EXTRA_POSITION, 0);
            list.remove(position);
            adapter.setMovieUrls(list);
            adapter.notifyDataSetChanged();
            Snackbar.make(rvUrlVideo, "One item success deleted", Snackbar.LENGTH_LONG).show();
        }
    }

    private class LoadUrlVideoAsync extends AsyncTask<Void, Void, ArrayList<MovieUrl>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (list.size() > 0){
                list.clear();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<MovieUrl> movieUrls) {
            super.onPostExecute(movieUrls);

            list.addAll(movieUrls);
            adapter.setMovieUrls(list);
            adapter.notifyDataSetChanged();

            if (list.size() == 0){
                Snackbar.make(rvUrlVideo, "Tidak ada data", Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        protected ArrayList<MovieUrl> doInBackground(Void... voids) {
            return videoUrlHelper.query();
        }
    }
}
