package net.happybrackets.device.sensors.gpio;

import net.happybrackets.device.sensors.Sensor;

public abstract class GPIODigitalOutput extends GPIO {
    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     */
    protected GPIODigitalOutput(int gpio_number) {
        super(gpio_number, PIN_TYPE.DigitalOutput);
    }

    /**
     * Sets the Output State
     * @param state the state to set output to
     */
    abstract public void setState(boolean state);

    /**
     * Gets the last state set
     * @return the last state set
     */
    abstract public boolean getState();
    /**
     * Get a GPIO input pin.
     * @param gpio_number the Pin Number
     * @return If an input pin already exists for that pin number or we were able to create it, will return the GPIOInput. If not, will retrun null
     */
    public synchronized static GPIODigitalOutput getOutputPin(int gpio_number){
        GPIODigitalOutput ret = null;
        GPIO existing = getGPIO(gpio_number);

        // see if we already have this pin assigned
        if (existing != null){
            // check if it is a digital input
            if (existing.pinType == PIN_TYPE.DigitalOutput)
            {
                ret = (GPIODigitalOutput) existing;
            }
        }
        else { // we need to create one
            ret = createOutputPin(gpio_number);

            if (ret != null){
                addGpio(ret);
            }
        }

        return ret;
    }

    /**
     * Create a digital input based on our system and pin number
     * @param gpio_number the pin number we want to create
     * @return a new GPIOInput based on the Pin number and underlying system  Will try Hardware version before attempting simulator
     */
    private static GPIODigitalOutput createOutputPin(int gpio_number) {
        GPIODigitalOutput ret = null;

        ret =  RaspbianGPIOOutput.createGPIO(gpio_number);
        if (ret == null){
            if (Sensor.isSimulatedOnly()){
                ret = new GPIOOutputSimulator(gpio_number);
            }
        }

        return ret;
    }
}
