package letmelisten.unibo.studio.letmelisten.model.server;

import android.bluetooth.BluetoothSocket;


import java.util.List;

import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 04/04/2016.
 */

public interface IServerModel {

    List<BluetoothSocket> getSocketListReference();

    List<BluetoothSocket> getSocketList();

    List<ISocketManager> getSocketManagers();

    ISocketManager getManagerOf(final BluetoothSocket socket);

    List<ITrack> getPlaylistReference();

    List<ITrack> getPlaylist();

    List<ITrack> getUploadedReference();

    List<ITrack> getUploaded();

    boolean addSocketToList(final BluetoothSocket socket);

    boolean addTrackToPlaylist(final ITrack track);

    boolean addTrackToUploaded(final int index);

    boolean removeSocketFromList(final BluetoothSocket socket);

    boolean removeTrackFromPlayList(final ITrack track);

    void emptySocketList();

    void emptyPlaylist();

    void emptyUploaded();

}
