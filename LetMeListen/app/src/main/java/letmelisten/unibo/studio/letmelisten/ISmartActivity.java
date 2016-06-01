package letmelisten.unibo.studio.letmelisten;

import android.os.IBinder;
import android.support.v4.app.Fragment;

/**
 * Created by doomdiskday on 25/04/2016.
 */
public interface ISmartActivity {

    boolean isInForeground();

    Fragment getCurrentFragment();

}
