package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;

import java.util.concurrent.Callable;

public class RaspbianGPIOInput extends GPIOInput

{
    private GpioPinDigitalInput inputPin;

    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number
     * @param pinPullResistance The pullup resistance we require.
     */
    private RaspbianGPIOInput(int gpio_number, PinPullResistance pinPullResistance) {
        super(gpio_number);

        // Get the actual Pin
        Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));
        inputPin =  RaspbianGPIO.getGpioController().provisionDigitalInputPin(pin, pinPullResistance);
        inputPin.addTrigger(new GpioCallbackTrigger(new Callable<Void>() {
            public Void call() throws Exception {

                sendStateChange();
                return null;
            }
        }));
    }


    /**
     * Call method to change state
     */
    synchronized private void sendStateChange(){
        stateChanged(this, getState());
    }

    /**
     * Factory method for creating a new  RaspbianGPIOInput. Rather than throwing
     * an exception because unable to connect to Pi4J, we see if we can get GPIO and return null if we can't
     * @param gpio_number the Pin number
     * @param pinPullResistance The pullup resistance we require.
     * @return a new RaspbianGPIOInput if able to create, otherwise null
     */
    static RaspbianGPIOInput createGPIO(int gpio_number, PinPullResistance pinPullResistance){

        RaspbianGPIOInput ret = null;
        try
        {
            GpioController controller = RaspbianGPIO.getGpioController();
            if (controller != null){
                // First see if the pin actually exists
                Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));

                if (pin != null) {
                    ret = new RaspbianGPIOInput(gpio_number, pinPullResistance);
                }
            }
        }catch (UnsatisfiedLinkError e){} // this would be because we are not on hardware device

        return ret;
    }

    @Override
    public boolean getState() {
        return inputPin.isHigh();
    }

}
