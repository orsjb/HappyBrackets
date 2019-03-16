package examples.osc;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Sends accelerometer messages to  OSC on "127.0.0.1", 9000
 */
public class OSCSendAccelerometer implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        //hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        OSCUDPSender oscSender = new OSCUDPSender();

        /** type accelerometerSensor to create this. Values typically range from -1 to + 1 **/
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) { /*     Write your code below this line     */
                // send OSC message to "127.0.0.1", 9000
                oscSender.send(HB.createOSCMessage("/hb/accelerometer", x_val, y_val, z_val), "127.0.0.1", 9000);

                /*  Write your code above this line        */
            }
        };/*  End accelerometerSensor  */

        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
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
