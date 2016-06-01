package letmelisten.unibo.studio.letmelisten.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Federico on 04/04/2016.
 */
public interface IArtist {

    String getName();

    List<IAlbum> getAlbums();

    boolean addAlbum(final IAlbum album);

}
