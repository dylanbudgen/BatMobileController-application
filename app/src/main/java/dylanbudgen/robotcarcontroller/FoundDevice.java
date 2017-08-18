package dylanbudgen.robotcarcontroller;

/**
 * Created by dylanbudgen on 23/07/2017.
 */

public class FoundDevice {

    private String deviceAddress;
    private String deviceName;

    /**
     * Create new FoundDevice object
     * @param deviceAddress address of device
     * @param deviceName name of device
     */
    FoundDevice(String deviceAddress, String deviceName) {

        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;

    }

    /**
     * Get device address
     * @return device address
     */
    public String getDeviceAddress() {
        return deviceAddress;
    }

    /**
     * Get device name
     * @return device name
     */
    public String getDeviceName() {
        return deviceName;
    }

}
