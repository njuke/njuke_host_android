package njuke.njuke_host.ui.playlist;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import njuke.njuke_host.R;
import njuke.njuke_host.backend.Song;

public class PlaylistTabFragment extends ListFragment {
    /*private String[] songNames = new String[]{"Never Gonna Give You Up1", "Never Gonna Give You Up2",
            "Never Gonna Give You Up3", "Never Gonna Give You Up4",
            "Never Gonna Give You Up5", "Never Gonna Give You Up6",
            "Never Gonna Give You Up7", "Never Gonna Give You Up8",
            "Never Gonna Give You Up9", "Never Gonna Give You Up10",};
    private String artistName = "Rick Astley";*/
    private SongAdapter adapter;
    private int mAnimationDuration;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_tab, container, false);
        mAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime) + 100;

        ArrayList<Song> songs = new ArrayList<Song>();
        /*long idCounter = 0; //TODO real song id's!
        for (String song : songNames) {
            songs.add(new Song(song, artistName,idCounter++, 1));
        }*/

        ContentResolver musicResolver = view.getContext().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int isMusic = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.IS_MUSIC);
            //add songs to list
            do {
                if(musicCursor.getInt(isMusic) == 1){
                    long id = musicCursor.getLong(idColumn);
                    long duration = musicCursor.getInt(durationColumn);
                    String title = musicCursor.getString(titleColumn);
                    String artist = musicCursor.getString(artistColumn);
                    songs.add(new Song(title,artist,duration,id,0));
                }
            }
            while (musicCursor.moveToNext());
        }

        adapter = new SongAdapter(getActivity().getApplicationContext(), songs);
        adapter.addOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongAdapter adapter = (SongAdapter) parent.getAdapter();
                adapter.getItem(position).toggleVoted();
            }
        });
        setListAdapter(adapter);
        return view;
    }

    public Song getNextSong() {
        return adapter.playFirst();
    }

    private class SongAdapter extends BaseAdapter {
        /* Backing data store. */
        private ArrayList<Song> songs;
        /* OnItemClickListener to call when the vote button is clicked. */
        private AdapterView.OnItemClickListener itemClickListener;
        /* HashMap for saving state for the animations. */
        private HashMap<Long, Integer> mSavedState = new HashMap<Long, Integer>();
        /* Which interpolator to use when animating list changes. */
        private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
        /* The LayoutInflater to use when creating new list item views. */
        private final LayoutInflater inflater;

        private SongAdapter(Context context, ArrayList<Song> songs) {
            this.songs = songs;
            inflater = LayoutInflater.from(context);
        }

        public void addOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Song getItem(int position) {
            return songs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        public Song playFirst() {
            Song song = getItem(0);
            song.reset();
            reArrange();
            return song;
        }

        private class ViewHolder {
            public TextView title;
            public TextView artist;
            public VoteButton voteUp;
        }

        @Override
        public View getView(final int position, View recycledView, ViewGroup parent) {
            final ViewHolder vh;
            if (recycledView == null) {
                recycledView = inflater.inflate(R.layout.list_item, parent, false);
                vh = new ViewHolder();
                vh.title = (TextView) recycledView.findViewById(R.id.title);
                vh.artist = (TextView) recycledView.findViewById(R.id.artist);
                vh.voteUp = (VoteButton) recycledView.findViewById(R.id.voteup);
                recycledView.setTag(vh);
            } else {
                vh = (ViewHolder) recycledView.getTag();
            }

            final Song song = getItem(position);
            vh.voteUp.setState(song.isVoted());
            vh.voteUp.setText("+" + song.getVoteCount());
            if (itemClickListener != null) {
                vh.voteUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClickListener.onItemClick(getListView(), v, position,
                                getItemId(position));
                                reArrange();
                    }
                });
            }
            vh.title.setText(song.getTitle());
            vh.artist.setText(song.getArtist());

            return recycledView;
        }

        private void reArrange(){
            saveState();
            Collections.sort(songs, Collections.reverseOrder());
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            animateNewState();
        }

        /**
         * Save the state of the UI elements in the ListView. Should be called before
         * rearranging the list items to enable the animations.
         */
        public void saveState() {
            mSavedState.clear();
            ListView lv = getListView();
            int firstVisiblePos = lv.getFirstVisiblePosition();
            for (int i = 0; i < lv.getChildCount(); i++) {
                View v = lv.getChildAt(i);
                int top = v.getTop();
                long id = adapter.getItemId(firstVisiblePos + i);
                mSavedState.put(id, top);
            }
        }

        /**
         * Animate views into their new place in the list. Only animates if saveState has been
         * called before reordering the list.
         */
        private void animateNewState() {
            ListView lv = getListView();
            int first = lv.getFirstVisiblePosition();
            for (int i = 0; i < lv.getChildCount(); i++) {
                long id = adapter.getItemId(first + i);
                if (mSavedState.containsKey(id)) {
                    View v = lv.getChildAt(i);
                    int top = v.getTop();
                    int oldTop = mSavedState.get(id);
                    // Create and start animation.
                    ObjectAnimator animation = ObjectAnimator.ofFloat(v, "y", oldTop, top);
                    animation.setDuration(mAnimationDuration);
                    animation.setInterpolator(mInterpolator);
                    animation.start();
                }
            }
        }
    }
}
