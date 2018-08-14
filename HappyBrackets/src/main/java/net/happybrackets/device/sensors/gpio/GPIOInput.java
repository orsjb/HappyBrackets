package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.PinPullResistance;
import net.happybrackets.device.sensors.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for detecting GPIO input changes
 */
public abstract class GPIOInput extends GPIO implements GPIOInputListener {

    List<GPIOInputListener> inputListeners = Collections.synchronizedList(new ArrayList<>());

    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     */
     GPIOInput(int gpio_number) {
        super(gpio_number, PIN_TYPE.DigitalInput);
    }


    abstract boolean getState();

    @Override
    public void stateChanged(GPIOInput sensor, boolean new_state) {
        synchronized (inputListeners)
        {
            for(GPIOInputListener listener: inputListeners){
                listener.stateChanged(sensor, new_state);
            }
        }
    }


    /**
     * Add a listener to be notified when a state change occurs
     * @param listener the listener to be notified
     */
    public void addStateListener(GPIOInputListener listener){
        synchronized (inputListeners){
            inputListeners.add(listener);
        }
    }

    /**
     * Remove a specific listener
     * @param listener the listener to remove
     */
    public void removeStateListener(GPIOInputListener listener){
        synchronized (inputListeners){
            inputListeners.remove(listener);
        }
    }

    /**
     * Remove all listeners
     */
    public void clearAllStateListeners(){
        synchronized (inputListeners){
            inputListeners.clear();
        }
    }

    /**
     * Get a GPIO input pin. There will be no Pullup resistance defined
     * @param gpio_number the GPIO number
     * @return the GPIO Pin if it is available, otherwise null
     */
    public synchronized static GPIOInput getInputPin(int gpio_number){
        return getInputPin(gpio_number, PinPullResistance.OFF);
    }

    /**
     * Get a GPIO input pin.
     * @param gpio_number the Pin Number
     * @param pinPullResistance the Pullup type for the input. Will only count if the pin has not already been created in another sketch
     * @return If an input pin already exists for that pin number or we were able to create it, will return the GPIOInput. If not, will retrun null
     */
    public synchronized static GPIOInput getInputPin(int gpio_number, PinPullResistance pinPullResistance){
        GPIOInput ret = null;
        GPIO existing = getGPIO(gpio_number);

        // see if we already have this pin assigned
        if (existing != null){
            // check if it is a digital input
            if (existing.pinType == PIN_TYPE.DigitalInput)
            {
                ret = (GPIOInput) existing;
            }
        }
        else { // we need to create one
            ret = createInputPin(gpio_number, pinPullResistance);

            if (ret != null){
                addGpio(ret);
            }
        }

        return ret;
    }

    /**
     * Create a digital input based on our system and pin number
     * @param gpio_number the pin number we want to create
     * @param pinPullResistance The pullup resistance we require. Only has affect if the pin is not already been created in another sketch
     * @return a new GPIOInput based on the Pin number and underlying system  Will try Hardware version before attempting simulator
     */
    private static GPIOInput createInputPin(int gpio_number, PinPullResistance pinPullResistance) {
        GPIOInput ret = null;

        ret =  RaspbianGPIOInput.createGPIO(gpio_number, pinPullResistance);
        if (ret == null){
            if (Sensor.isSimulatedOnly()){
                ret = new GPIOInputSimulator(gpio_number, pinPullResistance);
            }
        }

        return ret;
    }
}
