package net.happybrackets.device.sensors.gpio;

import net.happybrackets.device.sensors.Sensor;

public abstract class GPIOPWMOutput extends GPIO {

    public static int getPwmRange() {
        return pwmRange;
    }

    public static void setPwmRange(int pwmRange) {
        GPIOPWMOutput.pwmRange = pwmRange;

        // We will also set the value to Raspbian
        RaspbianGPIOPWMOutput.setRaspiPwmRange(pwmRange);
    }

    static int pwmRange = 1024; // this is the default PWM range


    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     */
    protected GPIOPWMOutput(int gpio_number) {
        super(gpio_number, PIN_TYPE.PWMOutput);
    }

    /**
     * Sets the Output State
     * @param value the PWM value to set output to
     */
    abstract public void setValue(int value);

    /**
     * Get a GPIO input pin.
     * @param gpio_number the Pin Number
     * @return If an input pin already exists for that pin number or we were able to create it, will return the GPIOInput. If not, will return null
     */
    public synchronized static GPIOPWMOutput getOutputPin(int gpio_number){
        GPIOPWMOutput ret = null;
        GPIO existing = getGPIO(gpio_number);

        // see if we already have this pin assigned
        if (existing != null){
            // check if it is a digital input
            if (existing.pinType == PIN_TYPE.PWMOutput)
            {
                ret = (GPIOPWMOutput) existing;
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
    private static GPIOPWMOutput createOutputPin(int gpio_number) {
        GPIOPWMOutput ret = null;

        ret =  RaspbianGPIOPWMOutput.createGPIO(gpio_number);
        if (ret == null){
            if (Sensor.isSimulatedOnly()){
                ret = new GPIOPWMSimulator(gpio_number);
            }
        }

        return ret;
    }
}
