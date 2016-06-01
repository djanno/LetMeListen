package letmelisten.unibo.studio.letmelisten.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 04/04/2016.
 */
public class Artist implements IArtist, Serializable {

    private final String name;
    private final List<IAlbum> albums;

    public Artist(final String name) {
        this.name = name;
        this.albums = new ArrayList<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<IAlbum> getAlbums() {
        return this.albums;
    }

    @Override
    public boolean addAlbum(final IAlbum album) {
        return this.albums.add(album);
    }

}
