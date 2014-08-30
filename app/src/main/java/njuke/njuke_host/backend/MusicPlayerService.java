package njuke.njuke_host.backend;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

public class MusicPlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private final IBinder binder = new MusicBinder();
    public MusicPlayerService() {

    }
    @Override
    public void onCreate(){
        super.onCreate();
        player = new MediaPlayer();
        initMediaPlayer();
    }

    public boolean playSong(Song song){
        player.reset();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song.getId());
        try{
            player.setDataSource(getApplicationContext(),trackUri);
        }catch (Exception e){
            Log.e("MUSIC PLAYER SERVICE", "Error setting data source", e);
            return false;
        }
        player.prepareAsync();
        return true;
    }

    public void pauseSong(){
        if(player.isPlaying()){
            player.pause();
        }
    }

    public void resumeSong(){
        if(!player.isPlaying()){
            player.start();
        }
    }

    private void initMediaPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
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
