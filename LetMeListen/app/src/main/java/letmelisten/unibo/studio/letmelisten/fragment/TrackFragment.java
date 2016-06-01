package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.model.Album;
import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.ITrack;
import letmelisten.unibo.studio.letmelisten.model.music_library.MusicLibrary;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;


/**
 * Created by doomdiskday on 07/04/2016.
 */
public class TrackFragment extends Fragment {
    private View rootView;
    private TrackAdapter adapter;
    private List<ITrack> tracksToList;

    public static TrackFragment newInstanceFromAlbum(final IAlbum album){
        final TrackFragment trackFragment = new TrackFragment();
        final Bundle bundle = new Bundle();
        bundle.putSerializable("AlbumSelected", (Serializable) album);
        trackFragment.setArguments(bundle);
        return trackFragment;
    }

    public static TrackFragment newInstance(){
        final TrackFragment trackFragment = new TrackFragment();
        return trackFragment;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater,final ViewGroup container,final Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_track, container,false);
        final Bundle bundle = getArguments();

        if(bundle != null){
            Album albumSelected = (Album) bundle.getSerializable("AlbumSelected");
            this.tracksToList = albumSelected.getAlbumTracks();
        }else{
            this.tracksToList = MusicLibrary.getInstance().getTrackList();
        }

        this.adapter = new TrackAdapter(getActivity(), this.tracksToList);

        final ListView trackList = (ListView) rootView.findViewById(R.id.trackListView);
        trackList.setAdapter(adapter);

        final SearchView search = (SearchView) rootView.findViewById(R.id.searchField);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                search.setIconified(false);
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });



        return rootView;
    }

    class TrackAdapter extends BaseAdapter implements Filterable
    {
        private LayoutInflater inflater;
        private List<ITrack> trackList;
        private List<ITrack> temporaryList;


        public TrackAdapter(final Activity context,final List<ITrack> trackList){
            this.trackList = trackList;
            this.inflater = LayoutInflater.from(context);
            this.temporaryList = this.trackList;
        }

        @Override
        public int getCount() {
            return this.temporaryList.size();
        }

        @Override
        public ITrack getItem(final int position) {
            return this.temporaryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position,View convertView,final ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(R.layout.track_detail, parent, false);
            }else{
                ((CheckBox) convertView.findViewById(R.id.addedCheckBox)).setOnCheckedChangeListener(null);
            }


            final CheckBox added = (CheckBox) convertView.findViewById(R.id.addedCheckBox);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(added.isChecked()){
                        added.setChecked(false);
                    }else{
                        added.setChecked(true);
                    }
                }
            });
            ((TextView) convertView.findViewById(R.id.titleField)).setText(getItem(position).getTitle());
            (convertView.findViewById(R.id.titleField)).setSelected(true);

            String durationText = getDurationTxt(getItem(position).getDuration());
            ((TextView) convertView.findViewById(R.id.durationField)).setText(durationText);

            if(ServerModel.getInstance().getPlaylist().contains(getItem(position))){
                added.setChecked(true);
            }else{
                added.setChecked(false);
            }

            added.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (ServerModel.getInstance().getPlaylist().size() == 10) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.max_songs_selected_message), Toast.LENGTH_LONG).show();
                        if(!isChecked){
                            ServerModel.getInstance().removeTrackFromPlayList(getItem(position));
                        }else{
                            added.setChecked(false);
                        }

                    } else {
                        if (isChecked) {
                            ServerModel.getInstance().addTrackToPlaylist(getItem(position));
                        } else {
                            ServerModel.getInstance().removeTrackFromPlayList(getItem(position));
                        }
                    }
                }
            });
            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<ITrack> FilteredResults = new ArrayList<ITrack>();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = trackList;
                        results.count = trackList.size();
                    } else {
                        for (int i = 0; i < trackList.size(); i++) {
                            ITrack toFilter = trackList.get(i);
                            if (toFilter.getAlbumName().toLowerCase().contains(constraint.toString().toLowerCase())
                                    || toFilter.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                                    || toFilter.getArtistName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                FilteredResults.add(toFilter);
                            }
                        }
                        results.values = FilteredResults;
                        results.count = FilteredResults.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    temporaryList = (List<ITrack>) results.values;
                    adapter.notifyDataSetChanged();
                }
            };
            return filter;
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
