package letmelisten.unibo.studio.letmelisten.music_retriever.task;

import android.os.AsyncTask;

import letmelisten.unibo.studio.letmelisten.music_retriever.MusicRetriever;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class SearchMusicTask extends AsyncTask<Void, Void, Void> {
    private MusicRetriever retriever;
    private MusicRetrieverPreparedListener listener;

    public interface MusicRetrieverPreparedListener {
        public void onMusicRetrieverPrepared();
        public void stopSpinner();
    }

    public SearchMusicTask(MusicRetriever retriever, MusicRetrieverPreparedListener listener ){
        this.retriever = retriever;
        this.listener = listener;
    }


    @Override
    protected Void doInBackground(Void... params) {
        this.retriever.prepare();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        listener.stopSpinner();
        listener.onMusicRetrieverPrepared();
    }
}
