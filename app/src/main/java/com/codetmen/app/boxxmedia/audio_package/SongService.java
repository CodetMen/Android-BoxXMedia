package com.codetmen.app.boxxmedia.audio_package;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.codetmen.app.boxxmedia.R;
import com.codetmen.app.boxxmedia.activities.RawAudioActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static android.app.Service.START_NOT_STICKY;

public class SongService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition;
    private int resumePosition;
    private Random rand;
    private Intent intent;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private AudioManager audioManager;
    private MediaSessionManager mediaSessionManager;
    private MediaSession mediaSession;
    private MediaController.TransportControls transportControls;

    private final IBinder songBind = new SongBinder();
    private String songTitle = "";
    private static final int NOTIFICATION_ID = 101;
    private boolean ongoingCall = false;
    private boolean shuffle = false;

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_STOP = "ACTION_STOP";

    /*
     * Constructor SongService
     * */
    public SongService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return songBind;
    }

    // create service
    @Override
    public void onCreate() {
        super.onCreate();

        songPosition = 0;
        player = new MediaPlayer();
        initSongPlayer();

        rand = new Random();

        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.
        callStateListener();

        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();

        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null){
            stopSelf();
            player.release();
        }

        removeAudioFocus();

        // Disable the PhoneStateListener
        if (phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        // unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);
    }

    /*
     * Audio Focus
     **/
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // invoke when audio focus of the system is updated.
        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (player == null) initSongPlayer();
                else if (!player.isPlaying()) player.start();
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // lost focus for an unbounded amount of he time: stop playback and release mediaplayer
                if (player.isPlaying()) player.stop();
                player.release();
                player = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus fos short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()) player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but its ok to keep playing
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }

    }

    // set the list of songs
    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    // set shuffle songs
    public void setShuffle() {
        shuffle = false;
    }

    // unset shuffle songs
    public void unsetShuffle() {
        shuffle = false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initSongPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        //Invoked indicating the completion of a seek operation.
    }

    public class SongBinder extends Binder {
        public SongService getService(){
            return SongService.this;
        }
    }

    public void playSong(){
        // Play a song
        player.reset();
        // Get song
        Song playSong = songs.get(songPosition);

        // get title song
        songTitle = playSong.getTitle();

        // Get id
        long currSong = playSong.getId();
        // Set Uri
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }catch (Exception e){
            Log.e("MUSIC_SERVICE", "Error setting data source", e);
        }

        try{
            player.prepare();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // set position choose which send from activity
    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    // the system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        // Service using START_NOT_STICKY, then your app will kill your service
        // once your entire application is closed from background i.e. you cleaned your app from home screen.
        return START_NOT_STICKY;
    }

    public void initSongPlayer(){

        // set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnSeekCompleteListener(this);

    }

    public String getTitleSong(){
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getTitle();
        return songTitle;
    }

    public void pausePlayer(){
        player.pause();
    }

    public void stopPlayer(){
        player.stop();
    }

    public int currentSong(){
        return player.getCurrentPosition();
    }

    public double totalDuration(){
        return player.getDuration();
    }

    public double startDuration(){
        return player.getCurrentPosition();
    }

    public void playPrev(){
        songPosition--;
        if (songPosition < 0){
            songPosition = songs.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        if (shuffle){
            int newSong = songPosition;
            while (newSong == songPosition){
                newSong = rand.nextInt(songs.size());
            }
            songPosition = newSong;
        } else {
            songPosition++;
            if (songPosition > songs.size()) {
                songPosition = 0;
            }
        }
        playSong();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    // condition for phone listener
    private void callStateListener() {
        // Get the telephony manager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (player != null) {
                            player.pause();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (player != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                int songNow = currentSong();
                                player.seekTo(songNow);
                                player.start();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            player.pause();
            // Notification
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopPlayer();
            player.reset();
            initSongPlayer();
            // Notification
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(RawAudioActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    // method for building notification
    private void buildNotification(PlaybackStatus playbackStatus) {

        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        songTitle = getTitleSong();
        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_logo); //replace with your own image

        // Create a new Notification
        Notification.Builder notificationBuilder = (Notification.Builder) new Notification.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new Notification.MediaStyle()
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(songTitle)
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, SongService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    // MediaSession and Notification actions
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSession(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSession.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();

                resumeSong();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                player.pause();
                resumePosition = player.getCurrentPosition();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNext();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrev();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void resumeSong() {
        player.seekTo(resumePosition);
        player.start();
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
}