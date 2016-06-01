package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.connection.handshake.server.WelcomeService;
import letmelisten.unibo.studio.letmelisten.connection.handshake.server.binder.IWelcomeServiceBinder;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;

/**
 * Created by Federico on 10/04/2016.
 */
public class StationLobbyFragment extends Fragment {

    public interface StationLobbyFragmentInteractionListener {

        void loadStationOnlineFragment();

    }

    private static final int BECOME_DISCOVERABLE_REQUEST_ID = 6;

    private StationLobbyFragmentInteractionListener listener;
    private ServiceConnection serviceConnection;
    private IWelcomeServiceBinder serviceBinder;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private ConnectedClientsAdapter adapter;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        //if it's the first time this fragment is attached to an activity, initialized its components
        if(this.listener == null && activity instanceof StationLobbyFragmentInteractionListener) {
            //initialize listener
            this.listener = (StationLobbyFragmentInteractionListener) activity;
            //initialize service connection for service binding
            this.serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceConnected(final ComponentName name, final IBinder service) {
                    //attach the binder to the fragment
                    StationLobbyFragment.this.serviceBinder = (IWelcomeServiceBinder) service;
                }

                @Override
                public void onServiceDisconnected(final ComponentName name) {
                    //don't do anything
                }

            };
            //initialize broadcast receiver to receive intents from service
            this.broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(final Context context, final Intent intent) {
                    if(intent.getAction().equals(WelcomeService.UPDATE_CONNECTED_CLIENTS_IN_LOBBY) && StationLobbyFragment.
                            this.adapter != null) {
                        StationLobbyFragment.this.adapter.notifyDataSetChanged();
                    }
                    else if(intent.getAction().equals(WelcomeService.SERVER_SOCKET_CREATION_FAILED)) {
                        Toast.makeText(context, StationLobbyFragment.this.getResources().getString(R.string.error_create_socket_message),
                                Toast.LENGTH_SHORT).show();
                        StationLobbyFragment.this.getActivity().onBackPressed();
                    }
                }

            };
            //initialize intent filter for the broadcast receiver
            this.intentFilter = new IntentFilter();
            this.intentFilter.addAction(WelcomeService.UPDATE_CONNECTED_CLIENTS_IN_LOBBY);
            this.intentFilter.addAction(WelcomeService.SERVER_SOCKET_CREATION_FAILED);
        }
        else {
            throw new IllegalArgumentException("The activity must implement StationLobbyFragmentInteractionListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if the welcome service isn't bound to the fragment
        if(this.serviceBinder == null) {
            this.adapter = new ConnectedClientsAdapter(this.getActivity(), ServerModel.getInstance().getSocketListReference());
            final Intent bindWelcomeServiceIntent = new Intent(this.getContext(), WelcomeService.class);
            this.getActivity().bindService(bindWelcomeServiceIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
        }

        this.setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.station_lobby_fragment_layout, container, false);

        final TextView btAdapterNameLabel = (TextView) root.findViewById(R.id.adapterNameLabel);
        btAdapterNameLabel.setText(BluetoothAdapter.getDefaultAdapter().getName());

        ((ListView) (root.findViewById(R.id.connectedClientsList))).setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();

        root.findViewById(R.id.startBroadcasting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(ServerModel.getInstance().getSocketList().size() > 0) {
                    StationLobbyFragment.this.getActivity().unbindService(StationLobbyFragment.this.serviceConnection);
                    StationLobbyFragment.this.serviceBinder = null;
                    StationLobbyFragment.this.listener.loadStationOnlineFragment();
                }
                else {
                    Toast.makeText(StationLobbyFragment.this.getActivity(), getResources().getString(R.string.broadcast_error_message),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.serviceBinder != null) {
            this.serviceBinder.makeEmptyLobbyRequest();
            this.getActivity().unbindService(this.serviceConnection);
            this.serviceBinder = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BECOME_DISCOVERABLE_REQUEST_ID && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this.getActivity().getApplicationContext(), getResources().getString(R.string.visibility_message),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.server_lobby_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.makeYourselfVisible) {
            this.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), BECOME_DISCOVERABLE_REQUEST_ID);
        }
        return true;
    }

    public class ConnectedClientsAdapter extends ArrayAdapter<BluetoothSocket> {

        private final LayoutInflater inflater;

        public ConnectedClientsAdapter(final Context context, final List<BluetoothSocket> sockets) {
            super(context, 0, sockets);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.client_in_lobby_layout, parent, false);
            }

            final BluetoothSocket socket = this.getItem(position);
            ((TextView) convertView.findViewById(R.id.clientName)).setText(socket.getRemoteDevice().getName());
            convertView.findViewById(R.id.kickClient).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StationLobbyFragment.this.serviceBinder.makeKickClientRequest(socket);
                }
            });

            return convertView;
        }
    }
}
