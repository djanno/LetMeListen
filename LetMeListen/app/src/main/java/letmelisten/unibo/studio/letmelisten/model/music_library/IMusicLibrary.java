package letmelisten.unibo.studio.letmelisten.model.music_library;

import java.util.List;

import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public interface IMusicLibrary {

    public void setTrackList(List<ITrack> trackList);

    public void setAlbumList(List<IAlbum> albumList);

    public void setArtistList(List<IArtist> artistList);

    public List<ITrack> getTrackList();

    public List<IAlbum> getAlbumList();

    public List<IArtist> getArtistList();

}
