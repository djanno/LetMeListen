package letmelisten.unibo.studio.letmelisten.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import letmelisten.unibo.studio.letmelisten.ListenerActivity;
import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.StationActivity;
import letmelisten.unibo.studio.letmelisten.music_player.MusicPlayer;

/**
 * Created by Federico on 15/05/2016.
 */
public class DurationBarFragment extends Fragment {

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private SeekBar durationBar;
    private TextView currentLabel;
    private TextView durationLabel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.broadcastReceiver == null) {
            this.broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, final Intent intent) {
                    if(intent.getAction().equals(MusicPlayer.PLAYBACK_COMPLETED) && DurationBarFragment.this.getView() != null) {
                        DurationBarFragment.this.getView().setVisibility(View.GONE);

                    }
                    else if(intent.getAction().equals(MusicPlayer.UPDATE_DURATION_BAR) && DurationBarFragment.this.getView() != null) {
                        DurationBarFragment.this.getView().setVisibility(View.VISIBLE);
                        final int duration = intent.getIntExtra("duration", -1);
                        final String durationString = intent.getStringExtra("durationString");
                        final int current = intent.getIntExtra("current", -1);
                        final String currentString = intent.getStringExtra("currentString");
                        currentLabel.setText(currentString);
                        durationLabel.setText(durationString);
                        durationBar.setMax(duration);
                        durationBar.setProgress(current);
                    }
                }
            };
            this.intentFilter = new IntentFilter();
            this.intentFilter.addAction(MusicPlayer.PLAYBACK_COMPLETED);
            this.intentFilter.addAction(MusicPlayer.UPDATE_DURATION_BAR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity().registerReceiver(this.broadcastReceiver, this.intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getActivity().unregisterReceiver(this.broadcastReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.duration_bar_layout, container, false);
        this.durationBar = (SeekBar) root.findViewById(R.id.durationBar);
        this.durationBar.setEnabled(false);
        this.currentLabel = (TextView) root.findViewById(R.id.nowLabel);
        this.durationLabel = (TextView) root.findViewById(R.id.durationLabel);
        if(this.getActivity() instanceof ListenerActivity) {
            this.durationBar.setProgressDrawable(this.getResources().getDrawable(R.drawable.progress_seekbar_listener));
            this.durationBar.setThumb(this.getResources().getDrawable(R.drawable.thumb_seekbar_listener));
        }else if(this.getActivity() instanceof StationActivity) {
            this.durationBar.setProgressDrawable(this.getResources().getDrawable(R.drawable.progress_seekbar_station));
            this.durationBar.setThumb(this.getResources().getDrawable(R.drawable.thumb_seekbar_station));
        }
        root.setVisibility(View.GONE);
        return root;
    }
}
