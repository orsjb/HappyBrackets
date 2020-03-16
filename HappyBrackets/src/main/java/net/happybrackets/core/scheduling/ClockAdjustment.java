package net.happybrackets.core.scheduling;

import com.google.gson.Gson;
import net.happybrackets.core.control.CustomGlobalEncoder;
import net.happybrackets.core.control.DynamicControl;

/**
 * Class for encapsulating amounts to adjust  {@link HBScheduler}
 */
public class ClockAdjustment implements CustomGlobalEncoder {
    static Gson gson = new Gson();
    final double PRECISION = 0.000001d;

    private double adjustmentAmount = 0; // the amount we are going to adjust scheduler in ms
    private  long adjustmentDuration = 0; // how long we will take to make the adjustment in milliseconds

    /**
     * Return the amount that nwe need to adjust clock by
     * Used as a parameter to {@link HBScheduler#adjustScheduleTime(double, long)}
     * @return the amount of time in milliseconds
     */
    public double getAdjustmentAmount() {
        return adjustmentAmount;
    }


    /**
     * The amount of time we will take to make the adjustment
     * Used as a parameter to {@link HBScheduler#adjustScheduleTime(double, long)}
     * @return the amount of time to make adjustment
     */
    public long getAdjustmentDuration() {
        return adjustmentDuration;
    }


    /**
     * Object for sending the
     * @param amount the amount of time we will be shifting in milliseconds
     * @param time how long scheduler will take to do it in milliseconds
     */
    public ClockAdjustment(double amount, long time){
        adjustmentAmount = amount;
        adjustmentDuration = time;
    }


    /**
     * Default constructor with zero values
     */
    public ClockAdjustment(){};

    /**
     * Constructor using defined Object array as input.
     * @param args the arguments in object array.
     *             The args[0, 1, 2],  are ints that make up the adjustment amount as a double
     *             The args [3,4 ] are ints that make up a long as the time in Milliseconds to take to make the adjustment
     */
    private ClockAdjustment(Object... args){
        adjustmentAmount = DynamicControl.integersToScheduleTime((int)args[0], (int)args[1], (int)args[2]);
        adjustmentDuration = DynamicControl.integersToLong((int)args[3], (int)args[4]);
    }

    @Override
    public Object[] encodeGlobalMessage() {
        int [] amount_vals = DynamicControl.scheduleTimeToIntegers(adjustmentAmount);
        int [] duration_vals = DynamicControl.longToIntegers(adjustmentDuration);

        Object [] ret = new Object[amount_vals.length + duration_vals.length];

        for (int i =  0; i < amount_vals.length; i++){
            ret [i] = amount_vals[i];
        }

        for (int i = 0; i < duration_vals.length; i++){
            ret[i + amount_vals.length] = duration_vals[i];
        }
        return ret;
    }

    @Override
    public ClockAdjustment restore(Object restore_data) {
        ClockAdjustment ret =  null;

        // First see if this is just the class
        if (restore_data instanceof ClockAdjustment){
            ret = (ClockAdjustment)restore_data;
        }
        // let us see if it is JsonData
        else if (restore_data instanceof String){
            ret = new Gson().fromJson((String) restore_data, ClockAdjustment.class);
        }
        else if (restore_data instanceof Object[]){
            ret =  new ClockAdjustment((Object[]) restore_data);
        }

        return ret;
    }


    @Override
    public boolean equals(Object other){
        if (other == null){
            return false;
        }else if (! (other instanceof ClockAdjustment))
        {
            return false;
        }
        else {
            ClockAdjustment right  = (ClockAdjustment)other;

            double diff = Math.abs(right.adjustmentAmount - adjustmentAmount);

            return diff <= PRECISION  && right.adjustmentDuration == adjustmentDuration;
        }
    }

}
