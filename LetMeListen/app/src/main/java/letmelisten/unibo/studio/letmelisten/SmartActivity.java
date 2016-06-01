package letmelisten.unibo.studio.letmelisten;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public abstract class SmartActivity extends AppCompatActivity implements ISmartActivity {

    private boolean inForeground;
    private Fragment currentFragment;

    protected void replaceFragment(final int containerResId, final Fragment fragment, final boolean addPreviousToBackStack) {
        final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
        transaction.replace(containerResId, fragment);
        if(addPreviousToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        this.currentFragment = fragment;
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.inForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.inForeground = true;
    }

    @Override
    public boolean isInForeground() {
        return this.inForeground;
    }

    @Override
    public Fragment getCurrentFragment() {
        return this.currentFragment;
    }
}
