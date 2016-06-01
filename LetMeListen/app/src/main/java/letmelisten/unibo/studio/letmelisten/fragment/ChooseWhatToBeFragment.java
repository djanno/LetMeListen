package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.SmartActivity;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class ChooseWhatToBeFragment extends Fragment {

    public interface ChooseWhatToBeFragmentInteractionListener {
        void startMusicPickerActivity();

        void loadFindStationFragment();
    }

    private ChooseWhatToBeFragmentInteractionListener listener;

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof ChooseWhatToBeFragmentInteractionListener) {
            this.listener = (ChooseWhatToBeFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ChooseWhatToBeFragmentInteractionListener.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.choose_what_to_be_fragment_layout, container, false);

        root.findViewById(R.id.be_a_station_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.startMusicPickerActivity();
            }
        });

        root.findViewById(R.id.be_a_listener_frame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.loadFindStationFragment();
            }
        });


        return root;
    }

}
