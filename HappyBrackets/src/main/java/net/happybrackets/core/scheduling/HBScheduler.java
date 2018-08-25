package net.happybrackets.core.scheduling;

import java.util.PriorityQueue;

/**
 * Stores events with the time they need to be executed and executes them when they are due
 */
public class HBScheduler {

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
     * Run the schedule thread
     */
    private void runSchedule(){
        // set our reference time when thread starts
        AverageCalculator lagCalulator = new AverageCalculator();

        while (!exitThread) {
            synchronized (scheduleObject) {
                try {

                    //System.out.println("Wait " + waitTime);
                    // we will flag if we timed out or whether we received a notification
                    reschedulerReTriggered = false;
                    // Now wait for the event


                    // calculate how long we need to wait and reduce by the average lag time
                    double waitTime = nextScheduledTime - getSchedulerTime() -  lagCalulator.averageValue();

                    // don't allow a time of zero or less
                    if (waitTime <= 0) {
                        waitTime = 1;
                    }

                    // mark the time we started the wait
                    double start_wait = getSchedulerTime();

                    if (needsReschedule()) {
                        adjustScheduler();
                        reschedulerReTriggered = true;
                        scheduleObject.wait(RESCHEDULE_INCREMENT);
                    }else {
                        // we need to round down our Milliseconds
                        double wait_ms = waitTime - 0.5;

                        // now add our nanoseconds
                        double ns_wait = (waitTime - wait_ms) * 1000000;

                        scheduleObject.wait((long) wait_ms, (int) ns_wait);
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
                    }
                    else {
                        // set our thread notified flag to false - it will get set if a new schehule has been added
                        reschedulerReTriggered = false;
                    }

                    // define the
                    double schedule_threshold = getSchedulerTime() + lagCalulator.averageValue();

                    // now let us iterate through priority queue to see what needs to be actioned
                    while (nextScheduledTime < schedule_threshold) {
                        //System.out.println("While " + nextScheduledTime  + " < " + schedule_threshold);
                        // see if next item is due
                        ScheduledObject next_item = scheduledObjects.peek();
                        if (next_item == null) {
                            nextScheduledTime = current_time + WAIT_MAX;
                        } else {
                            if (next_item.getScheduledTime() <= schedule_threshold) {
                                // this is it pop it off front first
                                scheduledObjects.poll();

                                // now notify the listener for it
                                if (!next_item.isCancelled()) {
                                    next_item.getScheduledEventListener().doScheduledEvent(next_item.getScheduledTime(), next_item.getScheduledObject());
                                }

                            } else { // our next scheduled item is at front of queu
                                nextScheduledTime = next_item.getScheduledTime();
                            }
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
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

        synchronized (scheduleObject){
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
        return ret;
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
        synchronized (scheduleObject){
            // see if our time is less than next time
            scheduleSlideTime = 0;
            scheduleSlideTime = getSchedulerTime() + new_time;


            if (new_time < nextScheduledTime){
                nextScheduledTime = new_time;
                reschedulerReTriggered = true;

                scheduleObject.notify();
            }

        }
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
        double time_remaining = completeRescheduleTime - getUptime();
        if (time_remaining < RESCHEDULE_INCREMENT){
            scheduleSlideTime += rescheduleAmount;
            rescheduleAmount = 0;

        }
        else{ // we are going to have to change only by a fraction of the amount
            double number_increments = time_remaining / RESCHEDULE_INCREMENT;

            double adjustment = rescheduleAmount /  number_increments;
            scheduleSlideTime += rescheduleAmount;
            rescheduleAmount -= adjustment;
        }

    }
}
