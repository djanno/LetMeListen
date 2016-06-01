package letmelisten.unibo.studio.letmelisten.music_retriever;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.model.Album;
import letmelisten.unibo.studio.letmelisten.model.Artist;
import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.music_library.MusicLibrary;
import letmelisten.unibo.studio.letmelisten.model.Track;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class MusicRetriever {

    ContentResolver mContentResolver;
    // the items (songs) we have queried
    List<ITrack> trackList = new ArrayList<>();
    List<IArtist> artistsList = new ArrayList<>();
    List<IAlbum> albumLists = new ArrayList<>();

    public MusicRetriever(ContentResolver cr) {
        mContentResolver = cr;
    }
    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {
        final Uri uriTrack = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Uri uriArtist = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        final Uri uriAlbum = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        final Cursor curTrack = mContentResolver.query(uriTrack, null, MediaStore.Audio.Media.IS_MUSIC + " = 1",
                null, null);

        final Cursor curArtist = mContentResolver.query(uriArtist, null, null, null,null);

        final Cursor curAlbum = mContentResolver.query(uriAlbum, null, null, null, null);

        if (curTrack == null ) {
            // Query failed...
            return;
        }
        if (!curTrack.moveToFirst()) {
            // Nothing to query. There is no music on the device.
            return;
        }

        //only use path column, and remap it to absolute path, then use it to build the track
        final int titleColumn = curTrack.getColumnIndex(MediaStore.Audio.Media.TITLE);
        final int trackArtistColumn = curTrack.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        final int trackAlbumColumn = curTrack.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        final int durationColumn = curTrack.getColumnIndex(MediaStore.Audio.Media.DURATION);
        final int pathColumn = curTrack.getColumnIndex(MediaStore.Audio.Media.DATA);

        //artist index
        final int artistColumn = curArtist.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST);
        //only use the artist name

        //album indexes
        final int albumColumn = curAlbum.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM);
        final int albumArtistColumn = curAlbum.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST);

        curArtist.moveToFirst();

        do {
            this.artistsList.add(new Artist(curArtist.getString(artistColumn)));
        } while(curArtist.moveToNext());

        curArtist.close();

        curAlbum.moveToFirst();

        do {
            final Album toAdd = new Album(curAlbum.getString(albumColumn));
            this.albumLists.add(toAdd);
            for (final IArtist art : this.artistsList) {
                if(art.getName().equals(curAlbum.getString(albumArtistColumn))){
                    art.addAlbum(toAdd);
                }
            }
        } while(curAlbum.moveToNext());

        curAlbum.close();

        curTrack.moveToFirst();
        // add each song from SDcard to trackList
        do {
            final Track toAdd = new Track(curTrack.getString(titleColumn), curTrack.getString(trackArtistColumn),
                    curTrack.getString(trackAlbumColumn), (int) curTrack.getLong(durationColumn), curTrack.getString(pathColumn));
            this.trackList.add(toAdd);
            for (final IAlbum a : this.albumLists) {
                if(a.getAlbumName().equals(curTrack.getString(trackAlbumColumn))){
                    a.addTrack(toAdd);
                }
            }
        } while (curTrack.moveToNext());

        curTrack.close();

        MusicLibrary.getInstance().setAlbumList(this.albumLists);
        MusicLibrary.getInstance().setArtistList(this.artistsList);
        MusicLibrary.getInstance().setTrackList(this.trackList);
    }

}


