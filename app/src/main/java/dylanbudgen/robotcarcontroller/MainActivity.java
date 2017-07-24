package dylanbudgen.robotcarcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.util.ArrayList;

import dylanbudgen.robotcarcontroller.bluetooth.BleWrapper;
import dylanbudgen.robotcarcontroller.bluetooth.BleWrapperUiCallbacks;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 10;

    private BleWrapper mBleWrapper = null;

    private String mState;

    private ArrayList<FoundDevice> devicesList;

    private FoundDeviceArrayAdapter scanningListviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Initiate variables
        devicesList = new ArrayList<>();
        mState = "";


        // Initiate BL wrapper
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {

            // Override the methods for Bluetooth

            @Override
            public void uiDeviceFound(final BluetoothDevice device,
                                      final int rssi,
                                      final byte[] record)
            {
                if(device.getName() != null) {

                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress().toString();

                    Log.d("DEBUG", "000P uiDeviceFound: " + deviceName + ", " + rssi);

                    // Checking if device has already been found
                    for (FoundDevice foundDevice : devicesList) {
                        if (deviceAddress.equals(foundDevice.getDeviceUUID())) {
                            return; // Device has already been found
                        }
                    }

                    devicesList.add(new FoundDevice(deviceAddress, deviceName));
                    scanningListviewAdapter.notifyDataSetChanged();

                }

            }

        });

        // Check if BLE is supported by the device
        if(mBleWrapper.checkBleHardwareAvailable() == false) {
            Toast.makeText(this, "No BLE compatible hardware detected",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        devicesList.clear();
        mBleWrapper.initialize();

    }


    @Override
    public void onPause() {
        super.onPause();

        // TODO CLOSE THE POPUP WINDOW IF APP CLOSES, THIS WILL SCREW THIS UP OTHERWISE

        mBleWrapper.stopScanning();
        mBleWrapper.diconnect();
        mBleWrapper.close();

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



    private void connect() {

        // Check if the window is already open
        if (mState.equals("SCANNING")) {
            // The scanning window is already open.
        } else {
            // Check if Bluetooth is enabled
            if (mBleWrapper.isBtEnabled() == false) {
                // Bluetooth is not enabled.
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBT);
            } else {

                // Permissions for fine location called when user wishes to use Bluetooth
                checkBluetoothPermissions();

                // Check if Bluetooth is on
                checkBluetoothStatus();

                // Update the state to prevent user opening multiple windows
                mState = "SCANNING";

                ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.main_activity);
                LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

                // According to StackOverflow, last boolean is to detect touches in popup.
                // Although I think it works without it...

                // Inflate the custom layout/view
                View customView = inflater.inflate(R.layout.popup_scanning, null, true);

                // Initialize a new instance of popup window
                final PopupWindow mPopupWindow = new PopupWindow(
                        customView,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );

                // Set an elevation value for popup window
                if (Build.VERSION.SDK_INT >= 21) {
                    //
                    mPopupWindow.setElevation(5.0f);
                }

                // Get a reference for the custom view close button
                ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

                // Set a click listener for the popup window close button
                closeButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        mBleWrapper.stopScanning();
                        mState = "";
                        devicesList.clear();
                        mPopupWindow.dismiss();

                        // Null the scanningListviewAdapter to free resources
                        scanningListviewAdapter = null;
                    }
                });

                        /*
                            public void showAtLocation (View parent, int gravity, int x, int y)
                                Display the content view in a popup window at the specified location. If the
                                popup window cannot fit on screen, it will be clipped.
                                Learn WindowManager.LayoutParams for more information on how gravity and the x
                                and y parameters are related. Specifying a gravity of NO_GRAVITY is similar
                                to specifying Gravity.LEFT | Gravity.TOP.

                            Parameters
                                parent : a parent view to get the getWindowToken() token from
                                gravity : the gravity which controls the placement of the popup window
                                x : the popup's x location offset
                                y : the popup's y location offset
                        */

                // Finally, show the popup window at the center location of root relative layout
                mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

                scanningListviewAdapter = new FoundDeviceArrayAdapter(this,
                        android.R.layout.simple_list_item_2, devicesList);

                ListView listView = (ListView) customView.findViewById(R.id.listView_scanning);
                listView.setAdapter(scanningListviewAdapter);

                if(mBleWrapper == null) {
                    Log.d("DEBUG", "000P Wrapper is null");
                } else {
                    Log.d("DEBUG", "000P Wrapper is not null");
                }
                // Start scanning
                mBleWrapper.startScanning();

                /* TODO TO BE USED WHEN THE LIST IS UPDATED

                ArrayAdapter listAdapter = (ArrayAdapter) listView.getAdapter();
                listAdapter.notifyDataSetChanged();
                */
            }
        }








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

    private void checkBluetoothStatus() {

        if (mBleWrapper.isBtEnabled() == false) {
            // Bluetooth is not enabled.
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);
        }
    }









}
