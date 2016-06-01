package letmelisten.unibo.studio.letmelisten.connection.connection_managers.server;

import android.bluetooth.BluetoothSocket;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 08/05/2016.
 */
public interface IServerConnectionManager {

    void kickClient(final BluetoothSocket toKick);

    void sendPlayMessage(final ITrack track);

    void sendResumeMessage();

    void sendPauseMessage();

    boolean hasTransferPhaseFinished();

    String getUploadProgressOf(final int index);

}
