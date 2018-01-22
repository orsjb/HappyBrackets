package net.happybrackets.device.sensors;

/**
 * An exception indicating that a sensor could not be found
 */
public class SensorNotFoundException extends Exception {

    private SensorNotFoundException(){}


    /**
     * Create a custom exception to indicate a sensor could not be found
     * @param message the message to Display
     */
    public SensorNotFoundException(String message){
        super(message);
    }


}
