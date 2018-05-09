package net.happybrackets.core.config;

/**
 * Stores the way the device is displayed and is stored in Known Config.
 */
public class KnownDeviceID {

    // The ID number to display
    int deviceId = 0;

    // the friendly name we want to display item
    String friendlyName = "";


    public String getHostName() {
        return hostName;
    }

    String hostName = "";


    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }


    /**
     * Create a representation of how this device will be displayed
     * @param hostname The hostname of device
     * @param device_id the integer device ID
     * @param friendly_name the friendly name for the device
     */
    public KnownDeviceID(String hostname, int device_id, String friendly_name)
    {
        hostName = hostname;
        deviceId = device_id;
        friendlyName = friendly_name;
    }

    /**
     * Create a representation of how this device will be displayed
     *
     * @param device_id the integer device ID
     * @param friendly_name the friendly name for the device
     */
    public KnownDeviceID(int device_id, String friendly_name)
    {
        deviceId = device_id;
        friendlyName = friendly_name;
    }

    /**
     * Create a representation of how this device will be displayed with a default values
     */
    public KnownDeviceID()
    {
    }

    /**
     * Create a KnownDeviceId based on the line of text to restore.
     * @param restore_line the space delimited line fof text. May look like "hb-001d43201188 1 Dancer_1"
     * @return If the line is valid, a new KnownDeviceID is created, otherwise null
     */
    public static KnownDeviceID restore(String restore_line){

        KnownDeviceID ret = null;
        try {


            String[] line_split = restore_line.trim().split("[ ]+");
            // Ignore blank or otherwise incorrectly formatted lines.
            if (line_split.length >= 2) {
                // we are going to ignore this because we don't store it
                String device_name = line_split[0];
                int device_id = Integer.parseInt(line_split[1]);

                String friendly_name = "";
                if (line_split.length > 2) {
                    for (int i = 2; i < line_split.length; i++) {
                        friendly_name = friendly_name + " " + line_split[i];
                    }

                }

                ret = new KnownDeviceID(device_name, device_id, friendly_name);

            }
        }catch (Exception ex){}

        return ret;
    }

    /**
     * Get the line of text we need to save this device to a known config file
     * @return The line of text we use to save
     */
    public String getSaveLine(){
        return hostName + " " + deviceId + " " + friendlyName;
    }
}
