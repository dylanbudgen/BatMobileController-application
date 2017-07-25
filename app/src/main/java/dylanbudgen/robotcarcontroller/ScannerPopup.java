package dylanbudgen.robotcarcontroller;

import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.ArrayList;

/**
 * Created by dylanbudgen on 24/07/2017.
 */

public class ScannerPopup {

    // TODO THIS CLASS MAY BE AN INSTANCE, FINAL CLASS OR NORMAL
    // IT WILL HOLD ANYTHING TO DO WITH THE LISTVIEW, WITH ALL THE ADAPTERS ECT HELD INSIDE VARIABLES
    // OBVIOUSLY USE SETTERS AND GETTERS


    // THIS CLASS COULD BE A NULL VARIABLE, WHICH IS INITATED UPON SCANNING AND NULLED WHEN STOPPING

    // EXTRA CONSIDERATION IS NEEDED DUE TO THE DEVICELIST BEING USED IN ULTIPLE PLACES.

    // CHECK IF THE DEVICE LIST IS NEEDED AFTER THE SCANNERPOPUP WOULD LIKELY BE CLOSED

    private ConstraintLayout layout;
    private LayoutInflater inflater;
    private View customView;
    private PopupWindow mPopupWindow;
    private ImageButton closeButton;
    private FoundDeviceArrayAdapter scanningListviewAdapter;
    private ListView listView;

    private ArrayList<FoundDevice> deviceList;


    ScannerPopup() {




    }


    public void run() {

    }





    private void setupCloseButton() {





    }




}
