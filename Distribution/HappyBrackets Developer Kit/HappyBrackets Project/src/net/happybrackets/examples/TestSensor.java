package net.happybrackets.examples;

import net.beadsproject.beads.data.Pitch;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;

import java.lang.invoke.MethodHandles;

public class TestSensor implements HBAction {
    @Override
    public void action(HB hb) {


    }

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
}
