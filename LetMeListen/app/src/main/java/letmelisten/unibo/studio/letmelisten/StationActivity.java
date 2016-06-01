package letmelisten.unibo.studio.letmelisten;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import letmelisten.unibo.studio.letmelisten.fragment.DurationBarFragment;
import letmelisten.unibo.studio.letmelisten.fragment.ProgressFragment;
import letmelisten.unibo.studio.letmelisten.fragment.StationLobbyFragment;
import letmelisten.unibo.studio.letmelisten.fragment.StationOnlineFragment;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class StationActivity extends SmartActivity implements StationLobbyFragment.StationLobbyFragmentInteractionListener {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.station_activity_layout);
        this.replaceFragment(R.id.stationActivityMainLayout, new ProgressFragment(), false);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green_accent)));
        }
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                for(final ITrack track : ServerModel.getInstance().getPlaylist()) {
                    track.setMediaFile();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                StationActivity.this.replaceFragment(R.id.stationActivityMainLayout, new StationLobbyFragment(), false);
                StationActivity.this.replaceFragment(R.id.stationActivitySecondaryLayout, new DurationBarFragment(), false);
            }

        }.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                for(final ITrack track : ServerModel.getInstance().getPlaylist()) {
                    track.dropMediaFile();
                }
                return null;
            }

        }.execute();
    }

    @Override
    public void loadStationOnlineFragment() {
        this.replaceFragment(R.id.stationActivityMainLayout, new StationOnlineFragment(), false);
    }
}
