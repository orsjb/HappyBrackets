package net.happybrackets.core.scheduling;

import java.lang.management.ManagementFactory;
import java.util.PriorityQueue;

/**
 * Stores events with the time they need to be executed and executes them when they are due
 */
public class HBScheduler {

    private final int WAIT_MAX = Integer.MAX_VALUE;

    private boolean exitThread = false;

    // this object will act as timer, causing thread to wait for next scheduled time
    private Object scheduleObject = new Object();

    long referenceTime = getUptime();


    // we are going to determine if our thread was notified
    volatile boolean threadNotified = false;

    // we will cache the value of what will be at the top of priority queue to save on a function call
    long nextScheduledTime = 0;
    /**
     * This is our actual store of scheduled objects
     */
    private PriorityQueue<ScheduledObject> scheduledObjects = new PriorityQueue<>();

    /**
     * Create a scheduler object
     * @param priority the priority we want it to run. Thread.MAX_PRIORITY is highest
     */
    public HBScheduler (int priority)
    {
        Thread scheduleThread = new Thread(new Runnable() {
            public void run() {
                // set our reference time when thread starts
                referenceTime = getUptime();
                AverageCalculator lagCalulator = new AverageCalculator();
                int count = 1;
                long waitTime = WAIT_MAX;
                long expected_time = WAIT_MAX + getElapsedTime();

                while (!exitThread){
                    synchronized (scheduleObject){
                        try {
                            // first mark the time we started the wait
                            long start_wait = getElapsedTime();

                            // Now wait for the event
                            scheduleObject.wait(waitTime);
                            long current_time = getElapsedTime();

                            // see if we have a timed wait.
                            // if we do, then we need to calculate extra time we waited
                            if (!threadNotified) {
                                // se how much time we actually waited

                                long actual_wait = current_time - start_wait;
                                long lag = actual_wait - waitTime;
                                lagCalulator.addValue(lag);
                            }

                            long schedule_threshold = getElapsedTime() + (long)lagCalulator.averageValue();

                            // now let us iterate through priority queue to see what needs to be actioned
                            while(nextScheduledTime <= schedule_threshold)
                            {
                                // see if next item is due
                                ScheduledObject next_item = scheduledObjects.peek();
                                if (next_item.getScheduledTime() <= schedule_threshold){
                                    // pop it off front - it is time
                                    scheduledObjects.poll();
                                    //next_item.getScheduledEventListener().doScheduledEvent();
                                }
                            }
                                // determine what time we ae supposed to wait until next time
                                count++;
                                long next_time = count * WAIT_MAX;

                                // how late are we in receiving this message
                                long how_late = current_time - expected_time;

                                // store our new expected time expected_time
                                expected_time = next_time;

                                // calculate how long we need to wait and reduce by the average lag time
                                waitTime = expected_time - current_time - (long)lagCalulator.averageValue() ;
                                // display our values
                                //System.out.println(lag + " " + current_time + "(" + how_late + ")");


                            // don't allow a time of zero or less
                            if (waitTime <= 0){
                                waitTime = 1;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        });
        scheduleThread.setPriority(priority);
        scheduleThread.start();

    }


    /**
     * End our scheduler. Any items scheduled will be deleted
     */
    public void endScheduler() {
        exitThread = true;
        synchronized (scheduleObject) {
            scheduledObjects.clear();
            scheduleObject.notifyAll();
        }

    }

    /**
     * Add an object that needs to be notified when its scheduled time has occurred
     * If the scheduled time on this item is less than the current next scheduled item
     * We will notify the scheduleObject so it waits the appropriate time
     * @param scheduled_time the time the item needs to be scheduled for
     * @param param the parameter to be passed back to the listener
     * @param listener the listener to be called when the scheduled event is supposed to occur
     */
    public void addScheduledObject(long scheduled_time, Object param, ScheduledEventListener listener){

        synchronized (scheduleObject){
            scheduledObjects.add(new ScheduledObject(scheduled_time, param, listener));
            // see if our time is less than next time
            if (scheduled_time < nextScheduledTime){
                scheduled_time =  nextScheduledTime;
                threadNotified = true;
                scheduleObject.notify();
            }
        }
    }
    /**
     * Get the time JVM has been running
     * @return the time JVM has been running
     */
    private long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }


    /**
     * Get the amount of time elapsed since we set reference time
     * @return the elapsed time in milliseconds
     */
    private long getElapsedTime(){
        return getUptime() - referenceTime;
    }
}
