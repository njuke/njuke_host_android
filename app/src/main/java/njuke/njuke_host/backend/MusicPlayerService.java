package njuke.njuke_host.backend;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import njuke.njuke_host.R;
import njuke.njuke_host.main;
import njuke.njuke_host.ui.musicplayer.MusicQueries;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer player;
    private final IBinder binder = new MusicBinder();
    private Song currentSong;
    private static final int NOTIFICATION_ID = 1; //where to get a better id?
    private MusicQueries queries;
    @Override
    public void onCreate(){
        super.onCreate();
        initMediaPlayer();
    }

    public void initiate(MusicQueries queries){
        this.queries = queries;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(player != null){
            player.release();
            player = null;
        }
        stopForeground(true);
    }

    public long getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public long getDuration(){
        return player.getDuration();
    }

    public boolean playSong(Song song){
        if(song == null || !getAudioFocus()){
            return false;
        }
        if(player == null){
            initMediaPlayer();
        }
        player.reset();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song.getId());
        try{
            player.setDataSource(getApplicationContext(),trackUri);
        }catch (Exception e){
            Log.e("MUSIC PLAYER SERVICE", "Error setting data source", e);
            return false;
        }
        player.prepareAsync();
        currentSong = song;
        setNotification();
        return true;
    }

    private boolean getAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        //TODO implement audio focus
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: {
                //continue play, reset changes
                if (player == null) {
                    initMediaPlayer();
                }else if(!player.isPlaying()) player.start();
                player.setVolume(1.0f,1.0f);
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS: {
                //lost for long, clean up!
                stop();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                //temporarily lost, stop music and await focus_gain
                if(player.isPlaying()){
                    player.pause();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK: {
                //play at low audio
                if(player.isPlaying()){
                    player.setVolume(0.2f,0.2f);
                }
                break;
            }
        }
    }

    private void setNotification(){
        Intent notIntent = new Intent(this, main.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.icon)
                .setTicker(currentSong.getTitle() + " - " + currentSong.getArtist())
                .setOngoing(true)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist());
        Notification not = builder.build();

        startForeground(NOTIFICATION_ID, not);
    }

    public void pauseSong(){
        if(player != null && player.isPlaying()){
            player.pause();
        }
    }

    public void resumeSong(){
        if(player == null){
            initMediaPlayer();
            playSong(currentSong);
        }
        else if(!player.isPlaying()){
            player.start();
        }
    }

    public boolean isPlaying(){
        return player != null && player.isPlaying();
    }

    private void initMediaPlayer(){
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setLooping(false);
    }

    public void stop() {
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
        player = null;
        stopForeground(true);
    }

    public class MusicBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playSong(queries.requestNextSong());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}
