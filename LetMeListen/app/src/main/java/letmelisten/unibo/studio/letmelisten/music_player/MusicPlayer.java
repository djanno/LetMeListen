package letmelisten.unibo.studio.letmelisten.music_player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.music_player.binder.MusicPlayerBinder;

/**
 * Created by doomdiskday on 28/04/2016.
 */
public class MusicPlayer extends Service implements IMusicPlayer {

    public static final String PLAYBACK_COMPLETED = "Playback Completed";
    public static final String UPDATE_DURATION_BAR = "Update Duration Bar";

    private Queue<ITrack> queue;

    private MediaPlayer player;
    private ITrack currentlyPlaying;
    private FileInputStream dataStream;

    private Handler durationBarUpdateHandler;

    private void prepareDataSource(final ITrack track) {
        try {
            this.currentlyPlaying = track;
            this.dataStream = new FileInputStream(track.getMediaFile());
            this.player.setDataSource(this.dataStream.getFD());
            this.currentlyPlaying.play();
        } catch(IOException e) { /**/ }
    }

    private void closeDataSource() {
        try {
            this.dataStream.close();
        } catch(IOException e) { /**/ }
        this.currentlyPlaying.stop();
        this.currentlyPlaying = null;
    }

    private void loadFollowingTrack() {
        if(this.queue.size() > 0) {
            this.player.reset();
            this.prepareDataSource(this.queue.poll());
            this.player.prepareAsync();
        }
    }

    private void stop() {
        if(this.player.isPlaying()) {
            this.player.stop();
        }
        if(this.currentlyPlaying != null) {
            this.closeDataSource();
        }
    }



    @Override
    public void onCreate() {
        super.onCreate();
        this.queue = new ArrayDeque<>();
        this.player = new MediaPlayer();
        this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        this.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(final MediaPlayer mp) {
                player.start();
                durationBarUpdateHandler.post(new UpdateDurationBarTask());
            }

        });
        this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(final MediaPlayer mp) {
                MusicPlayer.this.closeDataSource();
                MusicPlayer.this.loadFollowingTrack();
                final Intent playbackCompletedIntent = new Intent(PLAYBACK_COMPLETED);
                MusicPlayer.this.sendBroadcast(playbackCompletedIntent);
            }

        });
        this.currentlyPlaying = null;

        final HandlerThread durationBarUpdateThread = new HandlerThread("DurationBarUpdateThread");
        durationBarUpdateThread.start();
        this.durationBarUpdateHandler = new Handler(durationBarUpdateThread.getLooper());
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return new MusicPlayerBinder(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.player.isPlaying()) {
            this.stop();
        }
        this.player.release();
        this.durationBarUpdateHandler.removeCallbacksAndMessages(null);
        this.durationBarUpdateHandler.getLooper().quit();
    }

    @Override
    public void enqueue(final ITrack track) {
        this.queue.add(track);
        if(this.currentlyPlaying == null) {
            this.loadFollowingTrack();
        }
    }

    @Override
    public void play(final ITrack track) {
        if(this.player.isPlaying()) {
            this.player.stop();
        }
        try {
            this.dataStream.close();
        } catch(IOException e) { /**/ }
        if(this.currentlyPlaying != null) {
            this.currentlyPlaying.stop();
            this.currentlyPlaying = null;
        }
        this.durationBarUpdateHandler.removeCallbacksAndMessages(null);
        this.queue = new ArrayDeque<>();
        this.player.reset();
        this.prepareDataSource(track);
        this.player.prepareAsync();
    }

    @Override
    public void resume() {
        if(!this.player.isPlaying()) {
            this.currentlyPlaying.resume();
            this.player.start();
        }
    }

    @Override
    public void pause() {
        if(this.player.isPlaying()) {
            this.currentlyPlaying.pause();
            this.player.pause();
        }
    }

    private class UpdateDurationBarTask implements Runnable {

        private int millis;

        private String parseDurationStringFromMillis(final int millis) {
            return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        }

        public UpdateDurationBarTask() {
            this.millis = 0;
        }

        @Override
        public void run() {
            //only run if the service hasn't stopped
            if(currentlyPlaying != null) {
                if (currentlyPlaying.isPaused()) {
                    durationBarUpdateHandler.post(this);
                }
                else {
                    final Intent updateDurationBarIntent = new Intent(UPDATE_DURATION_BAR);
                    final int duration = currentlyPlaying.getDuration();
                    updateDurationBarIntent.putExtra("duration", duration);
                    updateDurationBarIntent.putExtra("durationString", this.parseDurationStringFromMillis(duration));
                    updateDurationBarIntent.putExtra("current", this.millis);
                    updateDurationBarIntent.putExtra("currentString", this.parseDurationStringFromMillis(this.millis));
                    MusicPlayer.this.sendBroadcast(updateDurationBarIntent);
                    if ((this.millis += 1000) < duration) {
                        durationBarUpdateHandler.postDelayed(this, 1000);
                    }
                }
            }
        }
    }

}
