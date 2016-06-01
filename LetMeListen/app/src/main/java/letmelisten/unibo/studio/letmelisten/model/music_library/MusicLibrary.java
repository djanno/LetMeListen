package letmelisten.unibo.studio.letmelisten.model.music_library;

import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class MusicLibrary implements IMusicLibrary {

    private List<ITrack> trackList;
    private List<IAlbum> albumList;
    private List<IArtist> artistList;

    private static MusicLibrary library= null;

    private MusicLibrary(){
        this.trackList = new ArrayList<>();
        this.albumList = new ArrayList<>();
        this.artistList = new ArrayList<>();
    }

    public static MusicLibrary getInstance(){
        if(library == null){
            library = new MusicLibrary();
        }
        return library;
    }

    @Override
    public void setTrackList(List<ITrack> trackList) {
        this.trackList = trackList;
    }

    @Override
    public void setAlbumList(List<IAlbum> albumList) {
        this.albumList = albumList;
    }

    @Override
    public void setArtistList(List<IArtist> artistList) {
        this.artistList = artistList;
    }

    @Override
    public List<ITrack> getTrackList() {
        return trackList;
    }

    @Override
    public List<IAlbum> getAlbumList() {
        return albumList;
    }

    @Override
    public List<IArtist> getArtistList() {
        return artistList;
    }
}