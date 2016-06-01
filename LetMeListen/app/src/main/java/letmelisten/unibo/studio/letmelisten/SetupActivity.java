package letmelisten.unibo.studio.letmelisten;

import android.Manifest;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import letmelisten.unibo.studio.letmelisten.fragment.ChooseWhatToBeFragment;
import letmelisten.unibo.studio.letmelisten.fragment.FindStationFragment;

public class SetupActivity extends SmartActivity implements ChooseWhatToBeFragment.ChooseWhatToBeFragmentInteractionListener {

    private static final int BLUETOOTH_ENABLE_REQUEST_ID = 1;
    public static final String UUID = "d7f7d203-f5ec-4b2d-bfe8-f418d714ea0f";

    private static final int REQUEST_APP_PERMISSIONS = 1;

    private void checkForPermissions() {
        int hasPermission = ActivityCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SetupActivity.this,
                    new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_APP_PERMISSIONS);
        }
    }

    private void enableBluetooth() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if(adapter == null) {
            Toast.makeText(this.getApplicationContext(),this.getResources().getString(R.string.bt_adapter_message),
                    Toast.LENGTH_SHORT).show();
            this.finish();
        }
        if(!adapter.isEnabled()) {
            this.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_REQUEST_ID);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity_layout);



        this.checkForPermissions();

        this.replaceFragment(R.id.setupActivityMainLayout, new ChooseWhatToBeFragment(), false);

        this.enableBluetooth();
    }

    @Override
    public void onBackPressed() {
        final FragmentManager manager = this.getFragmentManager();
        if(manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_APP_PERMISSIONS && grantResults.length == 2 && (grantResults[0] != PackageManager.PERMISSION_GRANTED
                || grantResults[1] != PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(this, this.getResources().getString(R.string.permission_message), Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }


    @Override
    public void startMusicPickerActivity() {
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            final Intent musicPickerIntent = new Intent(SetupActivity.this, MusicRetrieverActivity.class);
            startActivity(musicPickerIntent);
        }
        else {
            Toast.makeText(this.getApplicationContext(), this.getResources().getString(R.string.enable_bt_message),
                    Toast.LENGTH_SHORT).show();
            this.enableBluetooth();
        }

    }

    @Override
    public void loadFindStationFragment() {
        if(BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            this.replaceFragment(R.id.setupActivityMainLayout, new FindStationFragment(), true);
        }
        else {
            Toast.makeText(this.getApplicationContext(),this.getResources().getString(R.string.turn_on_bt_message),
                    Toast.LENGTH_SHORT).show();
            this.enableBluetooth();
        }

    }

}
