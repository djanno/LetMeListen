package letmelisten.unibo.studio.letmelisten.music_player;

import letmelisten.unibo.studio.letmelisten.model.ITrack;

/**
 * Created by doomdiskday on 28/04/2016.
 */
public interface IMusicPlayer {

    void enqueue(final ITrack track);

    void play(final ITrack track);

    void resume();

    void pause();

}
