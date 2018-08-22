package net.happybrackets.core.scheduling;

/**
 * Enables a very efficient method for calculation accumulative averages by storing accumulated sum
 * of all values and number of elements
 */
public class AverageCalculator {
    private double accumulator = 0;
    private int numMessages =  0;

    // cached value - just make it a little bit faster
    private double averageValue = 0;
    /**
     * Add a value after adding this message
     * @param next the next lag interval we are adding
     * @return the average of accumulated vlaues
     */
    double addValue(double next){
        accumulator += next;
        numMessages++;
        averageValue = accumulator / numMessages;
        return averageValue;
    }

    /**
     * Get the average value we have calculated so far
     * @return the accumulative average
     */
    double averageValue(){
        return averageValue;
    }
    /**
     * Reset our values to the new value
     * @param new_average our new average
     * @param buffer_length what our virtual buffer length will be
     */
    void reset(double new_average, int buffer_length){
        accumulator = new_average * buffer_length;
        numMessages = buffer_length;
    }
}
