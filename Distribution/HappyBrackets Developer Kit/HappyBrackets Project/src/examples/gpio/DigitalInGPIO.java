package examples.gpio;

import com.pi4j.io.gpio.PinPullResistance;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIO;
import net.happybrackets.device.sensors.gpio.GPIOInput;

import java.lang.invoke.MethodHandles;

/**
 * This composition will read a digital input on GPIO_4 (pin 16 on PI header)
 * See http://pi4j.com/pins/model-zero-rev1.html for pinouts
 *
 * The Pin has been configures so the pin floats high
 * Set pin to earth to read a low state. Release pin and it will go back to true
 *
 * The state will be displayed in HB Status
 *
 *                       ╱
 *                      ╱
 *    _________________╱      _______________ GPIO_4 (pin 16)
 * __|__
 *  ___
 *   _
 *
 *******************************************************/
public class DigitalInGPIO implements HBAction, HBReset {

    // Define what outr GPIO Input pin is
    final int GPIO_NUMBER = 4;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        //hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // Reset all our GPIO - Only really necessary if the Pin has been assigned as something other than an input before
        GPIO.resetAllGPIO();


        /* Type gpioDigitalIn to create this code*/
        GPIOInput inputPin = GPIOInput.getInputPin(GPIO_NUMBER, PinPullResistance.PULL_UP);
        if (inputPin != null) {

            inputPin.addStateListener((sensor, new_state) -> {/* Write your code below this line */
                hb.setStatus("GPIO State: " + new_state);
                /* Write your code above this line */
            });
        } else {
            hb.setStatus("Fail GPIO Input " + GPIO_NUMBER);
        }/* End gpioDigitalIn code */

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        /***** Type your HBReset code below this line ******/
        // now disable our GPIO pin
        GPIO.clearPinAssignment(GPIO_NUMBER);
        /***** Type your HBReset code above this line ******/
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
