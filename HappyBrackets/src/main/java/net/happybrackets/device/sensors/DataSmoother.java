package net.happybrackets.device.sensors;

/**
 * Creates an efficient data smoother that doe not require iteration through an array to get average
 *
 */
public class DataSmoother {
    double messages [];
    final int BUFF_SIZE;
    int buffIndex = 0;

    double lastValue = 0;
    double accumulator = 0;


    /**
     * See if our buffer has been fully primed
     * @return true if fully primed
     */
    public boolean dataPrimed(){
        return buffIndex >= BUFF_SIZE;
    }

    /**
     * See if our buffer is empty
     * @return true if empty
     */
    public boolean isEmpty(){
        return buffIndex == 0;
    }

    /**
     * Constructor
     * @param buffer_size the number of items in our buffer
     */
    public DataSmoother(int buffer_size){
        BUFF_SIZE = buffer_size;
        messages = new double[buffer_size];
    }

    /**
     * Reset the DataSmoother
     */
    public void reset(){
        buffIndex = 0;
        accumulator = 0;

    }

    /**
     * Get the Buffer size
     * @return the bufer size
     */
    public int getBuffSize(){
        return BUFF_SIZE;
    }
    /**
     * Get the average calculated value
     * @return the average data inside buffer
     */
    public double getAverage(){
        if (buffIndex > 0) {
            if (buffIndex >= BUFF_SIZE) {
                return accumulator / BUFF_SIZE;
            } else {
                return accumulator / (buffIndex);
            }
        } else {
            return 0;
        }
    }

    /**
     * Add a value to our accumulated value
     * @param new_val the new value to add
     * @return the current average value
     */
    public double addValue(double new_val){
        int array_index = buffIndex % BUFF_SIZE;

        if (buffIndex >= BUFF_SIZE){
            // we need to pop one off the front
            double dropped_val = messages[array_index];
            accumulator -= dropped_val;
        }
        messages[array_index] = new_val;
        accumulator += new_val;
        buffIndex++;
        return getAverage();
    }
}
