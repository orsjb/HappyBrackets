package net.happybrackets.core.scheduling;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.network.NetworkCommunication;
import net.happybrackets.device.sensors.DataSmoother;

import java.util.*;

/**
 * Singleton class for containing inter-device scheduling
 */
public class DeviceSchedules  {

    static final boolean SHOW_DEBUG = false;
    static final int SCHEDULE_ADJUST_TIME = 500;

    static final int LEAD_STRATUM = 10;
    static final int STARTUP_STRATUM =  LEAD_STRATUM + 1;
    static final double MAX_JIT_THRESHOLD_MS = 20; //
    static final float SUCCESS_CUDOS = 0.1f; // We will Decrement our faulre by this amount on each successful JVM match

    private static final Object creationLock = new Object();
    private static final Object listLock = new Object();

     SortedSet<DeviceSchedulerValue> deviceSchedulerValues = new TreeSet<>();
     HashMap<String, DeviceSchedulerValue> deviceSchedulerValueHashMap = new HashMap<>();


    private int stratum = STARTUP_STRATUM;

    /**
     * See if we are the current leading device on synchroniser network
     * @return true if this is the device that others are synchronising to
     */
    public boolean isLeadDevice(){
        boolean ret = false;
        if (numberDevices() > 0){
            synchronized (listLock){
                DeviceSchedulerValue first =  deviceSchedulerValues.first();
                ret = first.getDeviceName().equalsIgnoreCase(Device.getDeviceName());
            }
        }
        return ret;
    }

    /**
     * Return the number of devices synchonising on network
     * @return number devices
     */
    public int numberDevices(){
        return deviceSchedulerValues.size();
    }
    /**
     * Get the JVM Uptime
     * @return the JVM uptime in milliseconds
     */
    private double getUptime(){
        return HBScheduler.getUptime();
    }


    private static void showDebug(String text){
        if (SHOW_DEBUG) {
            System.out.println(text);
        }
    }
    /**
     * Get the Scheduled time of global {@link HBScheduler}
     * @return the scheduler time of this device
     */
    double getSchedulerTime(){
        return  HBScheduler.getGlobalScheduler().getSchedulerTime();
    }
    /**
     * Build our Local Time as we see it
     * @return the Local Device Time we are at
     */
    private DeviceSchedulerValue buildLocalDeviceSchedule() {
        localDeviceSchedule.schedulerTime = getSchedulerTime();
        localDeviceSchedule.upTime = getUptime();
        localDeviceSchedule.stratum = stratum;
        return localDeviceSchedule;
    }

    volatile DeviceSchedulerValue localDeviceSchedule = new DeviceSchedulerValue(Device.getDeviceName(), getUptime(), getSchedulerTime(), stratum);


    private static DeviceSchedules ourInstance = null;

    public static DeviceSchedules getInstance() {
        synchronized (creationLock){
            if (ourInstance == null){
                ourInstance = new DeviceSchedules();
            }
        }
        return ourInstance;
    }

    /**
     * Private constructor to ensure singleton
     */
    private DeviceSchedules() {
    }

    /**
     * Send the current Scheduler information about this device
     * @return true if able to send
     */
    public boolean sendCurrentTime(){
        // encode our message
        localDeviceSchedule = buildLocalDeviceSchedule();
        OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.CURRENT, localDeviceSchedule);

        return NetworkCommunication.sendNetworkOSCMessages(message, null, false);
    }

    /**
     * Check that two values are within our Maximum Jit Threshold
     * @param val_1 value 1
     * @param val_2 value 2
     * @return true if less than defined limit
     */
    private boolean valuesInThreshold (double val_1, double val_2){
        return Math.abs(val_1 - val_2) < MAX_JIT_THRESHOLD_MS;
    }

    /**
     * Get the total discards for all devices
     * @return to total of all discards
     */
    public float totalDiscardCount(){
        float ret = 0;
        List<DeviceSchedulerValue> devices =  getDeviceSchedulerValues();
        for (DeviceSchedulerValue device:
             devices) {
            ret += device.discardCount;
            ret += device.resetCount * device.jvmDifferences.getBuffSize();
        }

        return ret;
    }

    /**
     * Process a stratum message to advise us we should change our stratum
     * @param message the message containing stratum
     * @return true if we changed our stratum
     */
    public boolean processStratumMessage(DeviceStratumMessage message){
        boolean ret = false;

        if (message.deviceName.equalsIgnoreCase(localDeviceSchedule.deviceName)
                && message.stratum != stratum){
            stratum = message.stratum;
            sendCurrentTime();
            showDebug("Stratum Changed");

            ret = true;
        }
        return ret;
    }

    /**
     * Process reception of a DeviceSchedule message
     * @param source the source device that has sent this message
     * @param value the DeviceSchedulerValue message received
     */
    public  void processCurrentMessage(String source, DeviceSchedulerValue value) {
        String device_name = value.deviceName;

        synchronized (listLock) {
            // we will store our absolute time
            value.lastUpdateJVMTime = getUptime();

            double jvm_diff =  value.getUpTime() - value.lastUpdateJVMTime;

            if (deviceSchedulerValueHashMap.containsKey(device_name)) {
                // by just copying the values, we will be updating both the sorted set and the hashtable
                DeviceSchedulerValue stored = deviceSchedulerValueHashMap.get(device_name);

                DataSmoother stored_jvm_diffs =  stored.jvmDifferences;

                boolean store_value = true;

                if (!stored_jvm_diffs.dataPrimed()) {
                    if (stored_jvm_diffs.isEmpty()) {
                        stored.jvmDifferences.addValue(jvm_diff);
                        store_value = true;
                    }
                    else
                    {
                        if (valuesInThreshold(stored_jvm_diffs.getAverage(), jvm_diff)){
                            stored.jvmDifferences.addValue(jvm_diff);
                            store_value = true;
                        }
                        else
                        {
                            store_value = false;
                            // discard as it is outside jitter
                            stored.jvmDifferences.reset();
                            showDebug("Discard Not Primed " + stored.getDeviceName());
                        }
                    }
                } // !primed
                else // we are fully primed need to see if this has been affected by JIT
                {
                    if (valuesInThreshold(stored_jvm_diffs.getAverage(),jvm_diff)){
                        stored.jvmDifferences.addValue(jvm_diff);
                        store_value = true;

                    }
                    else
                    {
                        showDebug("Discard Primed " + stored.getDeviceName());
                        stored.discardCount++;
                        store_value = false;
                    }

                }
                if (store_value){
                    if (stored.copyValues(value)) { // The sort value may have changed
                        // we need to remove and then re-add otherwise it will not get sorted
                        deviceSchedulerValues.remove(stored);
                        deviceSchedulerValues.add(stored);
                    }

                    // we will allow our discard count to gradually reduce on each success
                    if (stored.discardCount > 0){
                        stored.discardCount -= SUCCESS_CUDOS;
                    }

                    // check that this message has come from the lead device that is not us
                    DeviceSchedulerValue first =  deviceSchedulerValues.first();

                    if (first.equals(stored) & !first.getDeviceName().equalsIgnoreCase(localDeviceSchedule.deviceName)) {
                        // we have a fully primed and stored other value
                        // if it is not us, it has a startup stratum and it has earlier JVM time,
                        // we will want to inform it to better its stratum
                        if (stored.stratum == STARTUP_STRATUM && stored_jvm_diffs.dataPrimed() && deviceSchedulerValues.size() > 1) {
                            // if the best JVM time is another device, inform it that we want it to have a better
                            // stratum to prevent a new device entering and taking over
                               sendDeviceStratum(first.getDeviceName(), LEAD_STRATUM);
                        } // end send stratum

                        // let us adjust our scheduler
                        adjustScheduler(first);

                    } // send stored == first

                }
                else {
                    if (stored.discardCount > stored_jvm_diffs.getBuffSize()){
                        stored.jvmDifferences.reset();
                        stored.discardCount = 0;
                        stored.resetCount++;

                        showDebug("Reset Smoother " + stored.getDeviceName());

                        // if we are having trouble with JVM Jitter, move this device to a worse stratum
                        if (stratum ==  STARTUP_STRATUM || stratum == LEAD_STRATUM) {
                            if (stored.getDeviceName().equalsIgnoreCase(localDeviceSchedule.deviceName)) {
                                stratum = STARTUP_STRATUM + 1;
                                showDebug("Lower Stratum " + stored.getDeviceName());
                            }
                            else if (totalDiscardCount() > stored.jvmDifferences.getBuffSize() * 2){
                                stratum = STARTUP_STRATUM + 1;
                                showDebug("Lower Stratum Multiple JVMs");

                            }
                        }

                    }
                }


            } else {
                deviceSchedulerValueHashMap.put(device_name, value);
                deviceSchedulerValues.add(value);

            }
        }


    }

    /**
     * Adjust the global {@link HBScheduler} so we synchronise to the lead device
     * @param first the lead device on our list
     */
    private void adjustScheduler(DeviceSchedulerValue first) {
        double estimated = first.estimateSchedulerTime(getUptime());
        double diff =  estimated - getSchedulerTime();
        if (Math.abs(diff) > MAX_JIT_THRESHOLD_MS / 2){
            HBScheduler.getGlobalScheduler().adjustScheduleTime(diff, SCHEDULE_ADJUST_TIME);
        }

    }

    /**
     * Send a message to Device that we want it to change it's startup stratum to this new one if
     * it is at the startup sratum
     * @param deviceName the name of the device
     * @param new_stratum new stratup we are setting to
     */
    private void sendDeviceStratum(String deviceName, int new_stratum) {
        DeviceStratumMessage adjustment = new DeviceStratumMessage(Device.getDeviceName(), new_stratum);

        OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.STRATUM, adjustment);
        NetworkCommunication.sendNetworkOSCMessages(message, null, false);

    }


    /**
     * Get the list of all scheduled devices we have listed
     * @return the list of all {@link DeviceSchedulerValue} we have stored
     */
    public  List<DeviceSchedulerValue> getDeviceSchedulerValues(){
        List<DeviceSchedulerValue> ret = new ArrayList<>();

        synchronized (listLock) {
            for (DeviceSchedulerValue value :
                    deviceSchedulerValues) {
                ret.add(value);
            }
        }
        return ret;
    }


    /**
     * Return the range of time in milliseconds between the maximum and minimum scheduled times
     * @return the difference between the minimum estimated schedule and maximum estimated scheduled times
     */
    public  double timeRange(){
        double jvmTime = getUptime();
        double ret =  0;

        if (deviceSchedulerValues.size() > 0){
            boolean started = false;

            double max = 0;
            double min = 0;

            synchronized (listLock) { // synchronise our list
                for (DeviceSchedulerValue value :
                        deviceSchedulerValues) {
                    double estimated = value.estimateSchedulerTime(jvmTime);
                    if (!started) {
                        max = estimated;
                        min = estimated;
                        started = true;
                    } else {
                        if (estimated > max) {
                            max = estimated;
                        }
                        if (estimated < min) {
                            min = estimated;
                        }
                    }
                }
            }

            ret = max - min;
        }

        return ret;

    }

    /**
     * If the Device marked as the lead device has not been heard for a certain time, we will remove it from our list
     * @param expiration_millisecond the amount of milliseconds that must expire before we decide to remove it
     * @return true if a device was removed
     */
    public boolean removeExpiredLead(long expiration_millisecond){
        boolean ret = false;

        try {
            DeviceSchedulerValue front = deviceSchedulerValues.first();

            if (front != null) {
                double elapsed = getUptime() - front.lastUpdateJVMTime;
                ret = elapsed > expiration_millisecond;
                if (ret) {
                    synchronized (listLock) {
                        deviceSchedulerValues.remove(front);
                        deviceSchedulerValueHashMap.remove(front.getDeviceName());
                    }
                }
            }
        }
        catch (Exception ex){}

        return ret;
    }

    /**
     * Get the device that is listed as the first device in our list
     * @return the Front device
     */
    public DeviceSchedulerValue getLeadingDevice(){
        DeviceSchedulerValue ret = localDeviceSchedule;

        synchronized (listLock) {
            if (deviceSchedulerValues.size() > 0){
                ret = deviceSchedulerValues.first();
            }
        }
        return ret;
    }

}
