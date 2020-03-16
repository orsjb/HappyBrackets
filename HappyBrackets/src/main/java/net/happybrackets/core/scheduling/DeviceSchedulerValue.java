package net.happybrackets.core.scheduling;

import com.google.gson.Gson;
import net.happybrackets.core.control.CustomGlobalEncoder;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.sensors.DataSmoother;

/**
 * Contains the parameters that define the Scheduled time for a device.
 * <br>The parameters stored are
 * <ul>
 *     <li>unique deviceName, eg HB-123456</li>
 *     <li>uptime, eg the JVM uptime from when device started</li>
 *     <li>scheduler time, the time that the device's global {@link HBScheduler} has it's time set to</li>
 *     <li>stratum, an index as to how close this device is to the domain time. The device with the lowest stratum is considered the reference time.
 *     If a device is synchronised using an NTP server, then it should have a lower value stratum than one without</li>
 * </ul>
 */
public class DeviceSchedulerValue implements CustomGlobalEncoder, Comparable {
    final double PRECISION = 0.000001d;
    final int NUM_JVM_DIFF_STORES = 5;



    String deviceName = null; // unique deviceName, eg HB-123456
    double upTime = 0; // eg the JVM uptime from when device started
    double schedulerTime = 0; // the time that the device's global {@link HBScheduler} has it's time set to
    int stratum = Integer.MAX_VALUE; // an index as to how close this device is to the domain time. The device with the lowest stratum is considered the reference time.

    double lastUpdateJVMTime = 0; // This value is not sent. We keep it as a local variable so we can know last time we set it. Use JVM because it is based on system clock

    float discardCount = 0;

    int resetCount = 0; // The number of times we have done a reset for jitter

    DataSmoother jvmDifferences = new DataSmoother(NUM_JVM_DIFF_STORES);
    /**
     * Copy the primitive types within the object. Using this to reduce fragmentation
     * @param right the object we are copying from.
     * @return true if it will change the sort comparator value
     */
    public boolean copyValues(DeviceSchedulerValue right){
        boolean sort_changed = Math.abs(right.upTime - upTime) > PRECISION || stratum == right.stratum;

        upTime = right.upTime;
        schedulerTime = right.schedulerTime;
        stratum = right.stratum;
        lastUpdateJVMTime = right.lastUpdateJVMTime;

        return sort_changed;
    }

    /**
     * Estimate what the device's {@link HBScheduler} time will be based on what it was set to when received.
     * @param jvm_time the current JVM time on THIS device
     * @return the estimated {@link HBScheduler} time of the device we are examining
     */
    public double estimateSchedulerTime (double jvm_time){
        double time_since_received =  jvm_time - lastUpdateJVMTime;
        return schedulerTime + time_since_received;
    }

    /**
     * Default constructor required for restore
     */
    public DeviceSchedulerValue(){
    }
    /**
     * Constructor
     * @param deviceName unique deviceName, eg HB-123456
     * @param upTime the JVM uptime from when device started
     * @param schedulerTime the time that the device has it's schedule set at the uptime
     * @param stratum  an index as to how close this device is to the domain time.
     */
    public DeviceSchedulerValue(String deviceName, double upTime, double schedulerTime, int stratum){
        this.deviceName = deviceName;
        this.upTime = upTime;
        this.schedulerTime = schedulerTime;
        this.stratum = stratum;
    }
    /**
     * Restore data from an array of objects encoded through the encodeGlobalMessage functionn
     * @param args the Object [] arguments
     */
    public DeviceSchedulerValue(Object... args) {
        deviceName  = (String) args [0];

        upTime = DynamicControl.integersToScheduleTime((int)args[1], (int)args[2], (int)args[3]);
        schedulerTime = DynamicControl.integersToScheduleTime((int)args[4], (int)args[5], (int)args[6]);
        stratum = (int) args[7];
    }

    // If a device is synchronised using an NTP server, then it should have a lower value stratum than one without

    @Override
    public Object[] encodeGlobalMessage() {

        int [] uptime_vals = DynamicControl.scheduleTimeToIntegers(upTime);
        int [] scheduler_vals = DynamicControl.scheduleTimeToIntegers(schedulerTime);

        // we need args for deviceName, uptime, schedulerTime and stratum
        Object [] ret = new Object[2 + uptime_vals.length + scheduler_vals.length];

        int index = 0;

        ret [index++] = deviceName;

        for (int i =  0; i < uptime_vals.length; i++, index++){
            ret [index] = uptime_vals[i];
        }

        for (int i = 0; i < scheduler_vals.length; i++, index++){
            ret[index] = scheduler_vals[i];
        }

        ret [index] = stratum;

        return ret;
    }

    @Override
    public DeviceSchedulerValue restore(Object restore_data) {
        DeviceSchedulerValue ret =  null;

        // First see if this is just the class
        if (restore_data instanceof DeviceSchedulerValue){
            ret = (DeviceSchedulerValue)restore_data;
        }
        // let us see if it is JsonData
        else if (restore_data instanceof String){
            ret = new Gson().fromJson((String) restore_data, DeviceSchedulerValue.class);
        }
        else if (restore_data instanceof Object[]){
            ret =  new DeviceSchedulerValue((Object[]) restore_data);
        }

        return ret;
    }

    @Override
    public boolean equals(Object other){
        if (other == null){
            return false;
        }else if (! (other instanceof DeviceSchedulerValue))
        {
            return false;
        }
        else {
            DeviceSchedulerValue right  = (DeviceSchedulerValue)other;

            return Math.abs(right.schedulerTime - schedulerTime) <= PRECISION
                    && Math.abs(right.upTime - upTime) <= PRECISION
                    && right.deviceName.equalsIgnoreCase(deviceName)
                    && right.stratum == stratum;
        }
    }

    @Override
    public String toString() {
        return deviceName + " " + (long) upTime + " " + (long) schedulerTime + " " + stratum;
    }

    @Override
    public int compareTo(Object o) {
        //
        DeviceSchedulerValue right = (DeviceSchedulerValue)o;

        // Lower stratum
        if (stratum < right.stratum){
            return -1;
        }
        else if (stratum > right.stratum)
        {
            return 1;
        }
        // higher uptime
        else if (upTime > right.upTime){
            return -1;
        }
        else if (upTime < right.upTime)
        {
            return 1;
        }
        else {
            return 0;
        }
    }

    /**
     * The device name of the device this represents
     * @return Device Name
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * The JVM uptime of the device this represents in milliseconds
     * @return JVM uptime
     */
    public double getUpTime() {
        return upTime;
    }

    /**
     * The time that the {@link HBScheduler} of the device this represents has it's time set to
     * @return the {@link HBScheduler} time of device
     */
    public double getSchedulerTime() {
        return schedulerTime;
    }

    /**
     * The Stratum of the device this represents
     * @return the stratum of the device
     */
    public int getStratum() {
        return stratum;
    }

    /**
     * The last JVM time of <b>THIS</b> device in milliseconds that we received a message from the device that this object represents
     * @return the JVM time of <b>THIS</b> Device when last message was received
     */
    public double getLastUpdateJVMTime() {
        return lastUpdateJVMTime;
    }
}
