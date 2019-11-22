package net.happybrackets.core.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clock Class that uses the {@link HBScheduler} class. If the {@link HBScheduler} has it's time adjusted, all {@link Clock} objects will follow the time of the {@link HBScheduler} .
 *
 * <br>A clock event is detected by adding a {@link ClockTickListener} to it through the {@link #addClockTickListener(ClockTickListener)}. More than one listener can be added to a clock
 * <br>Clocks are created stopped and must be started outside the clock definition. For example:
 *
 * <pre>
 *
 *     Clock testclock1 = new Clock(1000);
 *
 *     testclock1.addClockTickListener((offset, clock) -> {
 *          long number_ticks = clock.getNumberTicks();
 *          System.out.println("Num Ticks: " + number_ticks);
 *       });
 *
 *     <b>testclock1.start();</b>
 *
 * </pre>
 * <br> The clock is stopped through {@link #stop()} or when the sketch is reset
 * <br> Utility functions {@link #BPM2Interval(double)} and {@link #Interval2BPM(double)} facilitate converting intervals to and from Beats per minute
 *
 * <br> It is possible to shift a clock forwards or backwards using {@link #shiftTime(double)}, change the interval through {@link #setInterval(double)},  synchronise to another {@link Clock} using {@link #synchronizeClock(Clock)}, or cause the clock to restart with a zero count using {@link #reset()}
 * <br> You can obtain status of clock through
 * function {@link #getNumberTicks()}, {@link #getClockInterval()} and {@link #isRunning()}
 */
public class Clock implements ScheduledEventListener {

    /**
     * ClockTick interface for receiving clock Tick events
     * <br> The {@link ClockTickListener#clockTick(double, Clock)} receives the scheduled event and is usually represented as a Lambda. For example:
     <pre>
     testclock1.addClockTickListener(new Clock.ClockTickListener() {
     {@literal @}Override
            public void clockTick(double offset, Clock clock) {

            }
        });

     </pre>

     is generally represented as:
     <pre>
     testclock1.addClockTickListener((offset, clock) -> {

     });
     </pre>

     * <br>It is also possible to connect the one {@link ClockTickListener} to more than one clock. For example:
     *
     * <pre>
     * Clock testclock1 = new Clock(100);
     * Clock testclock2 = new Clock(37);
     *
     * Clock.ClockTickListener clockTickListener = (offset, clock) -> {
     *     double clock_interval = clock.getClockInterval();
     *     long num_ticks =  clock.getNumberTicks();
     *
     *     System.out.println("Clock interval " + clock_interval + " has had " + num_ticks + " ticks");
     * };
     *
     * // Now add listener to both clocks
     * testclock1.addClockTickListener(clockTickListener);
     * testclock2.addClockTickListener(clockTickListener);
     *
     * testclock1.start();
     * testclock2.start();
     * </pre>
     */
    public interface ClockTickListener {
        /**
         * Event occurs when clock tick occurs. The offset parameter indicates how far of the exact time in milliseconds the event occurred,
         * while the clock is the clock that triggered the event.
         * <pre>
         * testclock1.addClockTickListener((offset, clock) -> {
         *
         * });
         * </pre>
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
     * The interval of the clock in milliseconds
     * @return the clock interval
     */
    public double getClockInterval() {
        return clockInterval;
    }

    /**
     * Return the number of clock ticks since the clock was started
     * @return number of ticks
     */
    public long getNumberTicks() {
        return numberTicks;
    }

    private long numberTicks =  0;

    private final Object clockTickListenersLock = new Object();
    private List<ClockTickListener> clockTickListeners = new ArrayList<>();

    /**
     * Constructor using default Scheduler and an interval
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
            // see if we need to stop
            double time_remaining = clockScheduler.getSchedulerTime() - pendingSchedule.getScheduledTime();
            if (time_remaining > interval) {
                stop();
                start();
            }
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
     * Add a {@link ClockTickListener} for this clock. More than one listener can be attached to a {@link Clock} and they
     * can be added after the clock is already started. For example:
     *
     * <pre>
     * final int TICKS_PER_BEAT = 4;
     * double interval = Clock.BPM2Interval(120 * TICKS_PER_BEAT);
     *
     * Clock testclock1 = new Clock(interval);
     *
     * // create a listener for each Beat
     * testclock1.addClockTickListener((offset, clock) -> {
     *   if (clock.getNumberTicks() % TICKS_PER_BEAT == 0){
     *     System.out.println("Beat " + clock.getNumberTicks() / TICKS_PER_BEAT);
     *   }
     *
     * });
     *
     * testclock1.start();
     *
     * // We can even add listeners after clock is started
     * // Create a Listener for every Tick not on the beat
     * testclock1.addClockTickListener((offset, clock) -> {
     *    long tickNum = clock.getNumberTicks() % TICKS_PER_BEAT;
     *    System.out.println("tick " + tickNum);
     * });
     *
     * </pre>
     *
     * Listeners are removed using {@link #removeClockTickListener(ClockTickListener)}
     * @param listener the listener to add
     * @return this
     */
    public Clock addClockTickListener(ClockTickListener listener){
        synchronized (clockTickListenersLock){
            clockTickListeners.add(listener);
        }

        return this;
    }

    /**
     * Removes a {@link ClockTickListener} for this clock previously added using {@link #addClockTickListener(ClockTickListener)}
     * @param listener the listener to remove
     */
    public void removeClockTickListener(ClockTickListener listener){
        synchronized (clockTickListenersLock){
            clockTickListeners.remove(listener);
        }
    }


    /**
     * Clears all {@link ClockTickListener} for this clock
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
                    double event_time = clockScheduler.getSchedulerTime();
                    double offset = event_time - scheduledTime;

                    for (ClockTickListener listener : clockTickListeners) {
                        listener.clockTick(offset, this);
                    }

                    if (!doCancel) {
                        // now lets set our next time - the scheduler itself will account for any extra time taken

                        // see if our offset is greater than our interval. If it is, we will skip to it
                        if (Math.abs(offset) > clockInterval){
                            scheduledTime = event_time;
                            System.out.println("Interval exceeded");
                        }

                        double next_time = scheduledTime + clockInterval + shiftTime;
                        shiftTime = 0;

                        pendingSchedule = clockScheduler.addScheduledObject(next_time, this, this);
                    }
                }
            }
        }
    }

}

