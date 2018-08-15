package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

import java.util.concurrent.Callable;

public class RaspbianGPIOOutput extends GPIODigitalOutput{

    private GpioPinDigitalOutput outputPin;

    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     */
    private RaspbianGPIOOutput(int gpio_number) {
        super(gpio_number);

        // Get the actual Pin
        Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));
        outputPin =  RaspbianGPIO.getGpioController().provisionDigitalOutputPin(pin);
    }

    @Override
    public void setState(boolean state) {
        outputPin.setState(state);
    }

    /**
     *
     * @param gpio_number gpio_number the pin number we want to create
     * @return a new RaspbianGPIOOutput if able to create, otherwise null
     */
    public static RaspbianGPIOOutput createGPIO(int gpio_number) {
        RaspbianGPIOOutput ret = null;
        try
        {
            GpioController controller = RaspbianGPIO.getGpioController();
            if (controller != null){
                // First see if the pin actually exists
                Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));

                if (pin != null) {
                    ret = new RaspbianGPIOOutput(gpio_number);
                }
            }
        }catch (UnsatisfiedLinkError e){} // this would be because we are not on hardware device

        return ret;
    }
}
