package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.*;

import java.util.EnumSet;

public class RaspbianGPIOPWMOutput extends GPIOPWMOutput{

    private GpioPinPwmOutput outputPin;

    /**
     * Constructor
     *
     * @param gpio_number the GPIO pin number

     */
    private RaspbianGPIOPWMOutput(int gpio_number) {
        super(gpio_number);

        // Get the actual Pin
        Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));
        outputPin =  RaspbianGPIO.getGpioController().provisionPwmOutputPin(pin);
    }

    /**
     * Set the PWM range inside wiring PI
     * @param pwm_range the pwn rnage we are setting to
     */
    static void setRaspiPwmRange(int pwm_range) {

        try{
            com.pi4j.wiringpi.Gpio.pwmSetRange(pwm_range);
        }catch (UnsatisfiedLinkError e){}
    }

    @Override
    public void setValue(int value) {
        outputPin.setPwm(value);
    }

    /**
     *
     * @param gpio_number gpio_number the pin number we want to create
     * @return a new RaspbianGPIOOutput if able to create, otherwise null
     */
    public static RaspbianGPIOPWMOutput createGPIO(int gpio_number) {
        RaspbianGPIOPWMOutput ret = null;
        try
        {
            GpioController controller = RaspbianGPIO.getGpioController();
            if (controller != null){
                // First see if the pin actually exists
                Pin pin  = RaspiPin.getPinByName(RaspbianGPIO.getRaspPinName(gpio_number));

                if (pin != null) {
                    EnumSet<PinMode> supported_modes = pin.getSupportedPinModes();
                    if (supported_modes.contains(PinMode.PWM_OUTPUT)) {
                        ret = new RaspbianGPIOPWMOutput(gpio_number);
                    }
                    else {
                        System.out.println(RaspbianGPIO.getRaspPinName(gpio_number) + " does not support PWM");
                    }
                }
            }
        }catch (UnsatisfiedLinkError e){} // this would be because we are not on hardware device

        return ret;
    }

    @Override
    void reset() {

    }

    @Override
    void unnasign() {
        GpioController controller = RaspbianGPIO.getGpioController();
        controller.unprovisionPin(outputPin);
    }
}
