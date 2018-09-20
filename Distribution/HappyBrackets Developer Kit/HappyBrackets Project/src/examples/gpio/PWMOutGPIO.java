package examples.gpio;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIO;
import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;
import net.happybrackets.device.sensors.gpio.GPIOPWMOutput;

import java.lang.invoke.MethodHandles;

/**
 * This composition will SEND PWM output on GPIO_23 (pin 33 on PI header)
 * See http://pi4j.com/pins/model-zero-rev1.html for pinouts
 *
 * The LED will grow brighter and then softer
 * connect cathode of LED through a resistance to earth and then connect anode to GPIO 1 output
 *
 *
 *
 *                                 ↗ ↗
 *
 *                                ┃ ╱┃
 *    _________________╱╲  ╱╲  ___┃╱ ┃_______________ GPIO_23 (pin 33)
 * __|__                 ╲╱  ╲╱   ┃╲ ┃
 *  ___                           ┃ ╲┃
 *   _
 *
 *******************************************************/
public class PWMOutGPIO implements HBAction, HBReset {

    boolean exitThread = false;

    final int GPIO_OUTPUT = 23;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // Reset all our GPIO - Only really necessary if the Pin has been assigned as something other than an input before
        GPIO.resetAllGPIO();


        /* Type gpioPWMOut to create this code */
        GPIOPWMOutput outputPin = GPIOPWMOutput.getOutputPin(GPIO_OUTPUT);

        if (outputPin == null) {
            hb.setStatus("Fail GPIO PWM " + GPIO_OUTPUT);
        }/*** End gpioPWMOut code ***/


        if (outputPin != null)
        {
            /* Type threadFunction to generate this code */
            Thread thread = new Thread(() -> {
                int SLEEP_TIME = 1; // this is how long between consecutive values

                int pwm_val = 0;
                boolean count_down = false;

                // now loop back and forth
                while (!exitThread) {
                    /*** write your code below this line ***/

                    // set the output to pwm value
                    outputPin.setValue(pwm_val);

                    // see which direction we are going
                    if (count_down){
                        // we are getting less bright
                        if (pwm_val == 0){ // we are at bottom - start going up
                            count_down = false;
                        }else{
                            pwm_val--;
                        }
                    }
                    else {
                        // we are getting brighter

                        if (pwm_val == GPIOPWMOutput.getPwmRange()) { // we are at top
                            count_down = true;
                        }else{
                            pwm_val++;
                        }
                    }
                    /*** write your code above this line ***/

                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        /*** remove the break below to just resume thread or add your own action***/
                        break;
                        /*** remove the break above to just resume thread or add your own action ***/

                    }
                }
            });

            /*** write your code you want to execute before you start the thread below this line ***/

            /*** write your code you want to execute before you start the thread above this line ***/

            thread.start();
            /****************** End threadFunction **************************/
        }
        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        /***** Type your HBReset code below this line ******/
        // tell our thread to exit
        exitThread = true;

        // now disable our GPIO pin
        GPIO.clearPinAssignment(GPIO_OUTPUT);
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
