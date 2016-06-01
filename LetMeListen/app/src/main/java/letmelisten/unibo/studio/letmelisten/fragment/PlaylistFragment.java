package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.StationActivity;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;


/**
 * Created by doomdiskday on 14/04/2016.
 */
public class PlaylistFragment extends Fragment {

    private static final int MAX_SONGS = 20;

    private View rootView;
    private PlayListAdapter adapter;
    private ListView trackList;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(this.adapter == null) {
            this.adapter = new PlayListAdapter(getActivity(), ServerModel.getInstance().getPlaylistReference());
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,final Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_playlist_done, container, false);
        this.adapter.notifyDataSetChanged();
        this.trackList = (ListView) rootView.findViewById(R.id.playlistListView);
        this.trackList.setAdapter(adapter);


        return this.rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.tick:
                if(ServerModel.getInstance().getPlaylist().size() == 0){
                    Toast.makeText(getActivity(), getResources().getString(R.string.select_songs_tips_message) + MAX_SONGS + getResources().getString(R.string.song_message), Toast.LENGTH_LONG).show();
                }else if(ServerModel.getInstance().getPlaylist().size() > MAX_SONGS){
                    Toast.makeText(getActivity(), getResources().getString(R.string.max_songs_selected_message), Toast.LENGTH_LONG).show();
                }else{
                    final Intent forStationActivity = new Intent(this.getActivity(), StationActivity.class);
                    this.getActivity().startActivity(forStationActivity);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    class PlayListAdapter extends BaseAdapter{
        private List<ITrack> trackList;
        private LayoutInflater inflater;

        public PlayListAdapter(final Activity context,final List<ITrack> trackList){
            this.trackList = trackList;
            this.inflater = LayoutInflater.from(context);
        }


        @Override
        public int getCount() {
            return trackList.size();
        }

        @Override
        public ITrack getItem(final int position) {
            return this.trackList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @Override
        public View getView(final int position,View convertView,final ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(R.layout.track_in_playlist_detail, parent, false);
            }

            ((TextView) convertView.findViewById(R.id.titleField)).setText(getItem(position).getTitle());
            (convertView.findViewById(R.id.titleField)).setSelected(true);


            String durationText = getDurationTxt(getItem(position).getDuration());
            ((TextView) convertView.findViewById(R.id.durationField)).setText(durationText);

            (convertView.findViewById(R.id.removeTrackButton)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ServerModel.getInstance().removeTrackFromPlayList(getItem(position));
                    notifyDataSetChanged();
                    if(ServerModel.getInstance().getPlaylist().size() == 0){
                        getActivity().onBackPressed();
                    }
                }
            });

            return convertView;
        }

        private String getDurationTxt(long duration){
            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );
        }
    }
}
