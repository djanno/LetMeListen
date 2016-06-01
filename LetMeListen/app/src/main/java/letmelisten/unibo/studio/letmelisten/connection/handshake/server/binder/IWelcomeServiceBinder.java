package letmelisten.unibo.studio.letmelisten.connection.handshake.server.binder;

import android.bluetooth.BluetoothSocket;
import android.os.IBinder;

/**
 * Created by Federico on 08/05/2016.
 */
public interface IWelcomeServiceBinder extends IBinder {

    void makeKickClientRequest(final BluetoothSocket toKick);

    void makeEmptyLobbyRequest();

}
