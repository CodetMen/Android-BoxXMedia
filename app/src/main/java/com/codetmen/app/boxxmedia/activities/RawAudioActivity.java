package com.codetmen.app.boxxmedia.activities;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.additional_package.ItemClickSupport;
import com.codetmen.app.boxxmedia.audio_package.Song;
import com.codetmen.app.boxxmedia.audio_package.SongRawAdapter;
import com.codetmen.app.boxxmedia.audio_package.SongService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RawAudioActivity extends AppCompatActivity {

    @BindView(R.id.rv_RawAudio)
    RecyclerView recyclerView;
    @BindView(R.id.tv_SongName)
    TextView tvSongName;
    @BindView(R.id.tv_Maxtime)
    TextView tvMaxtime;
    @BindView(R.id.tv_Currenttime)
    TextView tvCurrenttime;
    @BindView(R.id.seekbar_mplayer)
    SeekBar seekbarMplayer;
    @BindView(R.id.btn_PreviousSong)
    ImageButton btnPreviousSong;
    @BindView(R.id.btn_playORpauseSong)
    ImageButton btnPlayORpauseSong;
    @BindView(R.id.btn_stopSong)
    ImageButton btnStopSong;
    @BindView(R.id.ib_repeat)
    ImageButton btnRepeat;
    @BindView(R.id.ib_shuffle)
    ImageButton btnShuffle;
    @BindView(R.id.btn_nextSong)
    ImageButton btnNextSong;

    private ArrayList<Song> listSongs;
    private SongService songService;
    private Intent playIntent;
    private Handler myHandler = new Handler();
    private int resumePosition;
    private boolean songBound = false, paused = false, playbackPaused = false;

    // this mean song is not playing
    private boolean statusPlayingSong = false, statusShuffle = false, statusRepeat = false;

    // initialize seekbar time
    private double startTime = 0;
    private double endTime = 0;
    public static int oneTimeOnly = 0;

    public static final String Broadcast_PLAY_NEW_AUDIO = "Broadcast_PlayNewAudio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_audio);
        ButterKnife.bind(this);

        // add custom app bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_raw_audio);
        setSupportActionBar(toolbar);

        listSongs = new ArrayList<Song>();

        //make text runner
        tvSongName.setSelected(true);

        // get list data song
        getListSongs();

        // sort song
        Collections.sort(listSongs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });

        // set click button for item list using recyclerview
        initRecyclerView();

        // set btn start or pause song
        btnPlayORpauseSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking status of song if true so the song should be paused when the button press the othherwise
                if (statusPlayingSong == true){
                    songService.pausePlayer();
                    statusPlayingSong = false;
                    btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_play_btn));
                    resumePosition = songService.currentSong();
                    // set title of song
                    String titleSong = songService.getTitleSong();
                    tvSongName.setText(titleSong);
                } else {
                    // ToDo This still cant save the last song played
                    songService.seek(resumePosition);
                    songService.playSong();
                    statusPlayingSong = true;
                    btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_paused_btn));
                    // set title of song
                    String titleSong = songService.getTitleSong();
                    tvSongName.setText(titleSong);
                    // set timer
                    startTimes();
                    endTimes();
                    // set seekbar
                    seekbarPlaying();
                }
            }
        });

        // set btn next song
        btnNextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusPlayingSong = true;
                btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_paused_btn));
                songService.playNext();
                // set title of song
                String titleSong = songService.getTitleSong();
                tvSongName.setText(titleSong);
                // set timer
                startTimes();
                endTimes();
                // set seekbar
                seekbarPlaying();
            }
        });

        // set btn previous song
        btnPreviousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusPlayingSong == false){
                    songService.playSong();
                    statusPlayingSong = true;
                    btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_paused_btn));
                    // set title of song
                    String titleSong = songService.getTitleSong();
                    tvSongName.setText(titleSong);
                    // set timer
                    startTimes();
                    endTimes();
                    // set seekbar
                    seekbarPlaying();
                } else {
                    statusPlayingSong = true;
                    btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_paused_btn));
                    songService.playPrev();
                    // set title of song
                    String titleSong = songService.getTitleSong();
                    tvSongName.setText(titleSong);
                    // set timer
                    startTimes();
                    endTimes();
                    // set seekbar
                    seekbarPlaying();
                }
            }
        });

        // set btn stop song
        btnStopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songService.stopPlayer();
                songService.removeNotification();
                statusPlayingSong = false;
                btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_play_btn));
            }
        });

        // btn repeat condition
        btnRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ToDo repeat all songs action still empty
                // condition which status repeat is true so button is shuold turn off image
                if (statusRepeat == true) {
                    statusRepeat = false;
                    btnRepeat.setImageDrawable(getDrawable(R.drawable.ic_repeat_off));
                    Toast.makeText(getBaseContext(), "Repeat all off", Toast.LENGTH_SHORT).show();
                } else {
                    statusRepeat = true;
                    btnRepeat.setImageDrawable(getDrawable(R.drawable.ic_repeat_on));
                    Toast.makeText(getBaseContext(), "Repeat all on", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // btn shuffle condition
        btnShuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ToDo shffulle song action still empty
                // condition which status shuffle
                if (statusShuffle == true){
                    statusShuffle = false;
                    btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle_off));
                    Toast.makeText(getBaseContext(), "Shuffle off", Toast.LENGTH_SHORT).show();
                    songService.unsetShuffle();
                } else {
                    statusShuffle = true;
                    btnShuffle.setImageDrawable(getDrawable(R.drawable.ic_shuffle_on));
                    Toast.makeText(getBaseContext(), "Shuffle on", Toast.LENGTH_SHORT).show();
                    songService.setShuffle();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                songService.setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                songService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        stopService(playIntent);
        songService = null;
        unbindService(songConnection);
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(songConnection);
        songService = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        paused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initRecyclerView() {
        if (listSongs.size() > 0) {
            SongRawAdapter adapter = new SongRawAdapter(listSongs, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // set click support
            ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    // ToDo click to begin services
                    songService.setSong(position);
                    songService.playSong();
                    statusPlayingSong = true;
                    btnPlayORpauseSong.setImageDrawable(getDrawable(R.drawable.ic_paused_btn));
                    // set title of song
                    String titleSong = songService.getTitleSong();
                    tvSongName.setText(titleSong);
                    // set timer
                    startTimes();
                    endTimes();
                    // set seekbar
                    seekbarPlaying();
                }
            });
        }
    }

    // method connect to the service
    private ServiceConnection songConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SongService.SongBinder binder = (SongService.SongBinder) service;
            // get service
            songService = binder.getService();
            // pass list
            songService.setList(listSongs);
            songBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            songBound = false;
        }
    };

    public void getListSongs() {

        // set data songs from device
        ContentResolver songResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = songResolver.query(songUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            // get columns
            int titleColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            // add songs to list
            do {
                long thisId = songCursor.getLong(idColumn);
                String thisTitle = songCursor.getString(titleColumn);
                String thisArtist = songCursor.getString(artistColumn);
                listSongs.add(new Song(thisId, thisTitle, thisArtist));
            } while (songCursor.moveToNext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, SongService.class);
            bindService(playIntent, songConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void startTimes(){
        tvCurrenttime.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                startTime)))
        );
    }

    public void endTimes(){
        tvMaxtime.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                endTime)))
        );
    }

    public void seekbarPlaying(){

        startTime = songService.startDuration();
        endTime = songService.totalDuration();

        if (oneTimeOnly == 0) {
            seekbarMplayer.setMax((int) endTime);
            oneTimeOnly = 1;
        }

        seekbarMplayer.setProgress((int)startTime);
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            startTime = songService.currentSong();
            tvCurrenttime.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) startTime)))
            );
            seekbarMplayer.setProgress((int)startTime);
            myHandler.postDelayed(this, 100);
        }
    };
}
