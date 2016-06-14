package net.happybrackets.device.config;

/**
 * A small class to keep our host controller's hostname and address together
 */
public class DeviceController {
    private String hostname;
    private String address;

    public DeviceController(String hostname, String address) {
        this.hostname = hostname;
        this.address = address;
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

