package net.happybrackets.core.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clock Class that uses HBScheduler
 */
public class Clock implements ScheduledEventListener {

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




    private double clockInterval;

    private double startTime;

    /**
     * The interval of the clock
     * @return the clock interval
     */
    public double getClockInterval() {
        return clockInterval;
    }

    /**
     * Return the number of clock ticks
     * @return number of ticks
     */
    public long getNumberTicks() {
        return numberTicks;
    }

    private long numberTicks =  0;

    private final Object clockTickListenersLock = new Object();
    private List<ClockTickListener> clockTickListeners = new ArrayList<>();

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
     * Change the interval of the clock and start from now
     * @param interval the new clock interval
     */
    public synchronized void setInterval(double interval){
        clockInterval = interval;
        if (isRunning()){
            stop();
            start();
        }
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
        synchronized (clockTickListenersLock){
            clockTickListeners.add(listener);
        }

        return this;
    }

    /**
     * Removes a listener for this clock
     * @param listener the listener to remove
     */
    public void removeClockTickListener(ClockTickListener listener){
        synchronized (clockTickListenersLock){
            clockTickListeners.remove(listener);
        }
    }


    /**
     * Clears all listeners for this clock
     */
    public void clearClockTickListener(){
        synchronized (clockTickListenersLock){
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

    /**
     * Calculate the interval required to create a clock with a defined beats per minute
     * Will throw an illegal argument exception if you pass in zero
     * @param beats_per_minute beats per minute
     * @return the number of microseconds between beats
     * @throws IllegalArgumentException if the beats_per minute is zero
     */
    public static double BPM2Interval(double beats_per_minute) throws IllegalArgumentException {

        if (beats_per_minute == 0) {
            throw new IllegalArgumentException("Argument 'beats_per_minute' is 0");
        }

        return 60 / beats_per_minute * 1000;
    }


    /**
     * Calculate number of Beats Per Minute given an interval between beats
     * @param milliseconds the number of milliseconds between beats
     * @return the calculated beats per minute
     * @throws IllegalArgumentException if milliseconds is zero
     */
    public static double Interval2BPM (double milliseconds) throws IllegalArgumentException{
        if (milliseconds == 0) {
            throw new IllegalArgumentException("Argument 'milliseconds' is 0");
        }

        return 60 / (milliseconds / 1000);
    }

    /**
     * Synchronise this clock to another one
     * @param master the clock we are synchronising with
     * @return the amount of time we have shifted this clock
     */
    public synchronized double synchronizeClock (Clock master){
        double ret = 0;

        // check our mast clock is running
        if (master.isRunning()){
            // get the next schedule time we require
            double master_schedule_time = master.shiftTime + master.pendingSchedule.getScheduledTime();
            // if we are running, caculate a difference
            if (isRunning()){
                double current_scheduled_time = shiftTime + pendingSchedule.getScheduledTime();
                ret = master_schedule_time - current_scheduled_time;
            }

            stop();
            // now add our schedule
            doCancel = false;
            pendingSchedule = clockScheduler.addScheduledObject(master_schedule_time, this, this);

        }

        return ret;
    }

    @Override
    public void doScheduledEvent(double scheduledTime, Object param) {
        // we no longer have pending. Clear that first
        synchronized (this) {
            // first see if we have been cancelled or not
            if (isRunning()) {
                setPendingSchedule(null);
                numberTicks++;
                // let our listeners know
                synchronized (clockTickListenersLock) {
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

}

