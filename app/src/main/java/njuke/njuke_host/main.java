package njuke.njuke_host;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Locale;

import njuke.njuke_host.backend.Song;
import njuke.njuke_host.ui.OverviewTabFragment;
import njuke.njuke_host.ui.VotersFragment;
import njuke.njuke_host.ui.musicplayer.MusicQueries;
import njuke.njuke_host.ui.playlist.PlaylistTabFragment;


public class main extends FragmentActivity implements ActionBar.TabListener, MusicQueries {
    /* Debug tag. */
    public static final String TAG = main.class.getSimpleName();
    private PlaylistTabFragment playList;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            Log.e(TAG, "Failed to get ActionBar. Bailing!");
            return;
        }
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public Song requestNextSong() {
        if(playList == null){
            playList = (PlaylistTabFragment)(mSectionsPagerAdapter.getItem(SectionsPagerAdapter.PLAYLIST));
        }
        return playList.getNextSong();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public static final int OVERVIEW = 0;
        public static final int PLAYLIST = 1;
        private static final int NUM_PAGES = 3;
        private Fragment[] fragments = new Fragment[3];
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // FIXME: This could probably be done cleaner.
            if(position < 0 || position > NUM_PAGES - 1){
                throw new IllegalArgumentException("Tried to access a non existing fragment.");
            }
            Fragment fragment = fragments[position];
            if(fragment != null){
                return fragment;
            }

            switch (position) {
                case 0:
                    fragment = new OverviewTabFragment();
                    break;
                case 1:
                    fragment = new PlaylistTabFragment();
                    break;
                case 2:
                    fragment = new VotersFragment();
                    break;
                default:
                    // TODO: Add last fragment.
                    fragment = new PlaylistTabFragment();
                    break;
            }
            fragments[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(final int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.overviewTabName).toUpperCase(l);
                case 1:
                    return getString(R.string.playlistTabName).toUpperCase(l);
                case 2:
                    return getString(R.string.votersTabName).toUpperCase(l);
                default:
                    return "";
            }
        }
    }

}
