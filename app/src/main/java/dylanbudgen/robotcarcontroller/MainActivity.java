package dylanbudgen.robotcarcontroller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dylanbudgen.robotcarcontroller.bluetooth.BleWrapper;
import dylanbudgen.robotcarcontroller.bluetooth.BleWrapperUiCallbacks;

/**
 * Main activity for scanning, connecting to and controlling a robot car.
 * @author Dylan Budgen
 */
public class MainActivity extends AppCompatActivity {

    // BatMobile Bluetooth address
    private static final String BATMOBILE_ADDRESS = "CA:2F:AE:13:FD:41";

    // Code for permissions
    private static final int REQUEST_CODE_ACCESS_FINE_LOCATION = 10;

    // Distance until avoidance is detected in cm
    private final int ERROR_DISTANCE_FORWARD = 15;
    private final int ERROR_DISTANCE_SIDES = 15;

    // Directions
    private final static int STOP = 0;
    private final static int FORWARD = 1;
    private final static int LEFT = 2;
    private final static int RIGHT = 3;
    private final static int BACKWARD = 4;

    // Colours
    private final static int RECT_GREY = R.drawable.rect_grey;
    private final static int RECT_GREEN = R.drawable.rect_green;
    private final static int RECT_RED = R.drawable.rect_red;
    private final static int CIRCLE_GREY = R.drawable.circle_grey;
    private final static int CIRCLE_GREEN = R.drawable.circle_green;

    // BL wrapper
    private BleWrapper mBleWrapper = null;

    // Media player for error sounds
    private MediaPlayer mErrorSoundPlayer;

    // Sound enabled setting
    private boolean mSoundEnabled;

    // Variables for scanning popup
    private FoundDeviceArrayAdapter mScanningListviewAdapter;
    private ArrayList<FoundDevice> mDevicesList = new ArrayList<>();
    private PopupWindow mPopupWindow = null;

    // State of application
    private String mState = "";

    // Current direction travelling
    private int mDirection = STOP;

    // Ultrasound values
    private int mUltrasoundLeftValue = 100;
    private int mUltrasoundFrontValue = 100;
    private int mUltrasoundRightValue = 100;

    // UUIDs of services and characteristics
    private static final UUID
            UUID_DIRECTION_SERVICE = UUID.fromString("f0000a000-0000-1000-8000-00805f9b34fb"),
            UUID_DIRECTION_WRITE = UUID.fromString("f0000a002-0000-1000-8000-00805f9b34fb"),
            UUID_SPEED_SERVICE = UUID.fromString("f0000a003-0000-1000-8000-00805f9b34fb"),
            UUID_SPEED_WRITE = UUID.fromString("f0000a004-0000-1000-8000-00805f9b34fb"),
            UUID_ULTRASOUND_SERVICE = UUID.fromString("f0000a005-0000-1000-8000-00805f9b34fb"),
            UUID_ULTRASOUND_LEFT = UUID.fromString("f0000a007-0000-1000-8000-00805f9b34fb"),
            UUID_ULTRASOUND_FRONT = UUID.fromString("f0000a006-0000-1000-8000-00805f9b34fb"),
            UUID_ULTRASOUND_RIGHT = UUID.fromString("f0000a008-0000-1000-8000-00805f9b34fb");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // Set up button listeners
        setupDirectionButton(R.id.button_direction_backward, BACKWARD);
        setupDirectionButton(R.id.button_direction_stop, STOP);
        setupDirectionButton(R.id.button_direction_forward, FORWARD);
        setupDirectionButton(R.id.button_direction_left, LEFT);
        setupDirectionButton(R.id.button_direction_right, RIGHT);

        // Initiate BL wrapper
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {

            // Override the methods for Bluetooth

            /**
             * Executed when a device is found while scanning. Device is added to
             * the array of devices.
             */
            @Override
            public void uiDeviceFound(final BluetoothDevice device,
                                      final int rssi,
                                      final byte[] record)
            {
                if(device.getName() != null) {

                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();

                    //Log.d("DEBUG", "000P uiDeviceFound: " + deviceName + ", " + rssi);

                    // Checking if device has already been found
                    for (FoundDevice foundDevice : mDevicesList) {
                        if (deviceAddress.equals(foundDevice.getDeviceAddress())) {
                            return; // Device has already been found
                        }
                    }

                    mDevicesList.add(new FoundDevice(deviceAddress, deviceName));
                    mScanningListviewAdapter.notifyDataSetChanged();
                }
            }

            /**
             *  Executed when services are discovered after connecting.
             *  Initiates the chain of events to setup car.
             */
            @Override
            public void uiAvailableServices(BluetoothGatt gatt,
                                            BluetoothDevice device,
                                            List<BluetoothGattService> services) {

                /* for (BluetoothGattService service : services) {
                    String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());

                    //Log.d("DEBUG", "000P Found service: " + serviceName + " with UUID: "
                            + service.getUuid().toString());

                    for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
                        //Log.d("DEBUG", "000P Found characteristic: " + " with UUID: "
                                + c.getUuid().toString());
                    }
                } */

                // Device was told to disconnect but attempted to connect again
                if (mState.equals("MANUAL_DISCONNECT")) {
                    return;
                }

                updateTextView(R.id.textview_connection_status, getString(R.string.updating_settings));
                setSpeedSetting();
            }

            /**
             * Executed when a characteristic is updated. Checks what characteristic was changed
             * and calls checkSensor method.
             */
            @Override
            public void uiNewValueForCharacteristic(BluetoothGatt gatt,
                                                    BluetoothDevice device,
                                                    BluetoothGattService service,
                                                    BluetoothGattCharacteristic ch,
                                                    String strValue,
                                                    int intValue,
                                                    byte[] rawValue,

                                                    String timestamp) {

                super.uiNewValueForCharacteristic(gatt,device,service,ch,strValue,
                        intValue,rawValue,timestamp);

                UUID updatedUUID = ch.getUuid();

                if (updatedUUID.equals(UUID_ULTRASOUND_LEFT)) {

                    //Log.d("DEBUG", "000P Ultrasound left read: " + intValue);
                    mUltrasoundLeftValue = intValue;

                } else if (updatedUUID.equals(UUID_ULTRASOUND_FRONT)) {

                    //Log.d("DEBUG", "000P Ultrasound front read: " + intValue);
                    mUltrasoundFrontValue = intValue;

                } else if (updatedUUID.equals(UUID_ULTRASOUND_RIGHT)) {

                    //Log.d("DEBUG", "000P Ultrasound right read: " + intValue);
                    mUltrasoundRightValue = intValue;
                }

                // Device was told to disconnect but attempted to connect again
                if (mState.equals("MANUAL_DISCONNECT")) {
                    return;
                }

                checkSensor();
            }

            /**
             * Executed on successful write of a characteristic.
             */
            @Override
            public void uiSuccessfulWrite( BluetoothGatt gatt,
                                           BluetoothDevice device,
                                           BluetoothGattService service,
                                           BluetoothGattCharacteristic ch,
                                           String description) {

                super.uiSuccessfulWrite(gatt, device, service, ch, description);

                //Log.d("DEBUG", "000P uiSuccessfulWrite.");

                // Chain reaction for the enabling of notifications
                switch (mState) {
                    case "SET_SPEED_SETTING" :
                        // Now enable the left sensor, then the front sensor
                        mState = "ULTRASOUND_FRONT_ENABLE";
                        enableNotifications(UUID_ULTRASOUND_SERVICE, UUID_ULTRASOUND_LEFT);
                        break;
                    case "ULTRASOUND_FRONT_ENABLE" :
                        // Now enable front sensor, then the right sensor
                        mState = "ULTRASOUND_RIGHT_ENABLE";
                        enableNotifications(UUID_ULTRASOUND_SERVICE, UUID_ULTRASOUND_FRONT);
                        break;
                    case "ULTRASOUND_RIGHT_ENABLE" :
                        mState = "UPDATE_PROGRESS_BAR";
                        enableNotifications(UUID_ULTRASOUND_SERVICE, UUID_ULTRASOUND_RIGHT);
                        break;
                    case "UPDATE_PROGRESS_BAR" :
                        mState = "CONNECTED";
                        updateProgressBar(false);
                        updateTextView(R.id.textview_connection_status, getString(R.string.connected));
                        updateDirectionButtons(true);
                        updateUltrasoundGraphics(RECT_GREEN, CIRCLE_GREEN);
                        break;
                }


            }

            /**
             * Executed on failed write of a characteristic.
             */
            @Override
            public void uiFailedWrite( BluetoothGatt gatt,
                                       BluetoothDevice device,
                                       BluetoothGattService service,
                                       BluetoothGattCharacteristic ch,
                                       String description) {

                super.uiFailedWrite(gatt, device, service, ch, description);

                mBleWrapper.diconnect();

                updateTextView(R.id.textview_connection_status, getString(R.string.error_connecting));
                updateProgressBar(false);

                //Log.d("DEBUG", "000P uiFailedWrite");
            }

            /**
             * Executed on device connection.
             */
            @Override
            public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
                super.uiDeviceConnected(gatt, device);

                //Log.d("DEBUG", "000P Device connected");

            }

            /**
             * Executed on device disconnection.
             */
            @Override
            public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
                super.uiDeviceDisconnected(gatt, device);

                // Device has disconnected unexpectedly - excepted disconnections handled in disconnect()
                if (!mState.equals("MANUAL_DISCONNECT")) {

                    //Log.d("DEBUG", "000P Device disconnected");
                    updateTextView(R.id.textview_connection_status, getString(R.string.disconnected));
                    updateProgressBar(false);
                    updateDirectionButtons(false);
                    updateUltrasoundGraphics(RECT_GREY, CIRCLE_GREY);
                    mState = "";
                }
            }

        });

        // Check if BLE is supported by the device
        if(!mBleWrapper.checkBleHardwareAvailable()) {
            Toast.makeText(this, "No BLE compatible hardware detected",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        checkBluetoothStatus();
    }

    @Override
    public void onResume() {
        super.onResume();

        mState = "";
        updateTextView(R.id.textview_connection_status, getString(R.string.no_connection));
        updateProgressBar(false);
        updateDirectionButtons(false);
        updateUltrasoundGraphics(RECT_GREY, CIRCLE_GREY);

        mDevicesList.clear();
        mBleWrapper.initialize();

        // Initialise media player
        mErrorSoundPlayer = MediaPlayer.create(this, R.raw.beep_censor);
        mErrorSoundPlayer.setLooping(false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundEnabled = sharedPref.getBoolean("sound_switch", false);
    }


    @Override
    public void onPause() {
        super.onPause();

            mBleWrapper.stopScanning();
            disconnect();
            mBleWrapper.close();

        if  (mPopupWindow != null) {
            mDevicesList.clear();
            mPopupWindow.dismiss();
            ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
            clearDim(viewGroup);

        }

        // Media mErrorSoundPlayer release
        mErrorSoundPlayer.release();
        mErrorSoundPlayer = null;

    }

    // Required to set up toolbar and add buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_connect:
                // Connect button pressed
                scan();
                return true;

            case R.id.action_settings:
                // Settings button pressed
                showSettings();
                return true;

            case R.id.action_about:
                // About button pressed
                showAbout();
                return true;

            default:
                // Input not recognised, invoke superclass.
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Start the about page activity
     */
    private void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    /**
     * Start the settings activity
     */
    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Open the pop up window and begin scanning. Allow the user to choose a device to connect to.
     */
    private void scan() {

        mDevicesList.clear();

        // Check if the window is already open
        if(mState.equals("SCANNING")) {
            // The scanning window is already open.
        } else {

            // Permissions for fine location called when user wishes to use Bluetooth
            if (!checkBluetoothPermissions()) {
                return;
            }

            // Reset bluetooth if already connected, so user can connect again
            if (mBleWrapper.isConnected()) {
                updateProgressBar(true);
                updateTextView(R.id.textview_connection_status, getString(R.string.disconnecting));
                disconnect();
                return;
            }

            if (!mBleWrapper.isBtEnabled()) {
                Toast.makeText(this, R.string.BL_off, Toast.LENGTH_SHORT).show();
                finish();
            }

            // Update the state to prevent user opening multiple windows
            mState = "SCANNING";

            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.main_activity);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            View customView = inflater.inflate(R.layout.popup_scanning, null, true);

            // Initialize a new instance of popup window
            mPopupWindow = new PopupWindow(
                    customView,
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
            );

            // Set an elevation value for popup window
            if (Build.VERSION.SDK_INT >= 21) {
                mPopupWindow.setElevation(5.0f);
            }

            // Set open/close animation
            mPopupWindow.setAnimationStyle(R.style.PopUp_Animation);

            // Get a reference for the custom view close button
            ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);

            // Dim background
            final ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
            applyDim(viewGroup, 0.5f);

            // Set a click listener for the popup window close button
            closeButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        mState = "";
                        mBleWrapper.stopScanning();
                        mDevicesList.clear();
                        mPopupWindow.dismiss();
                        clearDim(viewGroup);

                        // Null the mScanningListviewAdapter and PopupWindow to free resources
                        mPopupWindow = null;
                        mScanningListviewAdapter = null;
                    }
            });

            // Show the popup window at the center location of root relative layout
            mPopupWindow.showAtLocation(layout, Gravity.CENTER, 0, 0);

            // Set up the button listener for connecting
            AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, View v, int position, long id) {

                        mBleWrapper.stopScanning();
                        mPopupWindow.dismiss();
                        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
                        clearDim(viewGroup);

                        connect(mDevicesList.get(position).getDeviceAddress());

                    }
                };

            // Set up the list adapter to update the list
            mScanningListviewAdapter = new FoundDeviceArrayAdapter(this,
                        android.R.layout.simple_list_item_2, mDevicesList);

            ListView listView = (ListView) customView.findViewById(R.id.listView_scanning);
            listView.setAdapter(mScanningListviewAdapter);
                listView.setOnItemClickListener(mMessageClickedHandler);

            // Start scanning
            mBleWrapper.startScanning();

        }
    }

    /**
     * Apply a dim to the background view.
     * @author Markus Rubey
     * @author https://stackoverflow.com/users/1399597/markus-rubey
     * @param parent parent ViewGroup
     * @param dimAmount the amount of dim applied
     */
    private static void applyDim(@NonNull ViewGroup parent, float dimAmount){
        Drawable dim = new ColorDrawable(Color.BLACK);
        dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
        dim.setAlpha((int) (255 * dimAmount));

        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.add(dim);
    }

    /**
     * Clear the dim to the background view.
     * @author Markus Rubey
     * @author https://stackoverflow.com/users/1399597/markus-rubey
     * @param parent parent ViewGroup
     */
    private static void clearDim(@NonNull ViewGroup parent) {
        ViewGroupOverlay overlay = parent.getOverlay();
        overlay.clear();
    }

    /**
     * Connect to a specific device by address.
     * @param address device address for connecting
     */
    private void connect(String address) {

        if (!address.equals(BATMOBILE_ADDRESS)) {
            updateTextView(R.id.textview_connection_status, getString(R.string.wrong_device));
            return;
        }

        if (mBleWrapper.isConnected()) {
            Toast.makeText(this, R.string.error_restarting, Toast.LENGTH_SHORT).show();
            finish();

        } else {
            //Log.d("DEBUG", "000P Connecting");
            mState = "CONNECTING";
            updateProgressBar(true);
            updateTextView(R.id.textview_connection_status, getString(R.string.connecting));
            mBleWrapper.connect(address);
        }
    }

    /**
     * Manually disconnect, giving time for Bluetooth wrapper to disconnect fully.
     */
    private void disconnect() {

        mState = "MANUAL_DISCONNECT";

        mBleWrapper.diconnect();

        updateDirectionButtons(false);
        updateUltrasoundGraphics(RECT_GREY, CIRCLE_GREY);
        updateConnectButton(false);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                updateConnectButton(true);
                updateProgressBar(false);
                updateTextView(R.id.textview_connection_status, getString(R.string.ready));
            }
        }, 2000);
    }

    /**
     * Enable notifications to a characteristic
     * @param service UUID of service
     * @param characteristic UUID of characteristic
     */
    private void enableNotifications(UUID service, UUID characteristic) {

        //Log.d("DEBUG", "000P Setting on notifications");
        BluetoothGatt gatt = mBleWrapper.getGatt();
        BluetoothGattCharacteristic c = gatt.getService(service).getCharacteristic(characteristic);
        mBleWrapper.setNotificationForCharacteristic(c, true);
    }

    /**
     * Send speed setting to robotcar.
     */
    private void setSpeedSetting() {

        mState = "SET_SPEED_SETTING";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int speed = Integer.parseInt(sharedPref.getString("speed", "2"));

        writeToCharacteristic(UUID_SPEED_SERVICE, UUID_SPEED_WRITE, speed);
    }

    /**
     * Write an integer to a characteristic
     * @param service UUID of service
     * @param characteristic UUID of characteristic
     * @param writeValue value to write
     */
    private void writeToCharacteristic(UUID service, UUID characteristic, int writeValue) {

        BluetoothGatt gatt;
        BluetoothGattCharacteristic c;

        gatt = mBleWrapper.getGatt();
        c = gatt.getService(service).getCharacteristic(characteristic);

        byte[] value = new byte[1];

        value[0] = (byte) (writeValue);
        mBleWrapper.writeDataToCharacteristic(c, value);
    }

    /**
     * Check if sensors are triggered and avoid obstacles
     * @return if car is facing obstacle
     */
    private boolean checkSensor() {

        boolean status = true;

        if (mUltrasoundFrontValue <= ERROR_DISTANCE_FORWARD) {
            updateUltrasoundSensorGraphic(R.id.textView_front_ultrasound, RECT_RED);
            status = false;
        } else {
            updateUltrasoundSensorGraphic(R.id.textView_front_ultrasound, RECT_GREEN);
        }

        if (mUltrasoundLeftValue <= ERROR_DISTANCE_SIDES) {
            updateUltrasoundSensorGraphic(R.id.textView_left_ultrasound, RECT_RED);
            status = false;
        } else {
            updateUltrasoundSensorGraphic(R.id.textView_left_ultrasound, RECT_GREEN);
        }

        if (mUltrasoundRightValue <= ERROR_DISTANCE_SIDES) {
            updateUltrasoundSensorGraphic(R.id.textView_right_ultrasound, RECT_RED);
            status = false;
        } else {
            updateUltrasoundSensorGraphic(R.id.textView_right_ultrasound, RECT_GREEN);
        }

        // Sensors are triggered so disable forward button
        if (!status) {

            updateForwardButton(false);

            if (mDirection == FORWARD) {
                if (mSoundEnabled) { mErrorSoundPlayer.start(); }
                changeDirection(STOP);
                return false;
            }

            // direction is not forward, so car can proceed in current direction
            return true;

        } else {
            // sensors are all fine, so return true and enable forward button
            updateForwardButton(true);
            return true;
        }
    }

    private void changeDirection(int direction) {

        mDirection = direction;

        if (direction == STOP) {
            writeToCharacteristic(UUID_DIRECTION_SERVICE, UUID_DIRECTION_WRITE, direction);

        } else if (checkSensor()) {
            writeToCharacteristic(UUID_DIRECTION_SERVICE, UUID_DIRECTION_WRITE, direction);
        }
    }

    /**
     * Enable or disable the connect button
     * @param status new status of button
     */
    private void updateConnectButton(final boolean status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ActionMenuItemView button = (ActionMenuItemView) findViewById(R.id.action_connect);
                button.setEnabled(status);

            }
        });
    }

    /**
     * Enable or disable the forward button
     * @param status new status of forward button
     */
    private void updateForwardButton(final boolean status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Button buttonForward = (Button) findViewById(R.id.button_direction_forward);
                buttonForward.setEnabled(status);

            }
        });
    }

    /**
     * Enable or disable the all direction buttons
     * @param status new status of direction buttons
     */
    private void updateDirectionButtons(final boolean status) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Button buttonStop = (Button) findViewById(R.id.button_direction_stop);
                buttonStop.setEnabled(status);

                Button buttonLeft = (Button) findViewById(R.id.button_direction_left);
                buttonLeft.setEnabled(status);

                Button buttonForward = (Button) findViewById(R.id.button_direction_forward);
                buttonForward.setEnabled(status);

                Button buttonRight = (Button) findViewById(R.id.button_direction_right);
                buttonRight.setEnabled(status);

                Button buttonBackward = (Button) findViewById(R.id.button_direction_backward);
                buttonBackward.setEnabled(status);

            }
        });
    }

    /**
     * Update the colour of a part of the ultrasound graphic
     * @param id id of the graphic
     * @param colour new colour
     */
    private void updateUltrasoundSensorGraphic(final int id, final int colour) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView textView = (TextView) findViewById(id);
                textView.setBackgroundResource(colour);
            }
        });
    }

    /**
     * Update the colour of the whole ultrasound graphic
     * @param rectColour new colour for the rectangle
     * @param circleColour new colour for the circle
     */
    private void updateUltrasoundGraphics(final int rectColour, final int circleColour) {

        updateUltrasoundSensorGraphic(R.id.textView_left_ultrasound, rectColour);
        updateUltrasoundSensorGraphic(R.id.textView_right_ultrasound, rectColour);
        updateUltrasoundSensorGraphic(R.id.textView_front_ultrasound, rectColour);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ImageView image = (ImageView) findViewById(R.id.imageView);
                image.setImageResource(circleColour);

            }
        });

    }

    /**
     * Update the visibility of the progress bar
     * @param visibility new visibility of the progress bar
     */
    private void updateProgressBar(final boolean visibility) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

                if (visibility) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                } else {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });
    }

    /**
     * Update the status message text
     * @param textviewId id of text view
     * @param message string to show to user
     */
    private void updateTextView(final int textviewId, final String message) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView textView = (TextView) findViewById(textviewId);
                textView.setText(message);
            }
        });
    }

    /**
     * Check if permissions are activated or make request
     * @return user response
     */
    private boolean checkBluetoothPermissions() {

        if (Build.VERSION.SDK_INT > 22) {  // Device needs runtime permissions

            // Checking if permission for location is already set
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ACCESS_FINE_LOCATION);
                return false;
            }
        }
        return true;
    }

    /**
     * Executed when user responds to permissions and handles responses
     * @param requestCode identifier for permission
     * @param permissions permissions
     * @param grantResults user responses
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_ACCESS_FINE_LOCATION) {
            // Permission for location received
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, R.string.permissions_rejected, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * Set up the direction button listener
     * @param buttonName name of button
     * @param direction direction of button
     */
    private void setupDirectionButton(int buttonName, final int direction) {

        Button button = (Button) findViewById(buttonName);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        //Log.d("DEBUG", "00P Button down " + " Direction: " + direction);

                        mDirection = direction;
                        changeDirection(direction);
                        break;

                    case MotionEvent.ACTION_UP:
                        //Log.d("DEBUG", "00P Button up " + " Direction: " + direction);
                        changeDirection(STOP);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Check if Bluetooth is enabled, and make request.
     */
    private void checkBluetoothStatus() {

        if (!mBleWrapper.isBtEnabled()) {
            // Bluetooth is not enabled.
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBT);
            finish();
        }
    }

}
