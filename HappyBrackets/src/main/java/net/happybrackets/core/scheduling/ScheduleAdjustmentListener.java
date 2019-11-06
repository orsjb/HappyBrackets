package net.happybrackets.core.scheduling;

/**
 * Notify listeners that schedule has been changed
 */
public interface ScheduleAdjustmentListener {
    void scheduleComplete(HBScheduler scheduler);
}
