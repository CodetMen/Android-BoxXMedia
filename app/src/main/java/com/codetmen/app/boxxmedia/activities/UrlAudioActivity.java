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
import com.codetmen.app.boxxmedia.audio_package.SongUrl;
import com.codetmen.app.boxxmedia.audio_package.SongUrlAdapter;
import com.codetmen.app.boxxmedia.db_app.SongUrlHelper;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UrlAudioActivity extends AppCompatActivity {

    @BindView(R.id.rv_UrlAudio)
    RecyclerView rvUrlAudio;

    private LinkedList<SongUrl> list;
    private SongUrlAdapter adapter;
    private SongUrlHelper songUrlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_audio);
        ButterKnife.bind(this);

        rvUrlAudio.setLayoutManager(new LinearLayoutManager(this));
        rvUrlAudio.setHasFixedSize(true);

        // open database
        songUrlHelper = new SongUrlHelper(this);
        songUrlHelper.open();

        list = new LinkedList<>();

        // set recyclerview
        adapter = new SongUrlAdapter(this);
        adapter.setSongUrls(list);
        rvUrlAudio.setAdapter(adapter);

        new LoadUrlAudioAsync().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_add, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_add_media:
                Intent intent = new Intent(UrlAudioActivity.this, EditUrlSongActivity.class);
                startActivityForResult(intent, EditUrlSongActivity.REQUEST_ADD);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(songUrlHelper != null){
            songUrlHelper.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if request add
        if (requestCode == EditUrlSongActivity.REQUEST_ADD){
            if (resultCode == EditUrlSongActivity.RESULT_ADD){
                new LoadUrlAudioAsync().execute();
                Snackbar.make(rvUrlAudio, "One item success added", Snackbar.LENGTH_LONG).show();
            }
        }
        // update and delete have same request code but different result code
        else if (requestCode == EditUrlSongActivity.REQUEST_UPDATE){
            if (resultCode == EditUrlSongActivity.RESULT_UPDATE){
                new LoadUrlAudioAsync().execute();
                int position = data.getIntExtra(EditUrlSongActivity.EXTRA_POSITION, 0);
                rvUrlAudio.getLayoutManager().smoothScrollToPosition(rvUrlAudio, new RecyclerView.State(), position);
            }
            else if (resultCode == EditUrlSongActivity.RESULT_DELETE){
                int position = data.getIntExtra(EditUrlSongActivity.EXTRA_POSITION, 0);
                list.remove(position);
                adapter.setSongUrls(list);
                adapter.notifyDataSetChanged();
                Snackbar.make(rvUrlAudio, "One item success deleted", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private class LoadUrlAudioAsync extends AsyncTask<Void, Void, ArrayList<SongUrl>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (list.size() > 0){
                list.clear();
            }
        }

        @Override
        protected ArrayList<SongUrl> doInBackground(Void... voids) {
            return songUrlHelper.query();
        }

        @Override
        protected void onPostExecute(ArrayList<SongUrl> songUrls) {
            super.onPostExecute(songUrls);

            list.addAll(songUrls);
            adapter.setSongUrls(list);
            adapter.notifyDataSetChanged();

            if (list.size() == 0){
                Snackbar.make(rvUrlAudio, "Tidak ada data", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
