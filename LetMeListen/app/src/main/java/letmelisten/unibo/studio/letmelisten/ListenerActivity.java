package letmelisten.unibo.studio.letmelisten;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import letmelisten.unibo.studio.letmelisten.fragment.DurationBarFragment;
import letmelisten.unibo.studio.letmelisten.fragment.StationListenerFragment;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public class ListenerActivity extends SmartActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.listener_activity_layout);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue_accent)));
        this.replaceFragment(R.id.listenerActivityMainLayout, new StationListenerFragment(), false);
        this.replaceFragment(R.id.listenerActivitySecondaryLayout, new DurationBarFragment(), false);
    }

}
