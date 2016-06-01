package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.model.Album;
import letmelisten.unibo.studio.letmelisten.model.Artist;
import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.music_library.MusicLibrary;

/**
 * Created by doomdiskday on 07/04/2016.
 */
public class AlbumFragment extends Fragment {

    private ListView albumList;
    private View rootView;
    private OnAlbumClickListener listener;
    private AlbumAdapter adapter;
    private List<IAlbum> albums;
    private SearchView search;


    public interface OnAlbumClickListener{
        public void onAlbumClick(IAlbum album);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof OnAlbumClickListener){
            this.listener = (OnAlbumClickListener) activity;
        }
    }

    public static AlbumFragment newInstanceFromArtist(IArtist artist){
        AlbumFragment albumFragment = new AlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("ArtistSelected", (Serializable) artist);
        albumFragment.setArguments(bundle);
        return albumFragment;
    }

    public static AlbumFragment newInstance(){
        AlbumFragment albumFragment = new AlbumFragment();
        return  albumFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.albums = new ArrayList<>(MusicLibrary.getInstance().getAlbumList());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_album,container,false);
        albumList = (ListView) rootView.findViewById(R.id.albumListView);
        Bundle bundle = getArguments();
        if(bundle != null){
            Artist artist = (Artist) bundle.getSerializable("ArtistSelected");
            this.albums = artist.getAlbums();
        }

        this.adapter = new AlbumAdapter(getActivity(), this.albums);

        albumList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        albumList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onAlbumClick(adapter.getItem(position));
            }
        });

        search = (SearchView) rootView.findViewById(R.id.searchField);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


    class AlbumAdapter extends BaseAdapter implements Filterable {

        private Activity context;
        private LayoutInflater inflater;
        private List<IAlbum> albums;
        private List<IAlbum> temporaryList;

        public AlbumAdapter(Activity context, List<IAlbum> albums){
            this.context = context;
            this.albums = albums;
            this.inflater = LayoutInflater.from(context);
            this.temporaryList = albums;
        }
        @Override
        public int getCount() {
            return temporaryList.size();
        }

        @Override
        public IAlbum getItem(int position) {
            return temporaryList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = inflater.inflate(R.layout.album_detail, parent,false);
            }
            ((TextView) convertView.findViewById(R.id.albumNameField)).setText(getItem(position).getAlbumName());
            ((TextView) convertView.findViewById(R.id.albumNameField)).setSelected(true);

            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    ArrayList<IAlbum> FilteredResults = new ArrayList<IAlbum>();
                    if (constraint == null || constraint.length() == 0) {
                        results.values = albums;
                        results.count = albums.size();
                    } else {
                        for (int i = 0; i < albums.size(); i++) {
                            IAlbum toFilter = albums.get(i);
                            if (toFilter.getAlbumName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
                    temporaryList = (List<IAlbum>) results.values;
                    adapter.notifyDataSetChanged();
                }
            };
            return filter;
        }
    }
}
