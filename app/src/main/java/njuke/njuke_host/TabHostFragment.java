package njuke.njuke_host;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TabHost;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class TabHostFragment extends Fragment
        implements View.OnTouchListener, TabHost.OnTabChangeListener {

    private TabHost tabHost;
    private static final int ANIMATION_TIME = 350;
    private View previousView;
    private View currentView;
    private int prevTab;

    private float eventStartPos;
    private float moveTol = 10;
    public boolean onTouch(View view,MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                eventStartPos = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float diff = eventStartPos - event.getX();
                if(Math.abs(diff) > moveTol){
                    switchTab((int) diff);
                }
                break;
            }
            default:
                return false;
        }
        return true;
    }

    private void switchTab(int dir){
        int numOfTabs = tabHost.getTabWidget().getTabCount();
        if(prevTab == 0 && dir < 0 || prevTab == numOfTabs-1 && dir > 0){
            return; //If in ends, dont switch.
        }
        int newTabIndex = (tabHost.getCurrentTab() + Integer.signum(dir)+numOfTabs)%numOfTabs;
        tabHost.setCurrentTab(newTabIndex);
    }

    @Override
    public void onTabChanged(String tabId){

        currentView = tabHost.getCurrentView();
        if (tabHost.getCurrentTab() > prevTab)
        {
            previousView.setAnimation(outToLeftAnimation());
            currentView.setAnimation(inFromRightAnimation());
        }
        else
        {
            previousView.setAnimation(outToRightAnimation());
            currentView.setAnimation(inFromLeftAnimation());
        }
        previousView = currentView;
        prevTab = tabHost.getCurrentTab();

    }
    private Animation inFromRightAnimation()
    {
        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(inFromRight);
    }
    private Animation outToRightAnimation()
    {
        Animation outToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(outToRight);
    }
    private Animation inFromLeftAnimation()
    {
        Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(inFromLeft);
    }
    private Animation outToLeftAnimation()
    {
        Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        return setProperties(outToLeft);
    }
    private Animation setProperties(Animation animation)
    {
        animation.setDuration(ANIMATION_TIME);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        return animation;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);
        view.setOnTouchListener(this);
        previousView = view;
        init(view);
        return view;
    }

    private void init(View view){
        tabHost = (TabHost) view.findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.setOnTabChangedListener(this);
        TabHost.TabSpec spec = tabHost.newTabSpec("overview");
        spec.setContent(R.id.overview);
        spec.setIndicator(getString(R.string.overviewTabName));
        tabHost.addTab(spec);

        spec= tabHost.newTabSpec("playlist");
        spec.setContent(R.id.playlist);
        spec.setIndicator(getString(R.string.playlistTabName));
        tabHost.addTab(spec);

        spec= tabHost.newTabSpec("voters");
        spec.setContent(R.id.voters);
        spec.setIndicator(getString(R.string.votersTabName));
        tabHost.addTab(spec);
    }

}
