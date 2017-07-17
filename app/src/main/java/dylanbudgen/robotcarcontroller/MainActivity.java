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









    }


    public void connect(View view) {

        // Permissions for fine location called when user wishes to use Bluetooth
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
