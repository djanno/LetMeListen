package letmelisten.unibo.studio.letmelisten.connection.handshake.server.binder;

import android.bluetooth.BluetoothSocket;
import android.os.Binder;

import letmelisten.unibo.studio.letmelisten.connection.handshake.server.IWelcomeService;

/**
 * Created by Federico on 08/05/2016.
 */
public class WelcomeServiceBinder extends Binder implements IWelcomeServiceBinder {

    private final IWelcomeService welcomeService;

    public WelcomeServiceBinder(final IWelcomeService welcomeService) {
        this.welcomeService = welcomeService;
    }

    @Override
    public void makeKickClientRequest(final BluetoothSocket toKick) {
        this.welcomeService.kickClient(toKick);
    }

    @Override
    public void makeEmptyLobbyRequest() {
        this.welcomeService.emptyLobby();
    }

}
