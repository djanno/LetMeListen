package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.ServerConnectionManager;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.binder.ISCMSBinder;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;
import letmelisten.unibo.studio.letmelisten.music_player.MusicPlayer;

/**
 * Created by Federico on 23/04/2016.
 */
public class StationOnlineFragment extends Fragment {

    private SharedTracksAdapter sharedTracksAdapter;
    private ConnectedClientsAdapter connectedClientsAdapter;

    private ISCMSBinder connectionManagerServiceBinder;
    private ServiceConnection serviceConnection;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(this.connectionManagerServiceBinder == null) {
            this.serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(final ComponentName name, final IBinder service) {
                    StationOnlineFragment.this.connectionManagerServiceBinder = (ISCMSBinder) service;
                }

                @Override
                public void onServiceDisconnected(final ComponentName name) {
                    //don't do anything
                }

            };
            this.broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(final Context context, final Intent intent) {
                    if(intent.getAction().equals(ServerConnectionManager.UPDATE_CONNECTED_SOCKETS_LIST)) {
                        if(StationOnlineFragment.this.connectedClientsAdapter != null) {
                            StationOnlineFragment.this.connectedClientsAdapter.notifyDataSetChanged();
                        }
                    }
                    else if(intent.getAction().equals(ServerConnectionManager.UPDATE_UPLOADED_TRACKS_LIST) ||
                            intent.getAction().equals(MusicPlayer.PLAYBACK_COMPLETED)) {
                        if(StationOnlineFragment.this.sharedTracksAdapter != null) {
                            StationOnlineFragment.this.sharedTracksAdapter.notifyDataSetChanged();
                        }
                    }
                }

            };
            this.intentFilter = new IntentFilter();
            this.intentFilter.addAction(ServerConnectionManager.UPDATE_CONNECTED_SOCKETS_LIST);
            this.intentFilter.addAction(ServerConnectionManager.UPDATE_UPLOADED_TRACKS_LIST);
            this.intentFilter.addAction(MusicPlayer.PLAYBACK_COMPLETED);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.connectionManagerServiceBinder == null) {
            this.sharedTracksAdapter = new SharedTracksAdapter(this.getActivity(), ServerModel.getInstance().getUploadedReference());
            this.connectedClientsAdapter = new ConnectedClientsAdapter(this.getActivity(), ServerModel.getInstance().getSocketListReference());
            final Intent bindSCMSIntent = new Intent(this.getContext(), ServerConnectionManager.class);
            this.getActivity().bindService(bindSCMSIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.station_online_layout, container, false);
        ((TextView) root.findViewById(R.id.yourStationNameLabel)).setText(BluetoothAdapter.getDefaultAdapter().getName());
        ((ListView) root.findViewById(R.id.sharedTrackList)).setAdapter(this.sharedTracksAdapter);
        ((ListView) root.findViewById(R.id.connectedClientsList2)).setAdapter(this.connectedClientsAdapter);
        this.sharedTracksAdapter.notifyDataSetChanged();
        this.connectedClientsAdapter.notifyDataSetChanged();
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
        this.sharedTracksAdapter.notifyDataSetChanged();
        this.connectedClientsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.getActivity().unbindService(this.serviceConnection);
        this.connectionManagerServiceBinder = null;
        ServerModel.getInstance().emptyUploaded();
    }

    public class SharedTracksAdapter extends ArrayAdapter<ITrack> {

        private LayoutInflater inflater;

        public SharedTracksAdapter(final Context context, final List<ITrack> sharedTracks) {
            super(context, 0, sharedTracks);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(convertView == null) {
                convertView = this.inflater.inflate(R.layout.shared_track_layout, parent, false);
            }

            final ITrack track = this.getItem(position);

            final TextView sharedTrackInfoLabel = (TextView) convertView.findViewById(R.id.sharedTrackInfoLabel);
            final String toDisplay = track.getArtistName() + " - " + track.getTitle() + " - " + track.getAlbumName();
            sharedTrackInfoLabel.setText(toDisplay);
            sharedTrackInfoLabel.setSelected(true);

            final ImageButton playPauseButton = (ImageButton) convertView.findViewById(R.id.playPauseButton);
            playPauseButton.setVisibility(track.isPlayable() ? View.VISIBLE : View.INVISIBLE );
            playPauseButton.setEnabled(track.isPlayable());
            playPauseButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(track.isPlaying() && !track.isPaused()) {
                        StationOnlineFragment.this.connectionManagerServiceBinder.makePausePlayerRequest();
                    }
                    else if(track.isPaused()) {
                        StationOnlineFragment.this.connectionManagerServiceBinder.makeResumePlayingRequest();
                    }
                    else {
                        StationOnlineFragment.this.connectionManagerServiceBinder.makePlayTrackRequest(track);
                    }
                }

            });

            if(track.isPlaying()) {
                sharedTrackInfoLabel.setTextColor(Color.parseColor("#2ecc71"));
                if(!track.isPaused()) {
                    playPauseButton.setBackgroundResource(R.drawable.pause_icon);
                }
                else {
                    playPauseButton.setBackgroundResource(R.drawable.play_icon);
                }
            }
            else {
                playPauseButton.setBackgroundResource(R.drawable.play_icon);
                //only the tracks that have already been completely uploaded are colored like this
                if(ServerModel.getInstance().getUploaded().indexOf(track) < this.getCount() - 1 || StationOnlineFragment.this.
                        connectionManagerServiceBinder.askHasTransferPhaseFinished()) {
                    sharedTrackInfoLabel.setTextColor(Color.BLACK);
                }
            }

            return convertView;
        }
    }

    public class ConnectedClientsAdapter extends ArrayAdapter<BluetoothSocket> {

        private LayoutInflater inflater;

        public ConnectedClientsAdapter(final Context context, final List<BluetoothSocket> sockets) {
            super(context, 0, sockets);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if(this.getCount() == 0) {
                Toast.makeText(StationOnlineFragment.this.getContext(), R.string.station_closed_message, Toast.LENGTH_SHORT).show();
                StationOnlineFragment.this.getActivity().finish();
            }
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if(convertView == null) {
                convertView = this.inflater.inflate(R.layout.connected_listener_layout, parent, false);
            }

            final BluetoothDevice device = this.getItem(position).getRemoteDevice();

            final TextView clientNameLabel = (TextView) convertView.findViewById(R.id.clientName2);
            clientNameLabel.setText(device.getName());

            if(StationOnlineFragment.this.connectionManagerServiceBinder != null) {
                final TextView clientUploadProgressLabel = (TextView) convertView.findViewById(R.id.uploadProgressLabel);
                clientUploadProgressLabel.setText(StationOnlineFragment.this.connectionManagerServiceBinder.askUploadProgressOf(position));
            }

            convertView.findViewById(R.id.kickClientButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    StationOnlineFragment.this.connectionManagerServiceBinder.
                            makeKickClientRequest(ConnectedClientsAdapter.this.getItem(position));
                }

            });

            return convertView;
        }

    }
}
