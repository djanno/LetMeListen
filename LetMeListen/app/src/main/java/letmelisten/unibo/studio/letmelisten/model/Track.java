package letmelisten.unibo.studio.letmelisten.model;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Federico on 04/04/2016.
 */
public class Track implements ITrack, Serializable {

    private final String title;
    private final String artistName;
    private final String albumName;
    private final int duration;

    private String filePath;
    private String fileType;
    private File mediaFile;

    private boolean playable;
    private boolean playing;
    private boolean paused;

    private void setFileTypeFromPath() {
        this.fileType = "";
        for (int i = this.filePath.lastIndexOf('.'); i < this.filePath.length(); i++) {
            this.fileType.concat("" + this.filePath.charAt(i));
        }
    }

    public Track(final String title, final String artistName, final String albumName, final int duration,
                 final String filePath) {
        this.title = title;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.filePath = filePath;
        this.setFileTypeFromPath();
    }

    public Track(final ITrack track) {
        this.title = track.getTitle();
        this.artistName = track.getArtistName();
        this.albumName = track.getAlbumName();
        this.duration = track.getDuration();
        this.filePath = track.getFilePath();
        this.setMediaFile();
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public String getArtistName() {
        return this.artistName;
    }

    @Override
    public String getAlbumName() {
        return this.albumName;
    }

    @Override
    public int getDuration() {
        return this.duration;
    }

    @Override
    public String getFilePath() {
        return this.filePath;
    }

    @Override
    public String getFileType() {
        return this.fileType;
    }

    @Override
    public File getMediaFile() {
        return this.mediaFile;
    }

    @Override
    public boolean isPlayable() {
        return this.playable;
    }

    @Override
    public boolean isPlaying() {
        return this.playing;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void setPlayable(final boolean playable) {
        this.playable = playable;
    }

    @Override
    public void setFilePath(final String filePath) {
        this.filePath = filePath;
        this.setFileTypeFromPath();
    }

    @Override
    public void setMediaFile() {
        this.mediaFile = new File(this.filePath);
    }

    @Override
    public void dropMediaFile() {
        this.mediaFile = null;
    }

    @Override
    public void play() {
        this.playing = true;
    }

    @Override
    public void pause() {
        this.paused = true;
    }

    @Override
    public void resume() {
        this.paused = false;
    }

    @Override
    public void stop() {
        this.playing = false;
        this.paused = false;
    }
}
