package letmelisten.unibo.studio.letmelisten.model.client;

import android.bluetooth.BluetoothSocket;



import java.util.List;

import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 10/04/2016.
 */
public interface IClientModel {

    BluetoothSocket getSocket();

    ISocketManager getSocketManager();

    ITrack getTrack(final int index);

    List<ITrack> getDownloadedTracksReference();

    List<ITrack> getDownloadedTracks();

    void addReceivedTrack(final ITrack track);

    void setSocket(final BluetoothSocket socket);

    void emptyReceivedTracks();

}
