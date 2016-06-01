package letmelisten.unibo.studio.letmelisten.connection.handshake.client;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

import letmelisten.unibo.studio.letmelisten.ListenerActivity;
import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.SetupActivity;
import letmelisten.unibo.studio.letmelisten.fragment.FindStationFragment;
import letmelisten.unibo.studio.letmelisten.model.client.ClientModel;

/**
 * Created by Federico on 10/04/2016.
 */
public class ConnectionThread extends AsyncTask<Void, Void, Void> {

    private final FragmentActivity context;
    private BluetoothSocket socket;

    private final FindStationFragment.ServerListAdapter serverListAdapter;

    public ConnectionThread(final FragmentActivity context, final BluetoothDevice server) {
        this.context = context;
        this.serverListAdapter = (FindStationFragment.ServerListAdapter) ((ListView) this.context.
                findViewById(R.id.availableServersList)).getAdapter();
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.cancelDiscovery(); //canceling discovery to speed up the connection
        try {
            this.socket = server.createRfcommSocketToServiceRecord(UUID.fromString(SetupActivity.UUID));
        }
        catch(IOException e) {
            this.publishProgress();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(this.context, "Connecting to " + this.socket.getRemoteDevice().
                getName() + "...", Toast.LENGTH_SHORT).show();
        this.serverListAdapter.setConnectionButtonsEnabled(false);
        this.serverListAdapter.notifyDataSetChanged();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            this.socket.connect();
        }
        catch(IOException e) {
            this.publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        Toast.makeText(this.context, "Server unreachable, try refreshing the list.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.serverListAdapter.setConnectionButtonsEnabled(true);
        this.serverListAdapter.notifyDataSetChanged();
        if(this.socket.isConnected()) {
            ClientModel.getInstance().setSocket(this.socket);
            final Intent forListenerActivity = new Intent(this.context, ListenerActivity.class);
            this.context.startActivity(forListenerActivity);
        }
    }

}
