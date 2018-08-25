package net.happybrackets.core.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clock Class that uses HBScheduler
 */
public class Clock implements ScheduledEventListener {

    /**
     * The minimum interval we will allow in milliseconds
     */
    final double MIN_INTERVAL = 2;

    /// have a copy of our pending scheduled object in case we want to cancel it
    ScheduledObject pendingSchedule = null;

    // store a local copy for efficiency
    // we wont make static because we don't want to make one unless necessary
    private HBScheduler clockScheduler;

    // Define a variable that we can use to shift our clock time
    // Once we action the shift, we will set it to zero
    private double shiftTime = 0;

    // create a flag to set cancel while we are in  clock test
    private volatile boolean doCancel = false;

    @Override
    public void doScheduledEvent(double scheduledTime, Object param) {
        // we no longer have pending. Clear that first
        synchronized (this) {
            // first see if we have been cancelled or not
            if (isRunning()) {
                setPendingSchedule(null);
                numberTicks++;
                // let our listeners know
                synchronized (clockTickListeners) {
                    // calculate how late or early we are
                    double offset = clockScheduler.getSchedulerTime() - scheduledTime;

                    for (ClockTickListener listener : clockTickListeners) {
                        listener.clockTick(offset, this);
                    }

                    if (!doCancel) {
                        // now lets set our next time - the scheduler itself will account for any extra time taken
                        double next_time = scheduledTime + clockInterval + shiftTime;
                        shiftTime = 0;

                        pendingSchedule = clockScheduler.addScheduledObject(next_time, this, this);
                    }
                }
            }
        }
    }

    /**
     * ClockTick interface for receiving clock Tick events
     */
    public interface ClockTickListener {
        /**
         * Event occurs when clock tick occurs
         * @param offset the number of milliseconds we are off the tick. Positive number is late, negative is early
         * @param clock the clock object sending the message
         */
        void clockTick(double offset, Clock clock );
    }

    private double clockInterval;

    private double startTime;

    /**
     * Return the number of clock ticks
     * @return number of ticks
     */
    public long getNumberTicks() {
        return numberTicks;
    }

    private long numberTicks =  0;

    private List<ClockTickListener> clockTickListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructor using default Scheduler
     * @param interval the interval in milliseconds Must be greater than 2 milliseconds otherwise could lock up
     */
    public Clock(double interval){

        this(interval, HBScheduler.getGlobalScheduler());

    }

    /**
     * Constructor
     * @param interval the interval in milliseconds Must be greater than 2 milliseconds otherwise could lock up
     * @param scheduler the scheduler to use
     */
    public Clock(double interval, HBScheduler scheduler){

        clockScheduler = scheduler;

        startTime = clockScheduler.getSchedulerTime();

        if (interval < MIN_INTERVAL){
            clockInterval = MIN_INTERVAL;
        }
        else {
            clockInterval = interval;
        }
    }

    /**
     * See if clock is running
     * @return true if clock is running
     */
    public synchronized  boolean isRunning(){
        return pendingSchedule != null;
    }

    /**
     * Reset clock. If clock is running, it will restart the clock to current time
     */
    public synchronized void reset(){
        boolean was_running = isRunning();
        stop();
        if (was_running){
            start();
        }
    }

    /**
     * Set our pendingSchedule object within a synchronized function
     * @param pending the new pendingSchedule
     */
    private synchronized void setPendingSchedule(ScheduledObject pending){
        pendingSchedule = pending;
    }

    public synchronized void stop(){
        if (pendingSchedule != null){
            pendingSchedule.setCancelled(true);
            setPendingSchedule(null);
        }
        doCancel = true;
    }
    /**
     * Start the clock
     * @return this
     */
    public synchronized Clock start(){
        if (pendingSchedule == null) {
            numberTicks = 0;
            startTime = clockScheduler.getSchedulerTime();
            double next_time =  startTime + clockInterval;

            doCancel = false;
            pendingSchedule = clockScheduler.addScheduledObject(next_time, this, this);

        }
        return this;
    }

    /**
     * Add a listener for this clock
     * @param listener the listener
     * @return this
     */
    public Clock addClockTickListener(ClockTickListener listener){
        synchronized (clockTickListeners){
            clockTickListeners.add(listener);
        }

        return this;
    }

    /**
     * Removes a listener for this clock
     * @param listener the listener to remove
     */
    public void removeClockTickListener(ClockTickListener listener){
        synchronized (clockTickListeners){
            clockTickListeners.remove(listener);
        }
    }


    /**
     * Clears all listeners for this clock
     */
    public void clearClockTickListener(){
        synchronized (clockTickListeners){
            clockTickListeners.clear();
        }
    }


    /**
     * Shift the clock forwards or backwards an amount of time
     * @param shift_time the amount of milliseconds to shift clock
     */
    public synchronized void shiftTime(double shift_time){
        shiftTime = shift_time;
    }

}

