package dylanbudgen.robotcarcontroller;

/**
 * Created by dylanbudgen on 23/07/2017.
 */

public class FoundDevice {

    private String deviceAddress;
    private String deviceName;

    FoundDevice(String deviceAddress, String deviceName) {

        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;

    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
