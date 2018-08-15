package net.happybrackets.develop.gpio;

import com.pi4j.io.gpio.PinPullResistance;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIO;
import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;
import net.happybrackets.device.sensors.gpio.GPIOInput;

import java.lang.invoke.MethodHandles;

public class TestGPIOInput implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");
        GPIO.resetAllGPIO();
        GPIOInput input = GPIOInput.getInputPin(1, PinPullResistance.PULL_DOWN);

        GPIODigitalOutput output = GPIODigitalOutput.getOutputPin(2);

        if (input != null){
            System.out.println("Successful create 1");
        }

        GPIOInput input_mirror = GPIOInput.getInputPin(1, PinPullResistance.PULL_DOWN);
        if (input_mirror != null){
            System.out.println("Successful create input_mirror");
            input_mirror.addStateListener((sensor, new_state) -> {
                System.out.println("New state " + new_state);

                if (output != null){
                    output.setState(new_state);
                }
            });
        }
        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        /***** Type your HBReset code below this line ******/

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
