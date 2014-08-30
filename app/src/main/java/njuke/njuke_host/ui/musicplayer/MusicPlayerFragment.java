package njuke.njuke_host.ui.musicplayer;



import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import njuke.njuke_host.R;
import njuke.njuke_host.backend.MusicPlayerService;
import njuke.njuke_host.backend.Song;

public class MusicPlayerFragment extends Fragment implements View.OnClickListener{

    private MusicPlayerService playerService;
    private Intent playIntent;
    private boolean bound = false;
    private MusicQueries queries;
    private Song currentSong;
    private TextView artistTextView;
    private TextView titleTextView;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) iBinder;
            playerService = binder.getService();
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
        if(playIntent == null){
            Activity parent = getActivity();
            playIntent = new Intent(parent,MusicPlayerService.class);
            parent.bindService(playIntent,serviceConnection, Context.BIND_AUTO_CREATE);
            parent.startService(playIntent);

        }
    }

    public void nextSong() {
        Song song = queries.requestNextSong();
        titleTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtist());
        playerService.playSong(song);
        currentSong = song;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_player, container, false);
        ((Button) view.findViewById(R.id.nextBtn)).setOnClickListener(this);
        titleTextView = (TextView) view.findViewById(R.id.songTitle);
        artistTextView = (TextView) view.findViewById(R.id.songArtist);
        return view;
    }

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
                nextSong();
                break;
        }
    }

    public interface MusicQueries{
        public Song requestNextSong();
    }
}
