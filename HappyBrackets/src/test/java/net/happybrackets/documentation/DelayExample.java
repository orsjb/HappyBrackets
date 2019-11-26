package net.happybrackets.documentation;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.gpio.GPIODigitalOutput;

import java.lang.invoke.MethodHandles;

public class DelayExample implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        /* Type gpioDigitalOut to create this code */

        final int GPIO_OUTPUT = 23;
        GPIODigitalOutput outputPin = GPIODigitalOutput.getOutputPin(GPIO_OUTPUT);

        if (outputPin != null) {
            outputPin.setState(true);

            // now we will turn it off after 5 seconds
            new Delay(5000, outputPin, (delay_offset, param) -> {
                // delay_offset is how far out we were from our exact delay time in ms and is a double
                // param is the parameter we passed in, which was the output pin
                ((GPIODigitalOutput) param).setState(false);
            });
        }
        
        Object parameter = new Object();
        new Delay(100, parameter, (offset, param) -> {
            
        });


        // This delay will take 50 seconds
        Delay longdelay = new Delay(50000, null, (delay_offset, param) -> {
            // delay_offset is how far out we were from our exact delay time in ms and is a double
            // param is the parameter we passed in type your code below this line
            System.out.println("Delay Complete");
            // type your code above this line
        });

        // 30 seconds in the future - we will stop longdelay completing
        longdelay.stop();
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
