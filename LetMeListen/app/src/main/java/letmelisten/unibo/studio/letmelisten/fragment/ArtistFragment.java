package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.music_library.MusicLibrary;


/**
 * Created by doomdiskday on 07/04/2016.
 */
public class ArtistFragment extends Fragment {

    private View rootView;
    private OnArtistClickListener listener;
    private ArtistAdapter adapter;
    private List<IArtist> artists;

    public interface OnArtistClickListener{
        /*On artist click the activity will show the albums of the selected artist*/
        void onArtistClick(final IArtist albums);
    }

    public static ArtistFragment newInstance(){
        final ArtistFragment artistFragment = new ArtistFragment();
        return artistFragment;
    }
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artists = new ArrayList<>(MusicLibrary.getInstance().getArtistList());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,final  Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_artist, container, false);
        final ListView artistView = (ListView)rootView.findViewById(R.id.artistsView);
        final SearchView search = (SearchView) rootView.findViewById(R.id.searchField);

        if(this.adapter == null) {
            this.adapter = new ArtistAdapter(getActivity(), artists);
        }
        artistView.setAdapter(adapter);
        this.adapter.notifyDataSetChanged();

        artistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                listener.onArtistClick(adapter.getItem(position));
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                search.setIconified(false);
            }
        });


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if(activity instanceof OnArtistClickListener){
            this.listener = (OnArtistClickListener) activity;
        }
    }

    class ArtistAdapter extends BaseAdapter implements Filterable{
        private List<IArtist> artistList;
        private LayoutInflater inflater;
        private List<IArtist> temporaryList;

        public ArtistAdapter(Activity context, List<IArtist> artists){
            this.inflater = LayoutInflater.from(context);
            this.artistList = artists;
            this.temporaryList = artistList;
        }


        @Override
        public int getCount() {
            return this.temporaryList.size();
        }

        @Override
        public IArtist getItem(int position) {
            return this.temporaryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(R.layout.artist_detail, parent,false);
            }

            ((TextView)convertView.findViewById(R.id.artistNameField)).setText(getItem(position).getName());
            (convertView.findViewById(R.id.artistNameField)).setSelected(true);
            ((TextView)convertView.findViewById(R.id.numberOfAlbumsField)).setText(getResources().getString(R.string.album_message)+(getItem(position).getAlbums().size()));

            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<IArtist> FilteredResults = new ArrayList<IArtist>();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = artistList;
                        results.count = artistList.size();
                    } else {
                        for (int i = 0; i < artistList.size(); i++) {
                            IArtist toFilter = artistList.get(i);
                            if (toFilter.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
                    temporaryList = (List<IArtist>) results.values;
                    adapter.notifyDataSetChanged();
                }
            };
            return filter;
        }
    }
}
