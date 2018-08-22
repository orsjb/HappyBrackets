package net.happybrackets.core.scheduling;

import org.jetbrains.annotations.NotNull;

/**
 * Comparator
 */
public class ScheduledObject implements Comparable{
    private double scheduledTime;
    private Object scheduledObject;
    private ScheduledEventListener scheduledEventListener;

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
     * A listener that cn be called from this scheduled object
     * @return
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
    public int compareTo(@NotNull Object o) {
        double other_time = ((ScheduledObject)o).scheduledTime;
        double val = scheduledTime - other_time;

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
