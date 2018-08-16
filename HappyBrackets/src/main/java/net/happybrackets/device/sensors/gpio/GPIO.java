package net.happybrackets.device.sensors.gpio;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * Class combines GPIO input and GPIO output because a pin cannot be both input and output
 * By storing in a common class, we are able to notify user that assignment has failed
 * Becomes a class storage point
 */
abstract public class GPIO {

    enum PIN_TYPE {
        DigitalInput,
        DigitalOutput,
        PWMOutput
    }

    // Map of created GPIO
    static private Map <Integer, GPIO> assignedPins = Collections.synchronizedMap(new Hashtable<Integer, GPIO>());

    protected final int gpioNumber;
    protected final PIN_TYPE  pinType;

    /**
     * Get the Pin number of this GPIO
     * @return The GPIO number
     */
    public int getGpioNumber() {
        return gpioNumber;
    }

    /**
     * Constructor
     * @param gpio_number the GPIO pin number
     * @param pin_type the type of pin
     */
    protected GPIO (int gpio_number, PIN_TYPE pin_type){
        gpioNumber = gpio_number;
        pinType = pin_type;
    }




    /**
     * Get GPIO assigned to this pin number
     * @param gpio_number
     * @return a GPIO object if it has been created
     */
    synchronized static GPIO getGPIO(int gpio_number){
        GPIO ret = null;

        synchronized (assignedPins) {
            if (assignedPins.containsKey(gpio_number)) {
                ret = assignedPins.get(gpio_number);
            }
        }
        return ret;
    }

    /**
     * Add a GPIO object to assigned pins list
     * @param gpio the GPIO object we are adding
     */
    synchronized static void addGpio(GPIO gpio){
        int gpio_number = gpio.gpioNumber;
        synchronized (assignedPins) {
            assignedPins.put(gpio_number, gpio);
        }
    }

    /**
     * Clear any Listeners of perform any function thats required when a reset occurs
     */
    static public void resetGpioListeners()
    {
        synchronized (assignedPins){
            for (GPIO gpio : assignedPins.values()){
                gpio.reset();
            }
        }
    }

    /**
     * Clears provisioning of all GPIO
     */
    static public void resetAllGPIO(){
        resetGpioListeners();

        synchronized (assignedPins) {
            RaspbianGPIO.unprovisionAllPins();
            assignedPins.clear();
        }
    }

    /**
     * Clears the pin assignment of a pin so it cn be assigned to another function
     * @param gpio_number the GPIO number we want to unassigned
     */
    static public void clearPinAssignment(int gpio_number){
        synchronized (assignedPins) {
            if (assignedPins.containsKey(gpio_number)) {
                // we have found it.
                GPIO gpio = assignedPins.get(gpio_number);
                // first reset the object
                gpio.reset();

                // now unnasign
                gpio.unnasign();
                assignedPins.remove(gpio_number);
            }
        }
    }
    /**
     * Occurs when we want to do a reset
     */
    abstract void reset();

    /**
     * Remove the assignment of the pin
     */
    abstract void unnasign();
}
