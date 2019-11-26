package net.happybrackets.core.scheduling;

/**
 * Delay Class that uses HBScheduler
 * You can use a delay to schedule an event to occur at some time in the future. For example,
 * if we wanted to turn GPIO on for 5 seconds, we might use the following code:
 <pre>
 final int GPIO_OUTPUT = 23;
 GPIODigitalOutput outputPin = GPIODigitalOutput.getOutputPin(GPIO_OUTPUT);

 if (outputPin != null) {
     outputPin.setState(true);

     // now we will turn it off after 5 seconds
     new Delay(5000, outputPin, (delay_offset, param) -{@literal >} {
         // delay_offset is how far out we were from our exact delay time in ms and is a double
         // param is the parameter we passed in, which was the output pin
         ((GPIODigitalOutput) o).setState(false);
     });
 }
 </pre>
 More detail is in {@link DelayListener#delayComplete(double, Object)}
 */
public class Delay implements ScheduledEventListener {

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
     * DelayReceived interface for receiving delay complete. See {@link DelayListener#delayComplete(double, Object)}.
     */
    public interface DelayListener {
        /**
         * Event occurs when delay is complete. Consider the following code which will
         * create an event 100 milliseconds in the future passing the parameter object
         <pre>
        Object parameter = new Object();
        new Delay(100, parameter, new Delay.DelayListener() {
         {@literal @}Override
            public void delayComplete(double offset, Object param) {

            }
        });
     </pre>
     When the delay has completed, this function will be called with the parameter passed in
     at the constructor ands the amount of time off the actual time in milliseconds. If for example,
     the function was called 2.3 milliseconds late, the offset value would be 2.3
     <br>The function is generally coded as a lambda, where the above code would be presented as:
     <pre>
     Object parameter = new Object();
     new Delay(100, parameter, (offset, param) -{@literal >} {
         // delay_offset is how far out we were from our exact delay time in ms and is a double
         // param is the parameter we passed in, which was the output pin

     });
     </pre>
         * @param offset the number of milliseconds we are off the tick. Positive number is late, negative is early
         * @param param the parameter you want to pass back when delay completed
         */
        void delayComplete(double offset, Object param);
    }


    private double startTime;

    private DelayListener delayListener;

    /**
     * Constructor using default Scheduler.
     * if we wanted to turn GPIO on for 5 seconds, we might use the following code:
     <pre>
    final int GPIO_OUTPUT = 23;
    GPIODigitalOutput outputPin = GPIODigitalOutput.getOutputPin(GPIO_OUTPUT);

    if (outputPin != null) {
        outputPin.setState(true);

        // now we will turn it off after 5 seconds
        new Delay(5000, outputPin, (delay_offset, param) -{@literal >} {
            // delay_offset is how far out we were from our exact delay time in ms and is a double
            // param is the parameter we passed in, which was the output pin
            ((GPIODigitalOutput) o).setState(false);
        });
    }
     </pre>

     * @param interval the interval in milliseconds.
     * @param param  the parameter we want t pass back when our delay has completed
     * @param listener the  {@link DelayListener} to receive the callback when delay has finished
     */
    public Delay(double interval, Object param, DelayListener listener){

        this(interval, param, listener, HBScheduler.getGlobalScheduler());
    }

    /**
     * Constructor using an independent {@link HBScheduler}. Specifics about delay constructor are in
     * {@link #Delay(double, Object, DelayListener)}
     * @param interval the interval in milliseconds If less than zero, the delay will call back on next schedule update
     * @param param  the parameter we want t pass back when our delay has completed
     * @param listener the {@link DelayListener} to receive the callback when delay has finished
     * @param scheduler the {@link HBScheduler} to use
     */
    public Delay(double interval, Object param, DelayListener listener, HBScheduler scheduler){

        delayListener = listener;
        delayScheduler = scheduler;
        delayParam = param;
        startTime = delayScheduler.getSchedulerTime();
        delayTime = interval;
        double next_time =  startTime + delayTime;

        if (interval < 0){
            next_time = 0;
        }

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


    /**
     * Cancel the delay event and stop it from occurring. For example, if we had a delay schedule for
     * 50 seconds in the future and we wanted to cancel it before it completed, we could do the following:
     *
     <pre>
     // This delay will take 50 seconds
     Delay longdelay = new Delay(50000, null, (delay_offset, param) -{@literal >} {
         // delay_offset is how far out we were from our exact delay time in ms and is a double
         // param is the parameter we passed in type your code below this line
         System.out.println("Delay Complete");
         // type your code above this line
     });

     // 30 seconds in the future - we will stop longdelay completing
     longdelay.stop();

     </pre>
     */
    public synchronized void stop(){
        if (pendingSchedule != null){
            pendingSchedule.setCancelled(true);
            pendingSchedule = null;
        }
        doCancel = true;
    }




}

