package njuke.njuke_host;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;


public class main extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabs=(TabHost)findViewById(R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("overview");
        spec.setContent(R.id.overview);
        spec.setIndicator(getString(R.string.overviewTabName));
        tabs.addTab(spec);

        spec=tabs.newTabSpec("playlist");
        spec.setContent(R.id.playlist);
        spec.setIndicator(getString(R.string.playlistTabName));
        tabs.addTab(spec);

        spec=tabs.newTabSpec("voters");
        spec.setContent(R.id.voters);
        spec.setIndicator(getString(R.string.votersTabName));
        tabs.addTab(spec);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
