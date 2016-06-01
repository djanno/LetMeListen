package letmelisten.unibo.studio.letmelisten.model.client;

import android.bluetooth.BluetoothSocket;



import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.connection.SocketManager;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 10/04/2016.
 */
public class ClientModel implements IClientModel {

    private static IClientModel MODEL = null;

    private BluetoothSocket socket;
    private ISocketManager manager;
    private List<ITrack> tracks;

    public static IClientModel getInstance() {
        if(MODEL == null) {
            MODEL = new ClientModel();
        }

        return MODEL;
    }

    private ClientModel() {
        this.socket = null;
        this.tracks = new ArrayList<>();
    }

    @Override
    public BluetoothSocket getSocket() {
        return this.socket;
    }

    @Override
    public ISocketManager getSocketManager() {
        return this.manager;
    }

    @Override
    public ITrack getTrack(final int index) {
        return this.tracks.get(index);
    }

    @Override
    public List<ITrack> getDownloadedTracksReference() {
        return this.tracks;
    }

    @Override
    public List<ITrack> getDownloadedTracks() {
        return new ArrayList<>(this.tracks);
    }

    @Override
    public void addReceivedTrack(final ITrack track) {
        this.tracks.add(track);
    }

    @Override
    public void setSocket(final BluetoothSocket socket) {
        this.manager = new SocketManager(socket);
        this.socket = socket;
    }

    @Override
    public void emptyReceivedTracks() {
        this.tracks = new ArrayList<>();
    }

}
