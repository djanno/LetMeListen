package letmelisten.unibo.studio.letmelisten.music_player.binder;

import android.os.IBinder;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by Federico on 08/05/2016.
 */
public interface IMusicPlayerBinder extends IBinder {

    void makeEnqueueRequest(final ITrack track);

    void makePlayRequest(final ITrack track);

    void makeResumePlayingRequest();

    void makePausePlayerRequest();

}
