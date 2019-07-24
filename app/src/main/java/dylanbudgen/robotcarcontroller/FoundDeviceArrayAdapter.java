package dylanbudgen.robotcarcontroller;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ArrayAdapter class to allow ListView to show device name and device UUID
 * @author Dylan Budgen
 */

public class FoundDeviceArrayAdapter extends ArrayAdapter<FoundDevice>
{
    private TextView deviceName;
    private TextView deviceUUID;
    private List<FoundDevice> foundDevices = new ArrayList<>();

    /**
     * Create new FoundDeviceArrayAdapter object
     * @param context context
     * @param textViewResourceId textView resource id
     * @param objects list of objects
     */
    public FoundDeviceArrayAdapter(Context context, int textViewResourceId, List<FoundDevice> objects)
    {
        super(context, textViewResourceId, objects);
        this.foundDevices = objects;
    }

    /**
     * Get size of foundDevices
     * @return size of foundDevices
     */
    @Override
    public int getCount()
    {
        return this.foundDevices.size();
    }

    /**
     * Return item at specific index
     * @param index index of object to return
     * @return object at index
     */
    @Override
    public FoundDevice getItem(int index)
    {
        if (index <= getCount())    //IndexOutOfBoundsException fix
            return this.foundDevices.get(index);
        return this.foundDevices.get(getCount() - 1);
    }

    /**
     * Create rows of Listview with custom setup
     * @param position position of row to create
     * @param convertView row view
     * @param parent parent ViewGroup
     * @return row
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View row = convertView;

        if (row == null)
        {
            // Row inflation
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        // Get item
        FoundDevice foundDevice = getItem(position);

        deviceName = (TextView) row.findViewById(android.R.id.text1);
        deviceName.setText(foundDevice.getDeviceName());

        deviceUUID = (TextView) row.findViewById(android.R.id.text2);
        deviceUUID.setText(foundDevice.getDeviceAddress());
        deviceUUID.setTextSize(12f);

        return row;
    }
}