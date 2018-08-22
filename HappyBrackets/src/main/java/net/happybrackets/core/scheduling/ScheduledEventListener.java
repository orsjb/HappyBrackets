package net.happybrackets.core.scheduling;

/**
 * Used for receiving scheduled events. The param is the parameter that needs to be returned to listener
 */
public interface ScheduledEventListener {
    /**
     * Notification interface that a scheduled event is occuring
     * @param scheduledTime the time that was scheduled for the callback to occur
     * @param param the parameter to pass to the listener.
     */
    void doScheduledEvent (double scheduledTime, Object param);
}
