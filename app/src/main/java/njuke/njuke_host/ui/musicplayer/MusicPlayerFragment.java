package njuke.njuke_host.ui.musicplayer;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import njuke.njuke_host.R;
import njuke.njuke_host.backend.MusicPlayerService;
import njuke.njuke_host.backend.Song;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener, MusicQueries {

    private MusicPlayerService playerService;
    private Intent playIntent;
    private boolean bound = false;
    private MusicQueries queries,passer;
    private Song currentSong;
    private TextView artistTextView;
    private TextView titleTextView;
    private SeekBar musicSeeker;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) iBinder;
            playerService = binder.getService();
            playerService.initiate(passer);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    public void onStart(){
        super.onStart();
        passer = this;
        updateSeeker();
        if(playIntent == null){
            Activity parent = getActivity();
            playIntent = new Intent(parent,MusicPlayerService.class);
            parent.bindService(playIntent,serviceConnection, Context.BIND_AUTO_CREATE);
            parent.startService(playIntent);
        }
    }

    public Song nextSong() {
        Song song = queries.requestNextSong();
        titleTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtist());
        currentSong = song;
        updateSeeker();
        return song;
    }

    public void togglePlay(){
        if(playerService.isPlaying()){
            playerService.pauseSong();
        }else{
            updateSeeker();
            playerService.resumeSong();
        }
    }


    private void stopPlay() {
        musicSeeker.setProgress(0);
        playerService.stop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        ((Button) view.findViewById(R.id.nextBtn)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.playToggleBtn)).setOnClickListener(this);
        ((Button) view.findViewById(R.id.stopBtn)).setOnClickListener(this);
        titleTextView = (TextView) view.findViewById(R.id.songTitle);
        artistTextView = (TextView) view.findViewById(R.id.songArtist);
        musicSeeker = (SeekBar) view.findViewById(R.id.seekBar);
        musicSeeker.setEnabled(false);

        return view;
    }

    private boolean running = false;
    private void updateSeeker(){
        if(!running){
            musicSeeker.postDelayed(seekerUpdater,100);
        }
    }
    private Runnable seekerUpdater = new Runnable() {
        public void run() {
            if(playerService.isPlaying()){
                running = true;
                Double progress = (double) 0;
                long currentSeconds = (int) (playerService.getCurrentPosition() / 1000);
                long totalSeconds = (int) (playerService.getDuration() / 1000);
                progress =(((double)currentSeconds)/totalSeconds)*100;
                Log.d("Progress","Current: "+playerService.getCurrentPosition()+" Duration: "+playerService.getDuration() + " Max: "+musicSeeker.getMax()+ " Progress: "+progress);
                musicSeeker.setProgress(progress.intValue());
                musicSeeker.postDelayed(this, 100);
            }else{
                running = false;
            }
        }
    };
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            queries = (MusicQueries) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()
                    + " must implement "+MusicQueries.class.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextBtn:
                Song song = nextSong();
                playerService.playSong(song);
                break;
            case R.id.playToggleBtn:
                togglePlay();
                break;
            case R.id.stopBtn:
                stopPlay();
                break;
        }
    }

    @Override
    public Song requestNextSong() {
        return nextSong();
    }
}
