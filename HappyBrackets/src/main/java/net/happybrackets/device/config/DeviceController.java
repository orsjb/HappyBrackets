package net.happybrackets.device.config;

/**
 * A small class to keep our host controller's hostname and address together
 */
public class DeviceController {
    private String hostname;
    private String address;
    private int    deviceId;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceController(String hostname, String address, int deviceId) {
        this.hostname = hostname;
        this.address = address;
        this.deviceId = deviceId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}

