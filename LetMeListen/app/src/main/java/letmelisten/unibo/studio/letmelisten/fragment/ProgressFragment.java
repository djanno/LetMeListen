package letmelisten.unibo.studio.letmelisten.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import letmelisten.unibo.studio.letmelisten.R;

/**
 * Created by doomdiskday on 13/04/2016.
 */
public class ProgressFragment extends Fragment {

    private View rootView;
    private ProgressBar progressBar;
    private TextView loadingTracksTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_retrieving_music, container, false);
        this.progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        this.loadingTracksTextView = (TextView) rootView.findViewById(R.id.loadinTextView);
        return this.rootView;
    }

    public void stopLoading(){
        this.progressBar.setVisibility(View.GONE);
        this.loadingTracksTextView.setVisibility(View.GONE);
    }
}
