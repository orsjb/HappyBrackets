package examples.pihat;

import com.pi4j.io.gpio.PinPullResistance;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIO;
import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;
import net.happybrackets.device.sensors.gpio.GPIOInput;

import java.lang.invoke.MethodHandles;

/**
 *
 * These are for UNSW PI hats
 *
 * In order to use them, you need to set GPIO 28 to high - otherwise they are a tri-state
 *
 *
 * GPIO are 25, 23, 22 and 21
 *
 *
 * The state will be displayed in HB Status
 *
 *                                ____╱╲  ╱╲  ___ +5V (Red Grove pin - pin 3)
 *                       ╱       |      ╲╱  ╲╱
 *                      ╱        |
 *    _________________╱      ___|____________  GPIO (Pin 1 on Grove)
 * __|__ Black grove pin
 *  ___  (Pin 4 on Grove)
 *   _
 *
 *
 *******************************************************/
public class PiHatDigitalInGPIO implements HBAction, HBReset {

    // We need to SET GPIO 28 High to enable Input or Output for GPIO on PiHat board
    private static final int GPIO_ENABLE =  28;
    // Define what outr GPIO Input pin is
    final int GPIO_NUMBER = 25;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // Reset all our GPIO - Only really necessary if the Pin has been assigned as something other than an input before
        GPIO.resetAllGPIO();


        /* Type gpioDigitalIn to create this code*/
        GPIOInput inputPin = GPIOInput.getInputPin(GPIO_NUMBER, PinPullResistance.OFF);
        if (inputPin != null) {

            inputPin.addStateListener((sensor, new_state) -> {/* Write your code below this line */
                HB.HBInstance.setStatus("GPIO State: " + new_state);
                /* Write your code above this line */
            });
        } else {
            HB.HBInstance.setStatus("Fail GPIO Input " + GPIO_NUMBER);
        }/* End gpioDigitalIn code */


        // Enable our GPIO on the PiHat
        GPIODigitalOutput outputPin = GPIODigitalOutput.getOutputPin(GPIO_ENABLE);
        if (outputPin == null) {
            HB.HBInstance.setStatus("Fail GPIO Digital Out " + GPIO_ENABLE);
        }/* End gpioDigitalOut code */
        else {
            outputPin.setState(true);
        }

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
