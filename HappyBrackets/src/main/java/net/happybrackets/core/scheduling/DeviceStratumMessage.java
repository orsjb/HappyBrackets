package net.happybrackets.core.scheduling;

import com.google.gson.Gson;
import net.happybrackets.core.control.CustomGlobalEncoder;

/**
 * Contains the parameters that define the DeviceStratumMessage time for a device.
 * <br>The parameters stored are
 * <ul>
 *     <li>unique deviceName, eg HB-123456</li>
 *     <li>stratum, an index as to how close this device is to the domain time. The device with the lowest stratum is considered the reference time.
 *     If a device is synchronised using an NTP server, then it should have a lower value stratum than one without</li>
 * </ul>
 */
public class DeviceStratumMessage implements CustomGlobalEncoder {

    String deviceName = null; // unique deviceName, eg HB-123456
    int stratum = Integer.MAX_VALUE; // an index as to how close this device is to the domain time. The device with the lowest stratum is considered the reference time.


    @Override
    public Object[] encodeGlobalMessage() {
        return new Object[]{deviceName, stratum};
    }

    @Override
    public DeviceStratumMessage restore(Object restore_data) {
        DeviceStratumMessage ret =  null;

        // First see if this is just the class
        if (restore_data instanceof DeviceStratumMessage){
            ret = (DeviceStratumMessage)restore_data;
        }
        // let us see if it is JsonData
        else if (restore_data instanceof String){
            ret = new Gson().fromJson((String) restore_data, DeviceStratumMessage.class);
        }
        else if (restore_data instanceof Object[]){
            ret =  new DeviceStratumMessage((Object[]) restore_data);
        }

        return ret;
    }

    @Override
    public String toString() {
        return deviceName + " " +  stratum;
    }

    /**
     * Restore data from an array of objects encoded through the encodeGlobalMessage functionn
     * @param args the Object [] arguments
     */
    public DeviceStratumMessage(Object... args) {
        deviceName  = (String) args [0];
        stratum = (int) args[1];
    }
    /**
     * Constructor
     * @param deviceName unique deviceName, eg HB-123456
     * @param stratum  an index as to how close this device is to the domain time.
     */
    public DeviceStratumMessage(String deviceName, int stratum){
        this.deviceName = deviceName;
        this.stratum = stratum;
    }

    /**
     * Default constructor required for restore
     */
    public DeviceStratumMessage(){

    }

    @Override
    public boolean equals(Object other){
        if (other == null){
            return false;
        }else if (! (other instanceof DeviceStratumMessage))
        {
            return false;
        }
        else {
            DeviceStratumMessage right  = (DeviceStratumMessage)other;

            return right.deviceName.equalsIgnoreCase(deviceName)
                    && right.stratum == stratum;
        }
    }
}
