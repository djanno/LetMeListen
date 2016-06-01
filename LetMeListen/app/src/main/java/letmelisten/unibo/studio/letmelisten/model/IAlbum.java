package letmelisten.unibo.studio.letmelisten.model;

import java.util.List;

/**
 * Created by Federico on 04/04/2016.
 */
public interface IAlbum {

    String getAlbumName();

    List<ITrack> getAlbumTracks();

    boolean addTrack(final ITrack track);

}
