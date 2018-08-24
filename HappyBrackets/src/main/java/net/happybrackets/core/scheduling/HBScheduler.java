package net.happybrackets.core.scheduling;

import java.lang.management.ManagementFactory;
import java.util.PriorityQueue;

/**
 * Stores events with the time they need to be executed and executes them when they are due
 */
public class HBScheduler {

    private static HBScheduler globalScheduler = null;

    private final int WAIT_MAX = Integer.MAX_VALUE;

    private boolean exitThread = false;

    // this object will act as timer, causing thread to wait for next scheduled time
    private final Object scheduleObject = new Object();


    double referenceTime = getUptime();

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
    volatile boolean threadNotified = false;

    // we will cache the value of what will be at the top of priority queue to save on a function call
    double nextScheduledTime = WAIT_MAX;
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
        Thread scheduleThread = new Thread(new Runnable() {
            public void run() {
                runSchedule();

            }
        });
        scheduleThread.setPriority(priority);
        scheduleThread.start();

    }

    void runSchedule(){
        // set our reference time when thread starts
        referenceTime = getUptime();
        AverageCalculator lagCalulator = new AverageCalculator();
        int count = 1;
        double waitTime = WAIT_MAX;
        double expected_time = WAIT_MAX + getElapsedTime();

        while (!exitThread) {
            synchronized (scheduleObject) {
                try {
                    // first mark the time we started the wait
                    double start_wait = getElapsedTime();

                    //System.out.println("Wait " + waitTime);
                    // we will flag if we timed out or whether we received a notification
                    threadNotified = false;
                    // Now wait for the event



                    // we need to round down our Milliseconds
                    double wait_ms =  waitTime - 0.5;
                    double ns_wait = (waitTime - wait_ms) * 1000000;

                    scheduleObject.wait((long)wait_ms, (int)ns_wait);
                    double current_time = getElapsedTime();

                    //System.out.println("Wait end");
                    // see if we have a timed wait.
                    // if we do, then we need to calculate extra time we waited
                    if (!threadNotified) {
                        // se how much time we actually waited

                        double actual_wait = current_time - start_wait;
                        double lag = actual_wait - waitTime;
                        lagCalulator.addValue(lag);
                    }


                    double schedule_threshold = getElapsedTime() + lagCalulator.averageValue();

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

                    // calculate how long we need to wait and reduce by the average lag time
                    waitTime = nextScheduledTime - getElapsedTime() -  lagCalulator.averageValue();

                    // don't allow a time of zero or less
                    if (waitTime <= 0) {
                        waitTime = 1;
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
                nextScheduledTime = (long)scheduled_time;
                threadNotified = true;
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
    public double getElapsedTime(){
        return getUptime() - referenceTime;
    }
}
