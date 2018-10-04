package examples.sensor.accelerometer;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;

import java.lang.invoke.MethodHandles;

/* THis composition will use the accelerometer sensor and map each axis to a different wave Type

 */
public class TripleAxisAccelerometer implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule x_player = new WaveModule(1000, 0.1f, Buffer.SINE);
        x_player.connectTo(hb.ac.out);

        WaveModule y_player = new WaveModule(1000, 0.1f, Buffer.SQUARE);
        y_player.connectTo(hb.ac.out);

        WaveModule z_player = new WaveModule(1000, 0.1f, Buffer.SAW);
        z_player.connectTo(hb.ac.out);


        /** type accelerometerSensor to create this. Values typically range from -1 to + 1 **/
        new AccelerometerListener(hb) {
            @Override
            public void sensorUpdated(float x_val, float y_val, float z_val) { /*     Write your code below this line     */

                x_player.setFequency(scaleValue(x_val, 0, 1000));
                y_player.setFequency(scaleValue(y_val, 0, 1000));
                z_player.setFequency(scaleValue(z_val, 0, 1000));

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
