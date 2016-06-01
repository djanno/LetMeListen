package letmelisten.unibo.studio.letmelisten.music_player.binder;

import android.os.Binder;

import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.music_player.IMusicPlayer;

/**
 * Created by Federico on 08/05/2016.
 */
public class MusicPlayerBinder extends Binder implements IMusicPlayerBinder {

    private final IMusicPlayer service;

    public MusicPlayerBinder(final IMusicPlayer service) {
        this.service = service;
    }

    @Override
    public void makeEnqueueRequest(final ITrack track) {
        this.service.enqueue(track);
    }

    @Override
    public void makePlayRequest(final ITrack track) {
        this.service.play(track);
    }

    @Override
    public void makeResumePlayingRequest() {
        this.service.resume();
    }

    @Override
    public void makePausePlayerRequest() {
        this.service.pause();
    }

}
