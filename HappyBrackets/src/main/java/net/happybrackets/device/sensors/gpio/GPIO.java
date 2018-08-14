package net.happybrackets.device.sensors.gpio;

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
    static private Map <Integer, GPIO> assignedPins = new Hashtable<Integer, GPIO>();

    protected final int pinNumber;
    protected final PIN_TYPE  pinType;

    /**
     * Constructor
     * @param gpio_number the GPIO pin number
     * @param pin_type the type of pin
     */
    protected GPIO (int gpio_number, PIN_TYPE pin_type){
        pinNumber = gpio_number;
        pinType = pin_type;
    }




    /**
     * Get GPIO assigned to this pin number
     * @param gpio_number
     * @return a GPIO object if it has been created
     */
    synchronized static GPIO getGPIO(int gpio_number){
        GPIO ret = null;
        if (assignedPins.containsKey(gpio_number)){
            ret = assignedPins.get(gpio_number);
        }
        return ret;
    }

    /**
     * Add a GPIO object to assigned pins list
     * @param gpio the GPIO object we are adding
     */
    synchronized static void addGpio(GPIO gpio){
        int gpio_number = gpio.pinNumber;
        assignedPins.put(gpio_number, gpio);
    }
}
