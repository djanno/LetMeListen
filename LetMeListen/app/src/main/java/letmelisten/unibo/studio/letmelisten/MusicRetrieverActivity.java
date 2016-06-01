package letmelisten.unibo.studio.letmelisten;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import letmelisten.unibo.studio.letmelisten.fragment.AlbumFragment;
import letmelisten.unibo.studio.letmelisten.fragment.ArtistFragment;
import letmelisten.unibo.studio.letmelisten.fragment.ButtonMenuFragment;
import letmelisten.unibo.studio.letmelisten.fragment.PlaylistFragment;
import letmelisten.unibo.studio.letmelisten.fragment.ProgressFragment;
import letmelisten.unibo.studio.letmelisten.fragment.TrackFragment;
import letmelisten.unibo.studio.letmelisten.model.IAlbum;
import letmelisten.unibo.studio.letmelisten.model.IArtist;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;
import letmelisten.unibo.studio.letmelisten.music_retriever.MusicRetriever;
import letmelisten.unibo.studio.letmelisten.music_retriever.task.SearchMusicTask;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class MusicRetrieverActivity extends SmartActivity implements AlbumFragment.OnAlbumClickListener,
        SearchMusicTask.MusicRetrieverPreparedListener, ArtistFragment.OnArtistClickListener,
        ButtonMenuFragment.OnMenuClickListener {

    private static final int MAX_SONGS = 20;
    private static final int ARTIST = 0;
    private static final int ALBUM = 1;
    private static final int TRACK = 2;

    private MusicRetriever retriever;
    private ButtonMenuFragment menuFragment;
    private ProgressFragment progressFragment;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_retriever_activity_layout);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.green_accent)));


        this.progressFragment = new ProgressFragment();
        this.fragmentManager = getSupportFragmentManager();
        this.fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, progressFragment).commit();

        this.retriever = new MusicRetriever(getContentResolver());
        final SearchMusicTask searchMusic = new SearchMusicTask(this.retriever, MusicRetrieverActivity.this);
        searchMusic.execute();
        ServerModel.getInstance().emptyPlaylist();

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.tick:
                if(ServerModel.getInstance().getPlaylist().size() == 0){
                    Toast.makeText(this, getResources().getString(R.string.select_songs_tips_message) + MAX_SONGS + getResources().getString(R.string.song_message), Toast.LENGTH_LONG).show();
                    return true;
                }
                if(ServerModel.getInstance().getPlaylist().size() > MAX_SONGS){
                    Toast.makeText(this, getResources().getString(R.string.max_songs_selected_message), Toast.LENGTH_LONG).show();
                    return true;
                }
                else {
                    if(this.fragmentManager.findFragmentById(R.id.main_container) instanceof PlaylistFragment) {
                        final Intent forStationActivity = new Intent(this, StationActivity.class);
                        this.startActivity(forStationActivity);
                    }
                    else {
                        this.replaceFragment(new PlaylistFragment(), R.id.main_container, true);
                    }
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMenuClick(final int itemClicked) {
        clearBackStack();
        switch (itemClicked){
            case ARTIST:
                replaceFragment(ArtistFragment.newInstance(), R.id.list_container, false);
                this.showInfo(false);
                break;
            case ALBUM:
                replaceFragment(AlbumFragment.newInstance(), R.id.list_container, false);
                this.showInfo(false);
                break;
            case TRACK:
                replaceFragment(TrackFragment.newInstance(), R.id.list_container,false);
                this.showInfo(false);
                break;
        }


    }

    @Override
    public void onAlbumClick(IAlbum album) {
        final TrackFragment trackFragment = TrackFragment.newInstanceFromAlbum(album);
        this.replaceFragment(trackFragment, R.id.list_container, true);
        this.menuFragment.viewInfo(true, album.getAlbumName(), ALBUM);
        this.menuFragment.getInfos().add(this.menuFragment.getInfoView().getText().toString());
        this.showInfo(true);
    }

    @Override
    public void onMusicRetrieverPrepared() {
        final ButtonMenuFragment menuFragment = new ButtonMenuFragment();
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.remove(this.progressFragment);
        transaction.add(R.id.menu_container, menuFragment);
        transaction.add(R.id.list_container, new ArtistFragment());
        transaction.commit();

        this.menuFragment = menuFragment;
    }

    @Override
    public void stopSpinner() {
        this.progressFragment.stopLoading();
    }


    @Override
    public void onBackPressed() {
        if(this.fragmentManager.findFragmentById(R.id.main_container) instanceof ProgressFragment){
            return;
        }
        else if(this.fragmentManager.findFragmentById(R.id.main_container) instanceof PlaylistFragment){
            this.fragmentManager.popBackStack();
        }
        else if(this.fragmentManager.getBackStackEntryCount()>0){
            final Fragment fragment =  this.fragmentManager.findFragmentById(R.id.list_container);
            int elementSelected = TRACK;
            this.fragmentManager.popBackStack();
            this.menuFragment.getInfos().remove(this.menuFragment.getInfos().size() - 1);
            if(fragment instanceof TrackFragment){
                elementSelected = ARTIST;
            }
            this.menuFragment.viewInfo(true, this.menuFragment.getInfos().get(this.menuFragment.getInfos().size() - 1), elementSelected);
            if(this.menuFragment.getInfos().size() == 1){
               this.showInfo(false);
            }
        }
        else{
            super.onBackPressed();
        }

    }

    @Override
    public void onArtistClick(IArtist artist) {
        final AlbumFragment albumFragment = AlbumFragment.newInstanceFromArtist(artist);
        this.replaceFragment(albumFragment, R.id.list_container, true);
        this.menuFragment.viewInfo(true, artist.getName(), ARTIST);
        this.menuFragment.getInfos().add(this.menuFragment.getInfoView().getText().toString());
        this.showInfo(true);
    }

    private void clearBackStack(){
        for(int i = 0; i < this.fragmentManager.getBackStackEntryCount(); i++){
            this.fragmentManager.popBackStack();
        }
    }

    private void replaceFragment(final Fragment toAdd,final int container, final boolean addToBackStack){
        final FragmentTransaction transaction = this.fragmentManager.beginTransaction();
        transaction.replace(container, toAdd);
        if(addToBackStack){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void showInfo(boolean show){
        final LinearLayout listContainer = (LinearLayout) findViewById(R.id.list_container);
        final LinearLayout menuContainer = (LinearLayout) findViewById(R.id.menu_container);
        final LinearLayout.LayoutParams lpContainer;
        final LinearLayout.LayoutParams lpMenu;

        if(show){
            lpContainer = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 8f);
            lpMenu = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2f);
        }else{
            lpContainer = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 9f);
            lpMenu = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        }

        listContainer.setLayoutParams(lpContainer);
        menuContainer.setLayoutParams(lpMenu);
    }
}

