package letmelisten.unibo.studio.letmelisten.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.zip.Inflater;

import letmelisten.unibo.studio.letmelisten.R;
import letmelisten.unibo.studio.letmelisten.model.server.ServerModel;

/**
 * Created by doomdiskday on 13/04/2016.
 */
public class ButtonMenuFragment extends Fragment {

    private static final int ALBUM = 1;
    private static final int ARTIST = 0;
    private static final int TRACK = 2;

    private View rootView;
    private Button artistButton;
    private Button albumButton;
    private Button trackButton;
    private TextView info;
    private OnMenuClickListener listener;
    private ArrayList<String> infos;

    public interface OnMenuClickListener{
        /*The activity will show the right fragment  */
        void onMenuClick(final int itemClicked);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if(activity instanceof OnMenuClickListener){
            this.listener = (OnMenuClickListener) activity;
        }
    }



    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.infos = new ArrayList<>();
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,final ViewGroup container,final Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_button_menu, container, false);

        this.artistButton = (Button) this.rootView.findViewById(R.id.artistButton);
        this.albumButton = (Button) this.rootView.findViewById(R.id.albumButton);
        this.trackButton = (Button) this.rootView.findViewById(R.id.trackButton);
        this.info = (TextView) this.rootView.findViewById(R.id.infoField);

        viewInfo(false, "", TRACK);
        this.infos.add("");

        this.albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedButton(true, albumButton);
                setSelectedButton(false, artistButton);
                setSelectedButton(false, trackButton);
                infos = new ArrayList<String>();
                infos.add("");
                listener.onMenuClick(ALBUM);

            }
        });

        this.artistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedButton(true, artistButton);
                setSelectedButton(false,albumButton);
                setSelectedButton(false, trackButton);
                infos = new ArrayList<String>();
                infos.add("");
                listener.onMenuClick(ARTIST);
            }
        });

        this.trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedButton(true, trackButton);
                setSelectedButton(false, artistButton);
                setSelectedButton(false, albumButton);
                infos = new ArrayList<String>();
                infos.add("");
                listener.onMenuClick(TRACK);
            }
        });

        return this.rootView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar, menu);
    }

    private void setSelectedButton(final boolean setSelected,final Button toChange){
        if(setSelected){
            toChange.setBackgroundResource(R.drawable.button_selected);
            toChange.setTextColor(Color.WHITE);
            viewInfo(false, "", TRACK);
        }else{
            toChange.setBackgroundResource(R.drawable.button_not_selected);
            toChange.setTextColor(Color.BLACK);
        }

    }

    public void viewInfo(final boolean view,final String toView, int elementSelected){
        final ImageView infoView = (ImageView) rootView.findViewById(R.id.imageInfo);
        if(!view || toView.equals("")){
            this.info.setVisibility(View.GONE);
        } else {
            if(elementSelected ==  ALBUM){
                infoView.setBackground(getResources().getDrawable(R.drawable.albumicon));
            }else if(elementSelected == ARTIST){
                infoView.setBackground(getResources().getDrawable(R.drawable.artisticon7));
            }
            this.info.setVisibility(View.VISIBLE);
            this.info.setText(toView);
        }
    }

    public ArrayList<String> getInfos(){
        return this.infos;
    }

    public TextView getInfoView(){
        return this.info;
    }
}
