package letmelisten.unibo.studio.letmelisten.model.server;

import android.bluetooth.BluetoothSocket;
import android.util.Log;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.connection.SocketManager;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.Track;

/**
 * Created by Federico on 07/04/2016.
 */
public class ServerModel implements IServerModel {

    private static IServerModel MODEL = null;

    private List<BluetoothSocket> sockets;
    private List<ISocketManager> managers;

    private List<ITrack> playlist;
    private List<ITrack> uploaded;


    public static IServerModel getInstance() {
        if(MODEL == null) {
            MODEL = new ServerModel();
        }

        return MODEL;
    }

    private ServerModel() {
        this.sockets = new ArrayList<>();
        this.managers = new ArrayList<>();

        this.playlist = new ArrayList<>();
        this.uploaded = new ArrayList<>();
    }

    @Override
    public List<BluetoothSocket> getSocketListReference() {
        return this.sockets;
    }

    @Override
    public List<BluetoothSocket> getSocketList() {
        return new ArrayList<>(this.sockets);
    }

    @Override
    public List<ISocketManager> getSocketManagers() {
        return new ArrayList<>(this.managers);
    }

    @Override
    public ISocketManager getManagerOf(final BluetoothSocket socket) {
        if(!this.sockets.contains(socket)) {
            return null;
        }
        return this.managers.get(this.sockets.indexOf(socket));
    }

    @Override
    public List<ITrack> getPlaylistReference() {
        return this.playlist;
    }

    @Override
    public List<ITrack> getPlaylist() {
        return new ArrayList<>(this.playlist);
    }

    @Override
    public List<ITrack> getUploadedReference() {
        return this.uploaded;
    }

    @Override
    public List<ITrack> getUploaded() {
        return new ArrayList<>(this.uploaded);
    }

    @Override
    public boolean addSocketToList(final BluetoothSocket socket) {
        //first create the relative manager and initialize it
        final ISocketManager manager = new SocketManager(socket);
        manager.initializeObjectStreams();
        this.managers.add(manager);
        //then add the socket
        return this.sockets.add(socket);
    }

    @Override
    public boolean addTrackToPlaylist(final ITrack track) {
        return this.playlist.add(track);
    }

    @Override
    public boolean addTrackToUploaded(final int index) {
        return this.uploaded.add(new Track(this.playlist.get(index)));
    }

    @Override
    public boolean removeSocketFromList(final BluetoothSocket socket) {
        this.managers.remove(this.sockets.indexOf(socket));
        return this.sockets.remove(socket);
    }

    @Override
    public boolean removeTrackFromPlayList(final ITrack track) {
        return this.playlist.remove(track);
    }

    @Override
    public void emptySocketList() {
        this.sockets = new ArrayList<>();
        this.managers = new ArrayList<>();
    }

    @Override
    public void emptyPlaylist() {
        this.playlist = new ArrayList<>();
    }

    @Override
    public void emptyUploaded() {
        this.uploaded = new ArrayList<>();
    }

}
