package letmelisten.unibo.studio.letmelisten.connection.connection_managers.client;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoBaseMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.BrondoMessageContent;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoBaseMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoDataMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoMetadataMessage;
import letmelisten.unibo.studio.letmelisten.brondo_protocol.IBrondoPlayMessage;
import letmelisten.unibo.studio.letmelisten.connection.ISocketManager;
import letmelisten.unibo.studio.letmelisten.connection.connection_managers.client.binder.CMCBinder;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.client.ClientModel;
import letmelisten.unibo.studio.letmelisten.music_player.MusicPlayer;
import letmelisten.unibo.studio.letmelisten.music_player.binder.IMusicPlayerBinder;

/**
 * Created by Federico on 08/05/2016.
 */
public class ClientConnectionManager extends Service implements IClientConnectionManager {

    public static  final String UPDATE_TRACKS_LIST = "Update Tracks List";
    public static final String CONNECTION_CLOSED = "Connection Closed";

    private ISocketManager manager;
    private Handler connectionThreadHandler;

    private Handler mainThreadHandler;

    private ServiceConnection serviceConnection;
    private IMusicPlayerBinder musicPlayerBinder;

    private String downloadProgressLog;
    private boolean connectionClosed;

    private void closeConnection() {
        if(this.connectionClosed) {
            return;
        }
        //notify user
        Toast.makeText(this.getApplicationContext(), getResources().getString(R.string.disconnected_message)
                + ClientModel.getInstance().getSocket().getRemoteDevice().getName(), Toast.LENGTH_SHORT).show();
        //tell bound component that the connection has been closed
        final Intent connectionClosedIntent = new Intent(CONNECTION_CLOSED);
        this.sendBroadcast(connectionClosedIntent);
        //connection is closing
        this.connectionClosed = true;
        //remove all messages and callbacks from handler
        this.connectionThreadHandler.removeCallbacksAndMessages(null);
        //quit connection thread
        this.connectionThreadHandler.getLooper().quit();
        //unbind music player service
        this.unbindService(this.serviceConnection);
        //close the socket and its streams
        try {
            this.manager.getSocketObjectInputStream().close();
            this.manager.getSocketObjectOutputStream().close();
            this.manager.getSocket().close();
        } catch(IOException e) { /**/ }
        //remove from model
        ClientModel.getInstance().emptyReceivedTracks();
        ClientModel.getInstance().setSocket(null);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.manager = ClientModel.getInstance().getSocketManager();
        //connect to music player service
        this.serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(final ComponentName name, final IBinder service) {
                ClientConnectionManager.this.musicPlayerBinder = (IMusicPlayerBinder) service;
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                ClientConnectionManager.this.musicPlayerBinder = null;
            }

        };
        //connection is open
        this.connectionClosed = false;
        //start music player service
        final Intent bindMusicPlayerService = new Intent(this, MusicPlayer.class);
        this.bindService(bindMusicPlayerService, this.serviceConnection, BIND_AUTO_CREATE);
        //initialize socket streams
        this.manager.initializeObjectStreams();
        //create thread for connection handling
        final HandlerThread handleConnectionThread = new HandlerThread("HandleConnectionThread");
        handleConnectionThread.start();
        //attach the handler to the thread
        this.connectionThreadHandler = new Handler(handleConnectionThread.getLooper());
        //start the connection task
        this.connectionThreadHandler.post(new HandleConnectionTask());
        //link to the main thread so we can do operations on the main thread but in the background
        //this is useful when we have to add something to the model - if we don't do it on the main thread
        //and we use a secondary one, we could risk concurrent modification exceptions
        this.mainThreadHandler = new Handler(this.getMainLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new CMCBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.closeConnection();
    }

    @Override
    public String getDownloadProgress() {
        return this.downloadProgressLog;
    }

    private class HandleConnectionTask implements Runnable {

        private final BrondoMessageParser messageParser;

        private byte[] buffer;
        private int offset;

        public HandleConnectionTask() {
            this.messageParser = new BrondoMessageParser();
        }

        @Override
        public void run() {
            try {
                final BrondoBaseMessage received = (BrondoBaseMessage) ClientConnectionManager.this.manager.
                        getSocketObjectInputStream().readObject();

                if(!ClientConnectionManager.this.connectionClosed) {
                    final IBrondoBaseMessage response = this.messageParser.parseAndPrepareReply(received);
                    //check if the received message is a metadata message
                    if (received.getContent().equals(BrondoMessageContent.METADATA.getContent())) {
                        //if it was, prepare to receive the media file
                        final IBrondoMetadataMessage dmReceived = (IBrondoMetadataMessage) received;
                        this.buffer = new byte[dmReceived.getDataSize()];
                        this.offset = 0;
                        final ITrack receivedTrack = dmReceived.getDataInfo();
                        //add to the model - do it on the main thread
                        ClientConnectionManager.this.mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ClientModel.getInstance().addReceivedTrack(receivedTrack);
                            }
                        });
                        //tell the server that you've received the metadata and prepared the buffer
                        //to receive the media file
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().writeObject(response);
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().flush();
                    } else if (received.getContent().equals(BrondoMessageContent.DATA.getContent())) {
                        //send a response to inform the server that we're ready to receive the raw data
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().writeObject(response);
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().flush();
                        //read the chunk of data
                        final int byteCount = ((IBrondoDataMessage) received).getBytes() + this.offset;
                        while (this.offset < byteCount) {
                            this.offset += ClientConnectionManager.this.manager.getSocketObjectInputStream().
                                    read(this.buffer, this.offset, byteCount - this.offset);
                        }
                        //update view so we can display download progress
                        ClientConnectionManager.this.downloadProgressLog = "" + ((int) (((float) this.offset / this.buffer.length) * 100)) + "%";
                        final Intent updateDownloadProgressIntent = new Intent(UPDATE_TRACKS_LIST);
                        ClientConnectionManager.this.sendBroadcast(updateDownloadProgressIntent);
                        //if the media file has been entirely received
                        if (this.offset == this.buffer.length) {
                            //recover the track that has been completely received, a.k.a. the last one
                            final ITrack complete = ClientModel.getInstance().getTrack(ClientModel.getInstance().getDownloadedTracks().size() - 1);
                            //create the temp file to store the received media file
                            final File temp = File.createTempFile(complete.getTitle(), complete.getFileType());
                            temp.deleteOnExit();
                            //write the received media file to the temp file
                            final FileOutputStream fos = new FileOutputStream(temp);
                            fos.write(this.buffer);
                            fos.close();
                            //link the media file to the track
                            complete.setFilePath(temp.getPath());
                            complete.setMediaFile();
                            //send another done message if everything went right
                            ClientConnectionManager.this.manager.getSocketObjectOutputStream().writeObject(response);
                            ClientConnectionManager.this.manager.getSocketObjectOutputStream().flush();
                        }
                    } else if (received.getContent().equals(BrondoMessageContent.HEARTBEAT.getContent())) {
                        //if a heartbeat message has been received, respond with an identical message so that
                        //the server knows we're still alive
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().writeObject(response);
                        ClientConnectionManager.this.manager.getSocketObjectOutputStream().flush();
                    } else {
                        final Intent updateTracksListIntent = new Intent(UPDATE_TRACKS_LIST);
                        ClientConnectionManager.this.sendBroadcast(updateTracksListIntent);
                    }
                    ClientConnectionManager.this.connectionThreadHandler.post(this);
                }
            }
            catch(Exception e) {
                //stop the service
                ClientConnectionManager.this.closeConnection();
            }
        }

    }

    private class BrondoMessageParser {

        public IBrondoBaseMessage parseAndPrepareReply(final IBrondoBaseMessage message) {

            final String content = message.getContent();

            if(content.equals(BrondoMessageContent.METADATA.getContent()) || content.equals(BrondoMessageContent.DATA.getContent())) {
                return new BrondoBaseMessage(BrondoMessageContent.DONE.getContent());
            }
            else if(content.equals(BrondoMessageContent.QUEUE.getContent())) {
                final IBrondoPlayMessage enqueueMessage = (IBrondoPlayMessage) message;
                final ITrack toQueue = ClientModel.getInstance().getTrack(enqueueMessage.getIndex());
                toQueue.setPlayable(true);
                ClientConnectionManager.this.musicPlayerBinder.makeEnqueueRequest(toQueue);
                ClientConnectionManager.this.downloadProgressLog = "";
                final Intent updateDownloadProgressIntent = new Intent(UPDATE_TRACKS_LIST);
                ClientConnectionManager.this.sendBroadcast(updateDownloadProgressIntent);

                return null;
            }
            else if(content.equals(BrondoMessageContent.PLAY.getContent())) {
                final IBrondoPlayMessage playMessage = (IBrondoPlayMessage) message;
                ClientConnectionManager.this.musicPlayerBinder.makePlayRequest(ClientModel.getInstance().
                        getDownloadedTracks().get(playMessage.getIndex()));

                return null;
            }
            else if(content.equals(BrondoMessageContent.PAUSE.getContent())) {
                ClientConnectionManager.this.musicPlayerBinder.makePausePlayerRequest();

                return null;
            }
            else if(content.equals(BrondoMessageContent.RESUME.getContent())) {
                ClientConnectionManager.this.musicPlayerBinder.makeResumePlayingRequest();

                return null;
            }
            else if(content.equals(BrondoMessageContent.DONE.getContent())) {
                return null;
            }
            else if(content.equals(BrondoMessageContent.FAILED.getContent())) {
                throw new RuntimeException();
            }
            else if(content.equals(BrondoMessageContent.HEARTBEAT.getContent())) {
                return new BrondoBaseMessage(BrondoMessageContent.HEARTBEAT.getContent());
            }

            return null;

        }

    }

}
