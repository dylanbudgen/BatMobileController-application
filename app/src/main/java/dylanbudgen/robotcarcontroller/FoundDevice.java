package dylanbudgen.robotcarcontroller;

/**
 * Created by dylanbudgen on 23/07/2017.
 */

public class FoundDevice {

    private String deviceUUID;
    private String deviceName;

    FoundDevice(String deviceUUID, String deviceName) {

        this.deviceUUID = deviceUUID;
        this.deviceName = deviceName;

    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
