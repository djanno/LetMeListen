package letmelisten.unibo.studio.letmelisten.connection.handshake.server;

import android.bluetooth.BluetoothSocket;

/**
 * Created by Federico on 08/05/2016.
 */
public interface IWelcomeService {

    void kickClient(final BluetoothSocket toKick);

    void emptyLobby();

}
