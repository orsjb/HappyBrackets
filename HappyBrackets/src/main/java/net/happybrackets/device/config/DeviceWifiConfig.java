package net.happybrackets.device.config;

/**
 * A basic class to hold our wifi config and work with gson
 *
 * Created by Samg on 19/05/2016.
 */
public class DeviceWifiConfig {

    private String ssid;
    private String psk;

    public DeviceWifiConfig(String ssid, String psk) {
        this.ssid = ssid;
        this.psk = psk;
    }

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public String getPSK() {
        return psk;
    }

    public void setPSK(String psk) {
        this.psk = psk;
    }
}
