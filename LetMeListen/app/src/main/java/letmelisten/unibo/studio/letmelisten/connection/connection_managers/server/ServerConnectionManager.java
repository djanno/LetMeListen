package letmelisten.unibo.studio.letmelisten.connection.connection_managers.server;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoBaseMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoDataMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoMetadataMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoMessageContent;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoPlayMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoBaseMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoDataMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoPlayMessage;
import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.server.binder.SCMSBinder;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;
import letmelisten.unibo.studio.letmelisten.music_player.MusicPlayer;
import letmelisten.unibo.studio.letmelisten.music_player.binder.IMusicPlayerBinder;

/**
 * Created by Federico on 08/05/2016.
 */
public class ServerConnectionManager extends Service implements IServerConnectionManager {

    public static final String UPDATE_CONNECTED_SOCKETS_LIST = "Update Connected Sockets List";
    public static final String UPDATE_UPLOADED_TRACKS_LIST = "Update Uploaded Tracks List";

    private static final int CHECK_CONNECTION_STATUS_PERIOD_MS = 1000;

    private Queue<ITrack> toUpload;

    private List<SendRawDataTask> sendRawDataTasks;
    private Handler dataTransferHandler;
    private boolean transferCompleted;
    private boolean connectionClosed;

    private Handler mainThreadHandler;

    private ServiceConnection serviceConnection;
    private IMusicPlayerBinder musicPlayerBinder;

    private void broadcastBrondoMessage(final IBrondoBaseMessage message) {
        final List<ISocketManager> disconnected = new ArrayList<>();
        //the given message is broadcast over to each connected client
        for (final ISocketManager manager : ServerModel.getInstance().getSocketManagers()) {
            try {
                //write the message to each stream and flush it away
                manager.getSocketObjectOutputStream().writeObject(message);
                manager.getSocketObjectOutputStream().flush();
            }
            catch (Exception e) {
                try {
                    manager.getSocketObjectInputStream().close();
                    manager.getSocketObjectOutputStream().close();
                    manager.getSocket().close();
                } catch(IOException io) { /**/ }
                //if the transfer fails, the socket is listed as disconnected
                disconnected.add(manager);
            }
        }
        //remove all the sockets listed as disconnected
        if(disconnected.size() > 0) {
            this.removeDisconnectedSockets(disconnected);
        }
    }

    //waits for the clients to send a "done" message
    private void awaitConfirmation() {
        final List<ISocketManager> disconnected = new ArrayList<>();
        for(final ISocketManager manager : ServerModel.getInstance().getSocketManagers()) {
            try {
                manager.getSocketObjectInputStream().readObject();
            }
            catch(Exception e) {
                try {
                    manager.getSocketObjectInputStream().close();
                    manager.getSocketObjectOutputStream().close();
                    manager.getSocket().close();
                }
                catch(IOException io) { /**/ }
                disconnected.add(manager);
            }
        }
        if(disconnected.size() > 0) {
            ServerConnectionManager.this.removeDisconnectedSockets(disconnected);
        }
    }

    private void removeDisconnectedSockets(final List<ISocketManager> disconnected) {
        if(!this.connectionClosed) {
            //remove the tasks bound to those sockets and the sockets themselves
            for (final ISocketManager manager : disconnected) {
                this.sendRawDataTasks.remove(ServerModel.getInstance().getSocketManagers().indexOf(manager));
                //the manager will be automatically removed by this operation
                ServerModel.getInstance().removeSocketFromList(manager.getSocket());
            }
            //inform the component bound to this service to update the view, since the model has changed
            final Intent updateConnectedSocketsIntent = new Intent(UPDATE_CONNECTED_SOCKETS_LIST);
            this.sendBroadcast(updateConnectedSocketsIntent);
            //if there are no more living sockets, we can deallocate all the resources - this service
            //will be killed as soon as the last client disconnects and the application goes into foreground
            if(ServerModel.getInstance().getSocketManagers().size() < 1) {
                this.closeConnection();
            }
        }

    }

    private void closeConnection() {
        //if the connection has already been closed, return
        if(this.connectionClosed) {
            return;
        }
        //close connection
        this.connectionClosed = true;
        //remove all remaining callbacks and messages
        this.dataTransferHandler.removeCallbacksAndMessages(null);
        //quit the data transfer thread
        this.dataTransferHandler.getLooper().quit();
        //unbind player service
        this.unbindService(this.serviceConnection);
        //close remaining sockets and streams
        for (final ISocketManager manager : ServerModel.getInstance().getSocketManagers()) {
            try {
                manager.getSocketObjectInputStream().close();
                manager.getSocketObjectOutputStream().close();
                manager.getSocket().close();
            } catch (IOException e) { /**/ }
        }
        //remove all the sockets from the model as well
        ServerModel.getInstance().emptySocketList();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //create a queue of tracks to upload
        this.toUpload = new ArrayDeque<>();
        this.toUpload.addAll(ServerModel.getInstance().getPlaylist());
        //transfer phase has still to begin
        this.transferCompleted = false;
        //the connection is opened
        this.connectionClosed = false;
        //create service connection to bind to music player service
        this.serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(final ComponentName name, final IBinder service) {
                ServerConnectionManager.this.musicPlayerBinder = (IMusicPlayerBinder) service;
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                //dont' do anything
            }

        };
        //bind to music player service
        final Intent bindMusicPlayerServiceIntent = new Intent(this, MusicPlayer.class);
        this.bindService(bindMusicPlayerServiceIntent, this.serviceConnection, BIND_AUTO_CREATE);
        //create the thread of execution for the DataTransferTask
        final HandlerThread dataTransferThread = new HandlerThread("DataTransferThread");
        dataTransferThread.start();
        //initialize the tasks that will be used to send raw data (a.k.a. the mediafiles) to the clients
        //one for each socket
        this.sendRawDataTasks = new ArrayList<>();
        for(final ISocketManager manager : ServerModel.getInstance().getSocketManagers()) {
            final SendRawDataTask sendRawDataTask = new SendRawDataTask(manager);
            this.sendRawDataTasks.add(sendRawDataTask);
        }
        //start the data transfer task delayed, so that the fragment has time to create its view
        //and the other threads have time to finish their execution
        this.dataTransferHandler = new Handler(dataTransferThread.getLooper());
        this.dataTransferHandler.postDelayed(new SendMetadataTask(), 1000);
        //link to the main thread - this is done so we can do operations in the background but on the main
        //thread, like adding to model's lists - if we were to add anything to the model from a secondary
        //thread, we would risk a concurrent modification exception
        this.mainThreadHandler = new Handler(this.getMainLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new SCMSBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.closeConnection();
    }

    @Override
    public void kickClient(final BluetoothSocket toKick) {
        try {
            final ISocketManager manager = ServerModel.getInstance().getManagerOf(toKick);
            manager.getSocketObjectInputStream().close();
            manager.getSocketObjectOutputStream().close();
            manager.getSocket().close();
        }
        catch(IOException e) { /**/ }
        finally {
            Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.kick_message) + toKick.getRemoteDevice().getName() +
                           this.getResources().getString(R.string.out_of_station_message), Toast.LENGTH_SHORT).show();
            final Intent updateConnectedSocketsIntent = new Intent(UPDATE_CONNECTED_SOCKETS_LIST);
            this.sendBroadcast(updateConnectedSocketsIntent);
        }
    }

    @Override
    public void sendPlayMessage(final ITrack track) {
        final Runnable message = new Runnable() {

            @Override
            public void run() {
                final IBrondoPlayMessage playMessage = new BrondoPlayMessage(BrondoMessageContent.PLAY.getContent(),
                        ServerModel.getInstance().getUploaded().indexOf(track));
                ServerConnectionManager.this.broadcastBrondoMessage(playMessage);
                ServerConnectionManager.this.musicPlayerBinder.makePlayRequest(track);
                final Intent updateTracksListIntent = new Intent(UPDATE_UPLOADED_TRACKS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateTracksListIntent);
                //also enqueue all the successive tracks that have been entirely received
                final List<ITrack> uploaded = ServerModel.getInstance().getUploaded();
                //for each uploaded track first we check if the track is playable - a track is playable when it has
                //been enqueued already once, or, in other words, when all the connected clients have fully received it
                //keep in mind that an uploaded track is a track whose metadata has been uploaded, therefore it is possible
                //to have an uploaded track that is not playable, since a track will become playable only when also the raw
                //data associated to its media file will be received by all connected clients
                for(int i = uploaded.indexOf(track) + 1; i < uploaded.size(); i++) {
                    if(uploaded.get(i).isPlayable()) {
                        ServerConnectionManager.this.musicPlayerBinder.makeEnqueueRequest(uploaded.get(i));
                        final IBrondoPlayMessage enqueueMessage = new BrondoPlayMessage(BrondoMessageContent.QUEUE.getContent(), i);
                        ServerConnectionManager.this.broadcastBrondoMessage(enqueueMessage);
                    }
                }
            }

        };

        this.dataTransferHandler.post(message);
    }

    @Override
    public void sendResumeMessage() {
        final Runnable message = new Runnable() {

            @Override
            public void run() {
                final IBrondoBaseMessage resumeMessage = new BrondoBaseMessage(BrondoMessageContent.RESUME.getContent());
                ServerConnectionManager.this.broadcastBrondoMessage(resumeMessage);
                ServerConnectionManager.this.musicPlayerBinder.makeResumePlayingRequest();
                final Intent updateTracksListIntent = new Intent(UPDATE_UPLOADED_TRACKS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateTracksListIntent);
            }

        };

        this.dataTransferHandler.post(message);
    }

    @Override
    public void sendPauseMessage() {
        final Runnable message = new Runnable() {

            @Override
            public void run() {
                final IBrondoBaseMessage pauseMessage = new BrondoBaseMessage(BrondoMessageContent.PAUSE.getContent());
                ServerConnectionManager.this.broadcastBrondoMessage(pauseMessage);
                ServerConnectionManager.this.musicPlayerBinder.makePausePlayerRequest();
                final Intent updateTracksListIntent = new Intent(UPDATE_UPLOADED_TRACKS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateTracksListIntent);
            }

        };

        this.dataTransferHandler.post(message);
    }

    @Override
    public boolean hasTransferPhaseFinished() {
        return this.transferCompleted;
    }

    @Override
    public String getUploadProgressOf(final int index) {
        return this.sendRawDataTasks.get(index).getUploadProgressLog();
    }

    /**
     * This task broadcasts the metadata for each track to be uploaded to all the connected clients
     */
    private class SendMetadataTask implements Runnable {

        @Override
        public void run() {
            if(ServerConnectionManager.this.toUpload.size() > 0) {
                //if there are still tracks to be uploaded, broadcast the metadata of the next one to all
                //connected clients
                final ITrack metadata = ServerConnectionManager.this.toUpload.poll();
                //add the track to the model - do it on the main thread
                ServerConnectionManager.this.mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ServerModel.getInstance().addTrackToUploaded(ServerModel.getInstance().getPlaylist().indexOf(metadata));
                    }
                });
                //update the view to display upload progress
                final Intent updateUploadedTracksIntent = new Intent(UPDATE_UPLOADED_TRACKS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateUploadedTracksIntent);
                //sending metadata to all clients
                ServerConnectionManager.this.broadcastBrondoMessage(new BrondoMetadataMessage(metadata));
                //wait for clients confirmation (so we're sure that they received the metadata)
                ServerConnectionManager.this.awaitConfirmation();
                //prepare the buffer that will contain the raw data (mediafile) to be sent
                final byte[] buffer = new byte[(int) metadata.getMediaFile().length()];
                try {
                    //setup a file input stream to read the content of the media file and store it in the buffer
                    final FileInputStream fis = new FileInputStream(metadata.getMediaFile());
                    //transfer the media file's content into the buffer
                    fis.read(buffer);
                    fis.close();
                }
                catch(IOException e) { /**/ }
                //set the buffer to all the tasks used to send raw data (media files)
                for(final SendRawDataTask sendRawDataTask : ServerConnectionManager.this.sendRawDataTasks) {
                    sendRawDataTask.setBuffer(buffer);
                }
                //then start the first task to send the data, if the service hasn't been killed already
                if(!ServerConnectionManager.this.connectionClosed) {
                    ServerConnectionManager.this.dataTransferHandler.post(ServerConnectionManager.this.sendRawDataTasks.get(0));
                }
            }
            else {
                //transfer completed, all the songs have been uploaded, start monitoring connection
                ServerConnectionManager.this.transferCompleted = true;
                if(!ServerConnectionManager.this.connectionClosed) {
                    ServerConnectionManager.this.dataTransferHandler.post(new CheckConnectionStatusTask());
                }
            }
        }

    }

    /**
     * This tasks sends chunks of data to a specific client.
     */
    private class SendRawDataTask implements Runnable {

        //if the chunk is too big, this task will keep the socket occupied for too long, slowing down
        //the responsiveness of the application when you have to send play - pause messages mid transfers
        //if it's too small, it will overload the main thread by sending intents to update the upload progress
        private static final int CHUNK_SIZE = 32768;

        private final ISocketManager manager;
        private boolean lastChunk;
        private byte[] buffer;
        private int offset;

        private String uploadProgressLog;

        public SendRawDataTask(final ISocketManager manager) {
            this.manager = manager;
        }

        public void setBuffer(final byte[] buffer) {
            this.lastChunk = false;
            this.buffer = buffer;
            this.offset = 0;
            this.uploadProgressLog = "";
        }

        public void hideUploadProgressLog() {
            this.uploadProgressLog = "";
        }

        public String getUploadProgressLog() {
            return this.uploadProgressLog;
        }

        @Override
        public void run() {
            try {
                //send a chunk or whatever is left to be sent
                final int toWrite = this.offset + CHUNK_SIZE > this.buffer.length ? this.buffer.length - this.offset : CHUNK_SIZE;
                //set the lastChunk flag, so we know if this is the last chunk to be sent
                this.lastChunk = (this.offset + toWrite == this.buffer.length);
                //first though, we need to inform the client that he's about to receive raw data, and how much,
                //so that he can prepare accordingly
                final IBrondoDataMessage message = new BrondoDataMessage(toWrite);
                this.manager.getSocketObjectOutputStream().writeObject(message);
                this.manager.getSocketObjectOutputStream().flush();
                //wait for a "done" message, so that we know that the client has setup a buffer to receive
                //the raw data
                this.manager.getSocketObjectInputStream().readObject();
                //now we can finally write the raw data
                this.manager.getSocketObjectOutputStream().write(this.buffer, this.offset, toWrite);
                this.manager.getSocketObjectOutputStream().flush();
                this.offset += toWrite;
                //update UI with percentage of transfer
                this.uploadProgressLog = "" + ((int) (((float) this.offset / this.buffer.length) * 100)) + "%";
                final Intent updateConnectedSocketsIntent = new Intent(UPDATE_CONNECTED_SOCKETS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateConnectedSocketsIntent);
            }
            catch(Exception e) {
                try {
                    this.manager.getSocketObjectInputStream().close();
                    this.manager.getSocketObjectOutputStream().close();
                    this.manager.getSocket().close();
                }
                catch(IOException io) { /**/ }
                final List<ISocketManager> disconnected = new ArrayList<>();
                disconnected.add(this.manager);
                ServerConnectionManager.this.removeDisconnectedSockets(disconnected);
            }
            finally {
                if(!ServerConnectionManager.this.connectionClosed) {
                    //once the chunk has been sent, or an exception has been caught check if there is another task that has to
                    //send this chunk to another socket
                    if(ServerModel.getInstance().getSocketManagers().indexOf(this.manager) < ServerModel.getInstance().
                            getSocketManagers().size() - 1) {
                        //if there is, start it
                        ServerConnectionManager.this.dataTransferHandler.post(ServerConnectionManager.this.
                                sendRawDataTasks.get(ServerModel.getInstance().getSocketManagers().indexOf(this.manager) + 1));
                    }
                    else {
                        //check if this was the last chunk of the media file
                        if(this.lastChunk) {
                            //if it was, the media file has been entirely received from every client, so we can send an enqueue
                            //message for this track
                            ServerConnectionManager.this.dataTransferHandler.post(new SendEnqueueMessageTask());
                        } else {
                            //if it wasn't, restart the first task to send another chunk
                            ServerConnectionManager.this.dataTransferHandler.post(ServerConnectionManager.this.sendRawDataTasks.get(0));
                        }
                    }
                }
            }
        }

    }

    private class SendEnqueueMessageTask implements Runnable {

        @Override
        public void run() {
            //first we await a "done" message from every socket, so that we know that they've all created
            //the media file and linked it to the track and they're now ready to play that track
            ServerConnectionManager.this.awaitConfirmation();
            //if we still have at least one client connected, we can procede
            if(!ServerConnectionManager.this.connectionClosed) {
                //now we can broadcast an enqueue message, but first we recover the track and set it as playable
                final int trackIndex = ServerModel.getInstance().getUploaded().size() - 1;
                final ITrack track = ServerModel.getInstance().getUploaded().get(trackIndex);
                track.setPlayable(true);
                //then we enqueue the song ourselves
                ServerConnectionManager.this.musicPlayerBinder.makeEnqueueRequest(track);
                //update the view so that the user knows that the track has been uploaded and is now playing
                final Intent updateTracksListIntent = new Intent(UPDATE_UPLOADED_TRACKS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateTracksListIntent);
                //now we finally tell the clients to enqueue it as well
                final IBrondoPlayMessage message = new BrondoPlayMessage(BrondoMessageContent.QUEUE.getContent(), trackIndex);
                ServerConnectionManager.this.broadcastBrondoMessage(message);
                //update the view by removing the upload progress
                for (final SendRawDataTask task : ServerConnectionManager.this.sendRawDataTasks) {
                    task.hideUploadProgressLog();
                }
                final Intent updateConnectedSocketsIntent = new Intent(UPDATE_CONNECTED_SOCKETS_LIST);
                ServerConnectionManager.this.sendBroadcast(updateConnectedSocketsIntent);
                //now we relaunch the task that sends the metadata
                ServerConnectionManager.this.dataTransferHandler.post(new SendMetadataTask());
            }
        }

    }

    private class CheckConnectionStatusTask implements Runnable {

        @Override
        public void run() {
            //broadcast a heartbeat message
            ServerConnectionManager.this.broadcastBrondoMessage(new BrondoBaseMessage(BrondoMessageContent.HEARTBEAT.getContent()));
            if(!ServerConnectionManager.this.connectionClosed) {
                ServerConnectionManager.this.dataTransferHandler.postDelayed(this, CHECK_CONNECTION_STATUS_PERIOD_MS);
            }
        }

    }
}
