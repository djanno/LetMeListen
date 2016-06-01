package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.client.ClientConnectionManager;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.client.binder.ICMCBinder;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.client.ClientModel;
import letmelisten.unibo.studio.letmelisten.music_player.MusicPlayer;

/**
 * Created by Federico on 14/04/2016.
 */
public class StationListenerFragment extends Fragment {

    private DownloadedTracksAdapter adapter;

    private ServiceConnection serviceConnection;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private ICMCBinder serviceBinder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(intent.getAction().equals(ClientConnectionManager.UPDATE_TRACKS_LIST) ||
                        intent.getAction().equals(MusicPlayer.PLAYBACK_COMPLETED)) {
                    if(StationListenerFragment.this.adapter != null) {
                        StationListenerFragment.this.adapter.notifyDataSetChanged();
                    }
                }
                else if(intent.getAction().equals(ClientConnectionManager.CONNECTION_CLOSED)) {
                    StationListenerFragment.this.getActivity().finish();
                }
            }

        };
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction(ClientConnectionManager.UPDATE_TRACKS_LIST);
        this.intentFilter.addAction(ClientConnectionManager.CONNECTION_CLOSED);
        this.intentFilter.addAction(MusicPlayer.PLAYBACK_COMPLETED);

        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName name, final IBinder service) {
                StationListenerFragment.this.serviceBinder = (ICMCBinder) service;
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                StationListenerFragment.this.serviceBinder = null;
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(this.serviceBinder == null) {
            this.adapter = new DownloadedTracksAdapter(this.getActivity(), ClientModel.getInstance().getDownloadedTracksReference());
            final Intent bindClientConnectionManagerService = new Intent(this.getContext(), ClientConnectionManager.class);
            this.getActivity().bindService(bindClientConnectionManagerService, this.serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.station_listener_fragment_layout, container, false);

        final TextView serverName = (TextView) root.findViewById(R.id.stationNameLabel);
        serverName.setTextColor(Color.BLUE);
        serverName.setText(ClientModel.getInstance().getSocket().getRemoteDevice().getName());

        ((ListView) root.findViewById(R.id.downloadedTracksList)).setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();

        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().registerReceiver(this.broadcastReceiver, this.intentFilter);
        this.adapter.notifyDataSetChanged();
        //if the connection was closed while this was in background, the activity has to stop
        if(ClientModel.getInstance().getSocket() == null) {
            Toast.makeText(this.getContext(), getResources().getString(R.string.disconnected_message)
                    + ClientModel.getInstance().getSocket().getRemoteDevice().getName(), Toast.LENGTH_SHORT).show();
            this.getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getActivity().unbindService(this.serviceConnection);
    }

    public class DownloadedTracksAdapter extends ArrayAdapter<ITrack> {

        private LayoutInflater inflater;

        public DownloadedTracksAdapter(final Context context, final List<ITrack> downloadedTracks) {
            super(context, 0, downloadedTracks);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(convertView == null) {
                convertView = this.inflater.inflate(R.layout.downloaded_track_layout, parent, false);
            }

            final ITrack track = this.getItem(position);

            final TextView downloadedTrackInfoLabel = (TextView) convertView.findViewById(R.id.downloadedTrackInfoLabel);
            final String downloadedTrackInfo = track.getArtistName() + " - " + track.getTitle() + " - " + track.getAlbumName();
            downloadedTrackInfoLabel.setText(downloadedTrackInfo);
            downloadedTrackInfoLabel.setSelected(true);

            final TextView downloadProgressLabel = (TextView) convertView.findViewById(R.id.downloadProgressLabel);

            if(StationListenerFragment.this.serviceBinder != null) {
                downloadProgressLabel.setText(position < this.getCount() - 1 ? "" : StationListenerFragment.this.serviceBinder.askDownloadProgress());
            }

            final ImageView playingIcon = (ImageView) convertView.findViewById(R.id.playingIcon);

            if(track.isPlaying()) {
                playingIcon.setVisibility(View.VISIBLE);
                downloadProgressLabel.setVisibility(View.GONE);
                downloadedTrackInfoLabel.setTextColor(Color.BLUE);
            }
            else if(downloadProgressLabel.getText().equals("")){
                playingIcon.setVisibility(View.INVISIBLE);
                downloadProgressLabel.setVisibility(View.GONE);
                downloadedTrackInfoLabel.setTextColor(Color.BLACK);
            }
            else {
                downloadProgressLabel.setVisibility(View.VISIBLE);
                downloadedTrackInfoLabel.setTextColor(Color.GRAY);
            }

            return convertView;
        }

    }
}
