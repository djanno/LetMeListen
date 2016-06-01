package letmelisten.unibo.studio.letmelisten.connection;

import android.bluetooth.BluetoothSocket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Federico on 12/05/2016.
 */
public interface ISocketManager {

    void initializeObjectStreams();

    BluetoothSocket getSocket();

    ObjectInputStream getSocketObjectInputStream();

    ObjectOutputStream getSocketObjectOutputStream();

}
