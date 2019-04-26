package examples.sensor.gyroscope;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.GyroscopeListener;

import java.lang.invoke.MethodHandles;

/**
 * Uses gyroscope sensor to map each axis to the frequency of three different waveforms
 */
public class TripleAxisGyroscope implements HBAction, HBReset {
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
        // define the centre frequency we will use
        final float CENTRE_FREQUENCY = 1000;

        // define the amount we will change the waveformFrequency by based on gyroscope value
        final float MULTIPLIER_FREQUENCY = 500;

        WaveModule yaw_player = new WaveModule(CENTRE_FREQUENCY, 0.1f, Buffer.SINE);
        yaw_player.connectTo(hb.ac.out);

        WaveModule pitch_player = new WaveModule(CENTRE_FREQUENCY, 0.1f, Buffer.SQUARE);
        pitch_player.connectTo(hb.ac.out);

        WaveModule roll_player = new WaveModule(CENTRE_FREQUENCY, 0.1f, Buffer.SAW);
        roll_player.connectTo(hb.ac.out);

        /** type gyroscopeSensor to create this. Values typically range from -1 to + 1 **/
        new GyroscopeListener(hb) {
            @Override
            public void sensorUpdated(float pitch, float roll, float yaw) {/* Write your code below this line */
                yaw_player.setFrequency(yaw * MULTIPLIER_FREQUENCY + CENTRE_FREQUENCY);
                pitch_player.setFrequency(pitch * MULTIPLIER_FREQUENCY + CENTRE_FREQUENCY);
                roll_player.setFrequency(roll * MULTIPLIER_FREQUENCY + CENTRE_FREQUENCY);


                /* Write your code above this line */
            }
        };/*** End gyroscopeSensor code ***/
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
