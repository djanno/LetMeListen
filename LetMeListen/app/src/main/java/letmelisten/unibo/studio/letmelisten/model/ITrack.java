package letmelisten.unibo.studio.letmelisten.model;

import java.io.File;

/**
 * Created by Federico on 04/04/2016.
 */
public interface ITrack {

    String getTitle();

    String getArtistName();

    String getAlbumName();

    int getDuration();

    String getFilePath();

    String getFileType();

    File getMediaFile();

    boolean isPlayable();

    boolean isPlaying();

    boolean isPaused();

    void setPlayable(final boolean playable);

    void setFilePath(final String filePath);

    /* based on the file path */
    void setMediaFile();

    void dropMediaFile();

    void play();

    void pause();

    void resume();

    void stop();

}
