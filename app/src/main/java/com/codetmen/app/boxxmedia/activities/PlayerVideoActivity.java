package com.codetmen.app.boxxmedia.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.additional_package.OneMediaApplication;
import com.codetmen.app.boxxmedia.video_package.MovieUrl;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.RepresentationKey;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.StreamKey;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayerVideoActivity extends AppCompatActivity {

    @BindView(R.id.videoView)
    PlayerView videoView;

    private MovieUrl movieUrl;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;
    private String movieUri = "movie uri";

    private int currentWindow = 0;
    private long playBackPosition = 0;
    private boolean playWhenReady = true;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    public static String EXTRA_ID_MOVIE;
    public final static String EXTRA_MOVIE = "extra_movie";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_video);
        ButterKnife.bind(this);

        mediaDataSourceFactory = buildDataSourceFactory(true);
        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER){
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }

        // fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        // stay awake screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // initialize for rawvideo
        long idMovie =  getIntent().getLongExtra(EXTRA_ID_MOVIE, 0);

        // initilize for urlvideo
        movieUrl = getIntent().getParcelableExtra(EXTRA_MOVIE);

        // condition if idMovie not null that means data from rawvideo, otherwise
        if (idMovie != 0){

            // contentresolver is the single, global instance in your application that provides access to your (and other applications') content providers
            ContentResolver videoResolver = getContentResolver();

            // set Uri
            Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, idMovie);

            // cursor are what contain the result set of a query made against a database in Android.
            // get data with the result path
            String[] proj = {MediaStore.Video.Media.DATA};
            Cursor videoCursor = videoResolver.query(uri, proj, null, null, null);
            int nameVideo = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

            // move cursor to get data;
            videoCursor.moveToFirst();
            String thisUri = videoCursor.getString(nameVideo);
            videoCursor.close();
            Log.v("VALUE", "This value "+ thisUri);

            movieUri = thisUri;
        }
        // if movie from url is not null
        else if (movieUrl != null){
            // get movie uri from edittext
            movieUri = movieUrl.getUrlMovie();
        }
    }

    // method initilaize player for displaying it
    private void initializePlayer(String uriVideo){

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // keeping from off screen
        videoView.requestFocus();

        // Create the player
        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, new DefaultLoadControl());

        // Bind the player to the view.
        videoView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playBackPosition);

        Uri uri = Uri.parse(uriVideo);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }

    @SuppressWarnings("unchecked")
    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

//        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "one for media"), bandwidthMeter);

        // decide type uri
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<RepresentationKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParser(
                                new FilteringManifestParser<>(
                                        new HlsPlaylistParser(), (List<RenditionKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
        return ((OneMediaApplication) getApplication()).getDownloadTracker().getOfflineStreamKeys(uri);
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "ExoPlayerDemo"), bandwidthMeter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23){
            initializePlayer(movieUri);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (Util.SDK_INT <= 23 || player == null){
            // start player video
            initializePlayer(movieUri);
        }
    }

    private void hideSystemUi() {
        videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23){
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23){
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (player != null){
            playBackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
}