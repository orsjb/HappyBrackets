package net.happybrackets.device.sensors.gpio;

import com.pi4j.io.gpio.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaspbianGPIO {
    private static GpioController gpioController = null;

    private final static String GPIO_NAME_PREFIX = "GPIO ";


    /**
     * Get the Name of the GPIO pin in Pi4J
     * @param gpio_number the pin Number
     * @return what the RaspiPin name is for it
     */
    public static String getRaspPinName(int gpio_number){
        return GPIO_NAME_PREFIX + gpio_number;
    }

    static List<GpioPin> provisionedPins = Collections.synchronizedList(new ArrayList<>());
    /**
     * Get the GPIO controller. If it has not been created, then instantiate it
     * @return the Pi4J GpioController
     */
    synchronized static GpioController getGpioController(){
        if (gpioController == null){
            gpioController = GpioFactory.getInstance();
        }
        return gpioController;
    }

    /**
     * Store provisioned pin in our list
     * @param pin the Pin to store
     */
    static  void  addProvisionedPin(GpioPin pin){
        synchronized (provisionedPins){
            provisionedPins.add(pin);
        }
    }

    /**
     * Remove provisioned pin from our list. Is NOT unprovisioned in GPIO. Calling class needs to do that
     * @param pin pin to removed
     */
    void removeProvisionedPin(GpioPin pin){
        synchronized (provisionedPins){
            provisionedPins.remove(pin);
        }
    }

    /**
     * Unprovision all pins and clear the list of provisioned pins
     */
    static void unprovisionAllPins(){
        synchronized (provisionedPins){
            for (GpioPin pin :provisionedPins){
                System.out.println("Unprovision " + pin.getName());
                gpioController.unprovisionPin(pin);
            }
            provisionedPins.clear();
        }
    }

    /**
     * Enables provisioning of GPIO pin numbers using Broadcom numbering scheme
     * Note that actual physical pin numbers can change over board revisions when using Broadcom scheme so it is not recommended
     * The following URLs give more detail
     * <a href="http://pi4j.com/pin-numbering-scheme.html">http://pi4j.com/pin-numbering-scheme.html</a> and
     * <a href="http://wiringpi.com/pins/">http://wiringpi.com/pins/</a> for further explanation
     * @param use_broadcom set to true if you want to change to broadcom numbering scheme
     *
     */
    synchronized public void setBroadcomPinNumbering (boolean use_broadcom){
        if (use_broadcom)
        {
            GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING));
        }
        else
        {
            GpioFactory.setDefaultProvider(new RaspiGpioProvider(RaspiPinNumberingScheme.DEFAULT_PIN_NUMBERING));
        }
    }
}
