package letmelisten.unibo.studio.letmelisten.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Federico on 04/04/2016.
 */
public class Album implements IAlbum, Serializable {

    private final String name;
    private final List<ITrack> tracks;

    public Album(final String name) {
        this.name = name;
        this.tracks = new ArrayList<>();
    }

    @Override
    public String getAlbumName() {
        return this.name;
    }

    @Override
    public List<ITrack> getAlbumTracks() {
        return this.tracks;
    }

    @Override
    public boolean addTrack(final ITrack track) {
        return this.tracks.add(track);
    }
}
