package letmelisten.unibo.studio.letmelisten.connection.handshake.server;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.SetupActivity;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoBaseMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoMessageContent;
import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.connection.handshake.server.binder.WelcomeServiceBinder;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;

/**
 * Created by Federico on 08/05/2016.
 */
public class WelcomeService extends Service implements IWelcomeService {

    public static final String UPDATE_CONNECTED_CLIENTS_IN_LOBBY = "Update Connected Clients in Lobby";
    public static final String SERVER_SOCKET_CREATION_FAILED = "Server Socket Creation Failed";

    private static final int ACCEPTABLE_CONNECTIONS = 7; //max 7 connections, since we're using bluetooth
    private static final int CHECK_CONNECTION_PERIOD_MS = 1000; //period of execution (in ms) of the thread that checks the sockets status

    private BluetoothServerSocket welcomeSocket;

    private Handler acceptConnectionsHandler;
    private Handler checkConnectionStatusHandler;

    private boolean connectionClosed;

    @Override
    public void onCreate() {
        super.onCreate();
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            this.welcomeSocket = adapter.listenUsingRfcommWithServiceRecord(adapter.getName(),
                    java.util.UUID.fromString(SetupActivity.UUID));
        }
        catch(IOException e) {
            //if the welcome socket can't be created, send an intent to inform about it
            final Intent serverSocketCreationFailedIntent = new Intent(SERVER_SOCKET_CREATION_FAILED);
            this.sendBroadcast(serverSocketCreationFailedIntent);
        }
        //connection is open
        this.connectionClosed = false;
        //initialize the thread for accepting connections
        final HandlerThread acceptConnectionsThread = new HandlerThread("AcceptConnectionsThread");
        acceptConnectionsThread.start();
        //initialize the thread to monitor the connection status
        final HandlerThread checkConnectionStatusThread = new HandlerThread("CheckConnectionStatusThread");
        checkConnectionStatusThread.start();
        //initialize the threads' handlers
        this.acceptConnectionsHandler = new Handler(acceptConnectionsThread.getLooper());
        this.checkConnectionStatusHandler = new Handler(checkConnectionStatusThread.getLooper());
        //start accepting connections
        this.acceptConnectionsHandler.post(new AcceptConnectionsTask());
        //start monitoring the connection status
        this.checkConnectionStatusHandler.post(new CheckConnectionStatusTask());
        //service created successfully, notify the user
        Toast.makeText(this.getApplicationContext(), "Your station is now online, you can host up to " +
                        ACCEPTABLE_CONNECTIONS + " listeners.", Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        //return the binder for this service
        return new WelcomeServiceBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //closing connection
        this.connectionClosed = true;
        //remove all the remaining callbacks
        this.acceptConnectionsHandler.removeCallbacksAndMessages(null);
        this.checkConnectionStatusHandler.removeCallbacksAndMessages(null);
        //close the threads
        this.acceptConnectionsHandler.getLooper().quit();
        this.checkConnectionStatusHandler.getLooper().quit();
        //close the welcome socket
        try {
            this.welcomeSocket.close();
        }
        catch(IOException e) { /**/ }
    }

    @Override
    public void kickClient(final BluetoothSocket toKick) {
        //this method only closes the socket, without updating the view
        //the view will be updated once the thread that checks the connection status finds out
        //this is done so that we don't have multiple threads (UIThread and CheckConnectionStatusThread)
        //working on the same collection in a potentially conflicting way
        try {
            final ISocketManager manager = ServerModel.getInstance().getManagerOf(toKick);
            manager.getSocketObjectInputStream().close();
            manager.getSocketObjectOutputStream().close();
            toKick.close();
        }
        catch(IOException e) { /**/ }
        finally {
            //notify the user
            Toast.makeText(this.getApplicationContext(), "Kicking " + toKick.getRemoteDevice().getName() +
                            " out of your station...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void emptyLobby() {
        for(final ISocketManager manager : ServerModel.getInstance().getSocketManagers()) {
            try {
                manager.getSocketObjectInputStream().close();
                manager.getSocketObjectOutputStream().close();
                manager.getSocket().close();
            }
            catch(IOException e) { /**/ }
        }
    }

    /**
     * This task monitors the connection with each connected socket by exchanging heartbeat messages.
     * A heartbeat message is sent to each socket with a specified period. If a socket fails to respond,
     * this means that the connection to it has been lost. If that's the case, the task removes the socket
     * from the model and informs the application components bound to it to update the view accordingly
     * (this is done through a custom intent).
     */
    private class CheckConnectionStatusTask implements Runnable {

        private boolean updateSocketStatus(final List<ISocketManager> managers) {
            final List<BluetoothSocket> disconnected = new ArrayList<>();
            //send a heartbeat message to all the clients, to which they'll reply with the same message
            for(final ISocketManager manager : managers) {
                try {
                    manager.getSocketObjectOutputStream().writeObject(new BrondoBaseMessage(BrondoMessageContent.
                            HEARTBEAT.getContent()));
                    manager.getSocketObjectOutputStream().flush();
                    manager.getSocketObjectInputStream().readObject();
                }
                catch(Exception e) {
                    //if some client isn't responding, it means that the connection to such client has been lost,
                    //therefore it has to be removed
                    try {
                        manager.getSocketObjectInputStream().close();
                        manager.getSocketObjectOutputStream().close();
                        manager.getSocket().close();
                    } catch(IOException io) { /**/ }
                    //mark the socket as disconnected
                    disconnected.add(manager.getSocket());
                }
            }
            //the disconnected clients are removed
            for(final BluetoothSocket socket : disconnected) {
                ServerModel.getInstance().removeSocketFromList(socket);
            }
            //returns true if at least one client has disconnected
            return disconnected.size() > 0;
        }

        @Override
        public void run() {
            //if a socket has been removed, communicate it to the fragment that has launched this service,
            //so that it can update the view accordingly
            if(this.updateSocketStatus(ServerModel.getInstance().getSocketManagers())) {
                //broadcasting intent to update the view
                final Intent updateConnectedClientsInLobbyIntent = new Intent(UPDATE_CONNECTED_CLIENTS_IN_LOBBY);
                WelcomeService.this.sendBroadcast(updateConnectedClientsInLobbyIntent);
            }
            //relaunch the thread after its period time if service is still running
            if(!WelcomeService.this.connectionClosed) {
                WelcomeService.this.checkConnectionStatusHandler.postDelayed(this, CHECK_CONNECTION_PERIOD_MS);
            }
        }

    }

    /**
     * This task accepts connection requests and adds the accepted sockets to the model.
     * Each time a socket is added successfully, an intent is sent towards the component bound to the service
     * so that it can be update the view accordingly.
     */
    private class AcceptConnectionsTask implements Runnable {

        @Override
        public void run() {
            try {
                if (ServerModel.getInstance().getSocketList().size() < ACCEPTABLE_CONNECTIONS) {
                    try {
                        //if there's still room, accept a new connection request and add the socket to the list
                        ServerModel.getInstance().addSocketToList(WelcomeService.this.welcomeSocket.accept());
                        //broadcasting intent to update the view
                        final Intent updateConnectedClientsInLobbyIntent = new Intent(UPDATE_CONNECTED_CLIENTS_IN_LOBBY);
                        WelcomeService.this.sendBroadcast(updateConnectedClientsInLobbyIntent);
                    } catch (SocketException s) { /**/ }
                }
            } catch (IOException e) { /**/ }
            //relaunch the thread if the service is still running
            if(!WelcomeService.this.connectionClosed) {
                WelcomeService.this.acceptConnectionsHandler.post(this);
            }
        }

    }

}
