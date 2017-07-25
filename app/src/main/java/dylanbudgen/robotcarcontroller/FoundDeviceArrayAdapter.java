package dylanbudgen.robotcarcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dylanbudgen on 23/07/2017.
 */

public class FoundDeviceArrayAdapter extends ArrayAdapter<FoundDevice>
{

    private static final String tag = "FoundDeviceArrayAdapter";
    private Context context;
    private TextView deviceName;
    private TextView deviceUUID;
    private List<FoundDevice> foundDevices = new ArrayList<FoundDevice>();

    /**
     * The default constructor which is invoked to create the FoundDevice array
     * adapter.
     * <p>
     * The adapter is needed to 'translate' data into a viewable item / widget.
     *
     * @param context
     *            the application context
     * @param objects
     *            the backing array populated by FoundDevice objects to be displayed.
     * @see {@link ArrayAdapter}<T>
     */

    public FoundDeviceArrayAdapter(Context context, int textViewResourceId, List<FoundDevice> objects)
    {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.foundDevices = objects;
    }

    /**
     * The method used for determining how many views are in this list or in
     * other words, how many views are managed by this adapter.
     *
     * @return the number of items this adapter controls.
     */
    @Override
    public int getCount()
    {
        return this.foundDevices.size();
    }


    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param index
     *            Position of the item whose data we want within the adapter's
     *            data set.
     * @return the FoundDevice object data at the specified position.
     */
    @Override
    public FoundDevice getItem(int index)
    {
        if (index <= getCount())    //IndexOutOfBoundsException fix
            return this.foundDevices.get(index);
        return this.foundDevices.get(getCount() - 1);
    }

    /**
     * Get a View that displays the data at the specified position in the data
     * set. You can either create a View manually or inflate it from an XML
     * layout file. When the View is inflated, the parent View (GridView,
     * ListView...) will apply default layout parameters unless you use
     * inflate(int, android.view.ViewGroup, boolean) to specify a root view and
     * to prevent attachment to the root.
     * <p>
     * This method is used to generate views to be used in the ListView. This
     * the method that defines how data will look and be represented throughout
     * the UI.
     *
     * @param position
     *            The position of the item that is being placed / The position
     *            of the item within the adapter's data set of the item whose
     *            view we want.
     *            <p>
     * @param convertView
     *            The old view to reuse, if possible. Note: You should check
     *            that this view is non-null and of an appropriate type before
     *            using. If it is not possible to convert this view to display
     *            the correct data, this method can create a new view.
     *            Heterogeneous lists can specify their number of view types, so
     *            that this View is always of the right type (see
     *            getViewTypeCount() and getItemViewType(int))
     *            <p>
     * @param parent
     *            The parent that this view will eventually be attached to.
     * @return the view that defines how this FoundDevice object is represented in the
     *         ListView / A View corresponding to the data at the specified
     *         position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;

        if (row == null)
        {
            // ROW INFLATION
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