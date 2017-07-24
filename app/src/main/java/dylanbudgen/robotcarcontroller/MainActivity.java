package dylanbudgen.robotcarcontroller;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import dylanbudgen.robotcarcontroller.bluetooth.BleWrapper;
import dylanbudgen.robotcarcontroller.bluetooth.BleWrapperUiCallbacks;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 10;

    private BleWrapper mBleWrapper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        // Initiate BL wrapper
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {

            // Override the methods for Bluetooth

        });


        /* Allowing to run on emulator

        // Check if BLE is supported by the device
        if(mBleWrapper.checkBleHardwareAvailable() == false) {
            Toast.makeText(this, "No BLE compatible hardware detected",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        */


    }





    // Required to set up toolbar and add buttons from res/menu/menu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_connect:
                // Connect button pressed
                Log.d("DEBUG", "000P Connect button pressed");
                connect();
                return true;

            case R.id.action_settings:
                // Settings button pressed
                Log.d("DEBUG", "000P Settings button pressed");
                showAbout();
                return true;

            case R.id.action_about:
                // About button pressed
                Log.d("DEBUG", "000P About button pressed");
                showSettings();
                return true;

            default:
                // Input not recognised, invoke superclass.
                return super.onOptionsItemSelected(item);
        }
    }

    // Show the about page
    private void showAbout() {
        return;
    }

    // Change the settings
    private void showSettings() {
        return;
    }



    public void connect() {

        // Permissions for fine location called when user wishes to use Bluetooth
        checkBluetoothPermissions();









    }





















    private void checkBluetoothPermissions() {

        if (Build.VERSION.SDK_INT > 22) {  // Device needs runtime permissions
            Log.d("DEBUG", "000P Device needs runtime permissions.");

            // Checking if permission for location is already set
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "000P Permission granted.");
            } else {
                Log.d("DEBUG", "000P Permission not granted");

                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this, "The app needs location permission for Bluetooth search", Toast.LENGTH_SHORT).show();
                }

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
            }

        } else { // Device doesn't need runtime permissions
            Log.d("DEBUG", "000P Device does not need runtime permissions.");
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION) {
            // Permission for location recieved
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "000P User Permission granted.");
            } else {
                Log.d("DEBUG", "000P User Permission not granted");
                Toast.makeText(this, "Permisson not granted. Permission is needed to use Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }









}
