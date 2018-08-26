package net.happybrackets.core.scheduling;

/**
 * Delay Class that uses HBScheduler
 */
public class Delay implements ScheduledEventListener {

    /**
     * The minimum interval we will allow in milliseconds
     */
    final double MIN_INTERVAL = 2;

    /// have a copy of our pending scheduled object in case we want to cancel it
    ScheduledObject pendingSchedule = null;

    // store a local copy for efficiency
    // we wont make static because we don't want to make one unless necessary
    private HBScheduler delayScheduler;


    double delayTime;

    Object delayParam; // the object we will call back

    // we are cancelled
    private volatile boolean doCancel = false;

    @Override
    public void doScheduledEvent(double scheduledTime, Object param) {
        // we no longer have pending. Clear that first
        synchronized (this) {
            // first see if we have been cancelled or not
            if (isRunning()) {

                pendingSchedule = null;
                if (!doCancel) {
                    // calculate how late or early we are
                    double offset = delayScheduler.getSchedulerTime() - scheduledTime;

                    delayListener.delayComplete(offset, delayParam);

                }


            }
        }
    }

    /**
     * DelayReceived interface for receiving delay complee
     */
    public interface DelayListener {
        /**
         * Event occurs when delay is complete
         * @param offset the number of milliseconds we are off the tick. Positive number is late, negative is early
         * @param param the parameter you want to pass back when delay completed
         */
        void delayComplete(double offset, Object param);
    }


    private double startTime;

    private DelayListener delayListener;

    /**
     * Constructor using default Scheduler
     * @param interval the interval in milliseconds Must be greater than 2 milliseconds otherwise could lock up
     * @param param  the parameter we want t pass back when our delay has completed
     */
    public Delay(double interval, Object param, DelayListener listener){

        this(interval, param, listener, HBScheduler.getGlobalScheduler());
    }

    /**
     * Constructor
     * @param interval the interval in milliseconds Must be greater than 2 milliseconds otherwise could lock up
     * @param param  the parameter we want t pass back when our delay has completed
     * @param scheduler the scheduler to use
     */
    public Delay(double interval, Object param, DelayListener listener, HBScheduler scheduler){

        delayListener = listener;
        delayScheduler = scheduler;
        delayParam = param;
        startTime = delayScheduler.getSchedulerTime();
        delayTime = interval;
        double next_time =  startTime + delayTime;

        doCancel = false;
        pendingSchedule = delayScheduler.addScheduledObject(next_time, this, this);
    }

    /**
     * See if delay object is pending
     * @return true if a pending item is scheduled
     */
    public synchronized  boolean isRunning(){
        return pendingSchedule != null;
    }



    public synchronized void stop(){
        if (pendingSchedule != null){
            pendingSchedule.setCancelled(true);
            pendingSchedule = null;
        }
        doCancel = true;
    }




}

