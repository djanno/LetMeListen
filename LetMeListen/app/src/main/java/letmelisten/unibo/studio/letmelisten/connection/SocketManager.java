package letmelisten.unibo.studio.letmelisten.connection;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Federico on 12/05/2016.
 */
public class SocketManager implements ISocketManager {

    private final BluetoothSocket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public SocketManager(final BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void initializeObjectStreams() {
        try {
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.oos.flush(); //flushing stream header
            this.ois = new ObjectInputStream(socket.getInputStream());
        }
        catch(IOException e) { /**/ }
    }

    @Override
    public BluetoothSocket getSocket() {
        return this.socket;
    }

    @Override
    public ObjectInputStream getSocketObjectInputStream() {
        return this.ois;
    }

    @Override
    public ObjectOutputStream getSocketObjectOutputStream() {
        return this.oos;
    }

}
