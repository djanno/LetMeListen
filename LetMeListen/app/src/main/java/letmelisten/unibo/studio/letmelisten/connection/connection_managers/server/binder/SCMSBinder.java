package letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.binder;

import android.bluetooth.BluetoothSocket;
import android.os.Binder;

import letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.ServerConnectionManager;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 08/05/2016.
 */
public class SCMSBinder extends Binder implements ISCMSBinder {

    private final ServerConnectionManager service;

    public SCMSBinder(final ServerConnectionManager service) {
        this.service = service;
    }

    @Override
    public void makeKickClientRequest(final BluetoothSocket toKick) {
        this.service.kickClient(toKick);
    }

    @Override
    public void makePlayTrackRequest(final ITrack track) {
        this.service.sendPlayMessage(track);
    }

    @Override
    public void makeResumePlayingRequest() {
        this.service.sendResumeMessage();
    }

    @Override
    public void makePausePlayerRequest() {
        this.service.sendPauseMessage();
    }

    @Override
    public boolean askHasTransferPhaseFinished() {
        return this.service.hasTransferPhaseFinished();
    }

    @Override
    public String askUploadProgressOf(final int index) {
        return this.service.getUploadProgressOf(index);
    }
}
