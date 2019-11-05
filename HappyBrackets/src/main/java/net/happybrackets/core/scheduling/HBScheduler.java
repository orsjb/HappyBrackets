package net.happybrackets.core.scheduling;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.CustomGlobalEncoder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Hashtable;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Stores events with the time they need to be executed and executes them when they are due
 */
public class HBScheduler {

    enum MESSAGE_PARAMS{
        DEVICE_NAME,
        MESSAGE_ID,
        OBJ_VAL
    }

    // we will use 1 Dec 2018 as our global reference start clock
    static public final LocalDateTime REFERENCE_TIME = LocalDateTime.ofEpochSecond(1543622400, 0, ZoneOffset.UTC);

    private static HBScheduler globalScheduler = null;

    // when we are doing a reschedule, we will move our schedule reference clock by this much
    private final int RESCHEDULE_INCREMENT = 10;

    private final int WAIT_MAX = Integer.MAX_VALUE;

    private boolean exitThread = false;

    // this object will act as timer, causing thread to wait for next scheduled time
    private final Object scheduleObject = new Object();


    final double SCHEDULE_START_TIME = getUptime(); // This basically sets our Uptime to look like zero


    boolean displayNotify = false;

    // Set the max - 1 so audio will have higher priority
    static int defaultPriority = Thread.MAX_PRIORITY - 1;


    static int deviceSendId = 0; // we will increment these per message


    /**
     * Use a flag to set this as Controller so we can send messages from simulator as well as controller
     */
    public static void setDeviceController() {
        HBScheduler.deviceSendId = Integer.MAX_VALUE / 2;
    }

    /**
     * Set the default priority of thread.
     * @param priority thread priority
     */
    static void setDefaultPriority(int priority){
        defaultPriority = priority;
    }

    // we are going to determine if our thread was notified
    volatile boolean reschedulerReTriggered = false;

    // we will cache the value of what will be at the top of priority queue to save on a function call
    volatile double nextScheduledTime;

    // we will map Message ID to device name. If the last ID is in this map, we will ignore message
    static Map<String, Integer> messageIdMap = new Hashtable<>();


    /**
     * See if we will process a control message based on device name and message_id
     * If the message_id is mapped against the device_name, ignore message, otherwise store mapping and return true;
     * @param device_name the device name
     * @param message_id the message_id
     * @return true if we are going to process this message
     */
    public static boolean enableProcessControlMessage(String device_name, int message_id){
        boolean ret = true;

        if (messageIdMap.containsKey(device_name)) {
            if (messageIdMap.get(device_name) == message_id) {
                ret = false;
            }
        }

        if (ret){
            messageIdMap.put(device_name, message_id);
        }

        return ret;
    }
    // define variables for adjusting our scheduler
    private double rescheduleAmount = 0; // this is how much we need to adjust our scheduler by
    private double completeRescheduleTime = getUptime(); // The absolute time our rescheduling needs to be completed by based on getUpTime
    volatile private double scheduleSlideTime = 0; // this is the slider we use to move our elapsed time when we adjust scheduler
    /**
     * This is our actual store of scheduled objects
     */
    private PriorityQueue<ScheduledObject> scheduledObjects = new PriorityQueue<>();

    /**
     * Get singleton instance of HBScheduler
     * @return the single static instance with maximum priority
     */
    public static synchronized HBScheduler getGlobalScheduler(){
        if (globalScheduler == null){
            globalScheduler = new HBScheduler(defaultPriority);
        }
        return globalScheduler;
    }
    /**
     * Set flag to display notify messages to stdout for debugging and testing
     */
    public void displayNotifyMessage(){
        displayNotify = true;
    }
    /**
     * Create a scheduler object
     * @param priority the priority we want it to run. Thread.MAX_PRIORITY is highest
     */
    public HBScheduler (int priority) {
        // set out next schedule time as maximum
        nextScheduledTime = getSchedulerTime() + WAIT_MAX;

        Thread scheduleThread = new Thread(new Runnable() {
            public void run() {
                runSchedule();

            }
        });
        scheduleThread.setPriority(priority);
        scheduleThread.start();

    }

    /**
     * Get the amount of time difference between system clock and our scheduler
     * @return the amount of time difference between two clocks
     */
    public double getClockSkew(){
        double current = getCalcTime();
        double scheduler_time = getSchedulerTime();

        double schedule_diff = current - scheduler_time;
        return schedule_diff;
    }
    /**
     * Synchronise our timer with the system time on next tick
     * @return the number of milliseconds we will be moving scheduler
     */
    public double synchroniseClocks(){
        return synchroniseClocks(0);
    }

    /**
     * Synchronise our timer with the system time
     * @param slew_time the amount of milliseconds that we want to take to complete it
     * @return the number of milliseconds we will be moving scheduler
     */
    public double synchroniseClocks(long slew_time){

        double schedule_diff = getClockSkew();
        adjustScheduleTime(schedule_diff, slew_time);
        return schedule_diff;
    }

    /**
     * Get the calculated time in ms from 1 Dec 2018 from System Clock
     * @return number of milliseconds since 1 Dec 2018 GMT
     */
    static public double getCalcTime(){
        // get current time in GMT
        final LocalDateTime gmt = LocalDateTime.now(ZoneOffset.UTC);

        Duration gmt_diff =  Duration.between(REFERENCE_TIME, gmt);
        // compare to our reference
        double calc_gmt = gmt_diff.toNanos() / 1000000d;
        return calc_gmt;
    }

    /**
     * Run the schedule thread
     */
    private void runSchedule() {
        // set our reference time when thread starts
        AverageCalculator lagCalulator = new AverageCalculator();

        double start_wait = getSchedulerTime();
        double waitTime = nextScheduledTime - getSchedulerTime() - lagCalulator.averageValue();

        while (!exitThread) {
            synchronized (scheduleObject) {
                try {

                    displayDebug("runSchedule ");
                    //System.out.println("Wait " + waitTime);
                    // we will flag if we timed out or whether we received a notification
                    reschedulerReTriggered = false;
                    // Now wait for the event


                    // calculate how long we need to wait and reduce by the average lag time
                    waitTime = nextScheduledTime - getSchedulerTime() - lagCalulator.averageValue();

                    // don't allow a time of zero or less
                    if (waitTime <= 0) {
                        waitTime = 1;
                    }

                    // mark the time we started the wait
                    start_wait = getSchedulerTime();

                    if (needsReschedule()) {
                        adjustScheduler();
                        reschedulerReTriggered = true;
                        displayDebug("runSchedule increment");
                        scheduleObject.wait(RESCHEDULE_INCREMENT);
                    } else {
                        // we need to round down our Milliseconds
                        double wait_ms = waitTime - 0.5;

                        // now add our nanoseconds
                        double ns_wait = (waitTime - wait_ms) * 1000000;

                        displayDebug("runSchedule round down");
                        scheduleObject.wait((long) wait_ms, (int) ns_wait);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            double current_time = getSchedulerTime();

            // see if we have a timed wait.
            // If a new item has been added or reschedule required then we did not wait the full time and we are just recalculating what our new start point will be
            // if we do, then we need to calculate extra time we waited
            if (!reschedulerReTriggered) {
                // se how much time we actually waited

                double actual_wait = current_time - start_wait;
                double lag = actual_wait - waitTime;
                lagCalulator.addValue(lag);
            } else {
                // set our thread notified flag to false - it will get set if a new schehule has been added
                reschedulerReTriggered = false;
            }

            // define the
            double schedule_threshold = getSchedulerTime() + lagCalulator.averageValue();

            // now let us iterate through priority queue to see what needs to be actioned
            while (nextScheduledTime < schedule_threshold) {
                displayDebug("runSchedule less next rescheduled ");
                //System.out.println("While " + nextScheduledTime  + " < " + schedule_threshold);
                // see if next item is due
                // we need to put this only in places to protect the priority queue
                ScheduledObject next_item = null;

                boolean run_schedule = false;

                synchronized (scheduleObject) {
                    next_item = scheduledObjects.peek();

                    if (next_item == null) {
                        nextScheduledTime = current_time + WAIT_MAX;
                    } else {
                        if (next_item.getScheduledTime() <= schedule_threshold) {
                            // this is it pop it off front first
                            displayDebug("runSchedule Poll");
                            scheduledObjects.poll();

                            // now notify the listener for it
                            if (!next_item.isCancelled()) {
                                run_schedule = true;
                            }

                        } else { // our next scheduled item is at front of queue
                            nextScheduledTime = next_item.getScheduledTime();
                        }
                    }
                } // end synchronised

                if (run_schedule) {
                    displayDebug("runSchedule Do schedule");
                    next_item.getScheduledEventListener().doScheduledEvent(next_item.getScheduledTime(), next_item.getScheduledObject());
                    displayDebug("runSchedule Schedule done");
                }
            }


        }
        displayDebug("runSchedule Complete");
    }

    /**
     * End our scheduler. Any items scheduled will be deleted
     */
    public  void endScheduler() {
        exitThread = true;
        synchronized (scheduleObject) {
            scheduledObjects.clear();
            scheduleObject.notifyAll();
        }

    }

    /**
     * Erase all Scheduled objects from clock
     */
    public void reset(){
        synchronized (scheduleObject) {
            scheduledObjects.clear();
        }
    }
    /**
     * Add an object that needs to be notified when its scheduled time has occurred
     * If the scheduled time on this item is less than the current next scheduled item
     * We will notify the scheduleObject so it waits the appropriate time
     * @param scheduled_time the time the item needs to be scheduled for
     * @param param the parameter to be passed back to the listener
     * @param listener the listener to be called when the scheduled event is supposed to occur
     * @return the scheduled object that has been added
     */
    public  ScheduledObject addScheduledObject(double scheduled_time, Object param, ScheduledEventListener listener){

        ScheduledObject ret = new ScheduledObject(scheduled_time, param, listener);

        displayDebug("addScheduledObject Object");
        synchronized (scheduleObject){
            displayDebug("Lock Obtained");
            scheduledObjects.add(ret);
            // see if our time is less than next time
            if (scheduled_time < nextScheduledTime){
                nextScheduledTime = scheduled_time;
                reschedulerReTriggered = true;
                if (displayNotify) {
                    System.out.println("Notify " + scheduled_time);
                }

                scheduleObject.notify();
            }

        }
        displayDebug("addScheduledObject leave");
        return ret;
    }

    private void displayDebug(String debug){
        if (displayNotify){
            StackTraceElement l = new Exception().getStackTrace()[1];
            System.out.println(
                    l.getClassName()+"/"+l.getMethodName()+":"+l.getLineNumber());

            System.out.println(debug);
        }
    }
    /**
     * Get the time JVM has been running
     * @return the time JVM has been running
     */
    private double getUptime() {

        double ret = System.nanoTime();

        return  ret / 1000000;
    }


    /**
     * Get the amount of time elapsed since we set reference time
     * @return the elapsed time in milliseconds
     */
    public double getSchedulerTime(){
        return getUptime() - SCHEDULE_START_TIME + scheduleSlideTime;
    }

    /**
     * Set the scheduled time to this time
     * @param new_time the new time
     */
    public void setScheduleTime(double new_time){
        adjustScheduleTime (new_time - getSchedulerTime(), 0);
    }
    /**
     * Adjust the scheduler time
     * @param amount the amount of milliseconds we need to adjust our time by. A positive amount will advance the scheduler
     * @param duration the number of milliseconds over which we want this change to occur so we don't just get a jump
     */
    public void adjustScheduleTime(double amount, long duration){
        if (duration < 0)
        {
            duration = 0;
        }

        synchronized (scheduleObject) {
            rescheduleAmount = amount;
            // set the time this needs to be completed by
            completeRescheduleTime = getUptime() + duration;
            reschedulerReTriggered = true;
            nextScheduledTime = getSchedulerTime();
            // new retrigger our timer
            scheduleObject.notify();
        }

    }

    /**
     * Test if our reschedule amount is zero
     * @return true if we need to reschedule
     */
    private boolean needsReschedule(){
        return  (Math.abs(rescheduleAmount) > 2 * Double.MIN_VALUE);
    }

    /**
     * We are going to adjust our rescheduler by the amount
     * This will be done inside our schedule function only
     */
    private void adjustScheduler(){
        double uptime = getUptime();

        double time_remaining = completeRescheduleTime - uptime;
        if (time_remaining < RESCHEDULE_INCREMENT){
            scheduleSlideTime += rescheduleAmount;
            rescheduleAmount = 0;

        }
        else{ // we are going to have to change only by a fraction of the amount
            double number_increments = time_remaining / RESCHEDULE_INCREMENT;

            double adjustment = rescheduleAmount /  number_increments;
            scheduleSlideTime += adjustment;
            rescheduleAmount -= adjustment;
        }

    }

    /**
     * Build OSC Message that specifies a Network update
     * @param OSC_MessageName The OSC Message
     * @param adjustment the ClockAdjustment we are making
     * @return OSC Message directed to controls with same name, scope, but on different devices
     */
    static public OSCMessage buildNetworkSendMessage(String OSC_MessageName, ClockAdjustment adjustment) {

        deviceSendId++;
            /*
            DEVICE_NAME,
            MESSAGE_ID,
            OBJ_VAL,
            */
        // we need to see if we have a custom encode function
        Object[] encode_data = adjustment.encodeGlobalMessage();
        int num_args = encode_data.length + MESSAGE_PARAMS.OBJ_VAL.ordinal();

        Object[] osc_args = new Object[num_args];
        osc_args[MESSAGE_PARAMS.DEVICE_NAME.ordinal()] = Device.getDeviceName();
        osc_args[MESSAGE_PARAMS.MESSAGE_ID.ordinal()] = deviceSendId;


        // now encode the object parameters
        for (int i = 0; i < encode_data.length; i++) {
            osc_args[MESSAGE_PARAMS.OBJ_VAL.ordinal() + i] = encode_data[i];
        }

        return new OSCMessage(OSC_MessageName,
                osc_args);
    }

    /**
     * Process our Scheduler messages
     * @param msg the OSC message we are processing
     * @return true if we process this message
     */
    static public boolean ProcessSchedulerMessage(OSCMessage msg) {
        boolean ret = false;

        // This will be a message of type DynamicContrrol Network Message. So we will decode
        String device_name = (String) msg.getArg(MESSAGE_PARAMS.DEVICE_NAME.ordinal());

        int message_id = (int) msg.getArg(MESSAGE_PARAMS.MESSAGE_ID.ordinal());


        if (enableProcessControlMessage(device_name, message_id)) {
            Object[] values = new Object[msg.getArgCount() - MESSAGE_PARAMS.OBJ_VAL.ordinal()];

            for (int i = 0; i < values.length; i++) {
                values[i] = msg.getArg(MESSAGE_PARAMS.OBJ_VAL.ordinal() + i);
            }

            ClockAdjustment adjustment = new ClockAdjustment().restore(values);


            if (OSCVocabulary.match(msg, OSCVocabulary.SchedulerMessage.SET)) {
                getGlobalScheduler().setScheduleTime(adjustment.getAdjustmentAmount());
                ret = true;
            } else if (OSCVocabulary.match(msg, OSCVocabulary.SchedulerMessage.ADJUST)) {
                getGlobalScheduler().adjustScheduleTime(adjustment.getAdjustmentAmount(), adjustment.getAdjustmentDuration());
                ret = true;
            }
        }


        return ret;
    }

}
