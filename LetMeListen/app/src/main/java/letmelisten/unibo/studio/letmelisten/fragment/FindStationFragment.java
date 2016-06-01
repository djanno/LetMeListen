package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.SmartActivity;
import letmelisten.unibo.studio.letmelisten.connection.handshake.client.ConnectionThread;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class FindStationFragment extends Fragment {

    private IntentFilter filter;
    private BroadcastReceiver receiver;

    private ServerListAdapter adapter;
    private List<BluetoothDevice> serversFound;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final android.support.v7.app.ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue_accent)));
        }

        if(this.filter == null) {
            this.filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        }

        if(this.serversFound == null) {
            this.serversFound = new ArrayList<>();
        }

        if(this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                        final BluetoothDevice found = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if(!serversFound.contains(found)) {
                            serversFound.add(found);
                            adapter.notifyDataSetChanged();
                        }
                    }
                    else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                        Toast.makeText(context, getResources().getString(R.string.scanning_message), Toast.LENGTH_SHORT).show();
                    }
                    else if(intent.getAction().equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                        Toast.makeText(context, getResources().getString(R.string.discovery_terminated_message),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }

        if(this.adapter == null) {
            this.adapter = new ServerListAdapter(this.getActivity(), this.serversFound);
        }

        this.getActivity().registerReceiver(this.receiver, this.filter);

        this.setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.find_station_fragment_layout, container, false);
        ((ListView) root.findViewById(R.id.availableServersList)).setAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();
        ((TextView) root.findViewById(R.id.adapterNameLabel2)).setText(BluetoothAdapter.getDefaultAdapter().getName());
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        this.getActivity().unregisterReceiver(this.receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().registerReceiver(this.receiver, this.filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.serversFound = new ArrayList<>();
        final android.support.v7.app.ActionBar actionBar = ((SmartActivity)getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.grey_basic)));
        }

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.find_server_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.beginDiscovery) {
            final List<BluetoothDevice> toRemove = new ArrayList<>(this.serversFound);
            this.serversFound.removeAll(toRemove);
            adapter.notifyDataSetChanged();
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            BluetoothAdapter.getDefaultAdapter().startDiscovery();
        }
        return true;
    }

    public class ServerListAdapter extends ArrayAdapter<BluetoothDevice> {

        private LayoutInflater inflater;
        private boolean connectionButtonsEnabled;

        public ServerListAdapter(final Context context, final List<BluetoothDevice> servers) {
            super(context, 0, servers);
            this.inflater = LayoutInflater.from(context);
            this.connectionButtonsEnabled = true;
        }

        public void setConnectionButtonsEnabled(final boolean enabled) {
            this.connectionButtonsEnabled = enabled;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.inflater.inflate(R.layout.available_station_layout, parent, false);
            }

            final BluetoothDevice device = this.getItem(position);
            ((TextView) convertView.findViewById(R.id.availableServerNameLabel)).setText(device.getName());
            convertView.findViewById(R.id.connectToServerButton).setEnabled(this.connectionButtonsEnabled);
            convertView.findViewById(R.id.connectToServerButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ConnectionThread(FindStationFragment.this.getActivity(), device).execute();
                }
            });

            return convertView;
        }
    }
}
