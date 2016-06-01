package letmelisten.unibo.studio.letmelisten.connection.connection_managers.client.binder;

import android.os.Binder;

import letmelisten.unibo.studio.letmelisten.connection.connection_managers.client.ClientConnectionManager;

/**
 * Created by doomdiskday on 16/05/2016.
 */
public class CMCBinder extends Binder implements ICMCBinder {

    private final ClientConnectionManager service;

    public CMCBinder(final ClientConnectionManager service) {
        this.service = service;
    }

    @Override
    public String askDownloadProgress() {
        return this.service.getDownloadProgress();
    }
}
