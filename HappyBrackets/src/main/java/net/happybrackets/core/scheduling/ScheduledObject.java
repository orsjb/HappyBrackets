package net.happybrackets.core.scheduling;


/**
 * Comparator
 */
public class ScheduledObject implements Comparable{
    private double scheduledTime;
    private Object scheduledObject;
    private ScheduledEventListener scheduledEventListener;

    /**
     * if we have cancelled this event and it should be ignored
     * @return whether we have cancelled set
     */
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    /**
     * Whether we want to set cancellation of this object.
     * @param cancelled whther we are cancelling this scheduled event
     */
    public synchronized void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    boolean cancelled = false;


    /**
     * The scheduled time for this object
     * @return scheduled time
     */
    public double getScheduledTime() {
        return scheduledTime;
    }

    /**
     * The object that is scheduled
     * @return the object stored
     */
    public Object getScheduledObject() {
        return scheduledObject;
    }


    /**
     * A listener that can be called from this scheduled object
     * @return the ScheduledObject created. We return this so they can cancel it later
     */
    public ScheduledEventListener getScheduledEventListener() {
        return scheduledEventListener;
    }

    /**
     * Constructor for a comparable scheduled object
     * @param time the time it needs to be scheduled - this is what is compared
     * @param object the object that needs to be scheduled
     * @param listener the listener attached to this event
     */
    public ScheduledObject(double time, Object object, ScheduledEventListener listener){
        scheduledTime = time;
        scheduledObject = object;
        scheduledEventListener = listener;
    }

    @Override
    public int compareTo( Object o) {
        // we return 1, -1 or zero.
        // Doing subtraction using a double will give wrong values when we convert to an int
        // so we will just do less / greater comparison
        double other_time = ((ScheduledObject)o).scheduledTime;

        if (scheduledTime < other_time){
            return -1;
        }
        else if (other_time < scheduledTime)
        {
            return 1;
        }

        return 0;
    }
}
