package letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.binder;

import android.bluetooth.BluetoothSocket;
import android.os.IBinder;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 08/05/2016.
 */
public interface ISCMSBinder extends IBinder {

    void makeKickClientRequest(final BluetoothSocket toKick);

    void makePlayTrackRequest(final ITrack track);

    void makeResumePlayingRequest();

    void makePausePlayerRequest();

    boolean askHasTransferPhaseFinished();

    String askUploadProgressOf(final int index);

}
