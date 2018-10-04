package examples.samples.sensorcontroller;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;


import java.lang.invoke.MethodHandles;

/**
 * We will use the x value of an accelerometer to proportionally control the rate of playback
 * When the accelerometer x value is 1, the speed is double. When it is -1, the speed is double in reverse
 */
public class SampleRateAccelerometer implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /**************************************************************
         * Load a sample and play it
         *
         * simply type samplePLayer-basic to generate this code and press <ENTER> for each parameter
         **************************************************************/
        
        final float INITIAL_VOLUME = 1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Roje/i-write.wav";

        // create our actual sample
        Sample sample = SampleManager.sample(SAMPLE_NAME);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player
            SamplePlayer samplePlayer = new SamplePlayer(sample);

            // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
            samplePlayer.setKillOnEnd(false);

            // Connect our sample player to audio
            Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);
            gainAmplifier.addInput(samplePlayer);
            hb.ac.out.addInput(gainAmplifier);

            /******** Write your code below this line ********/
            final int NORMAL_SPEED = 1;
            Glide sampleSpeed = new Glide(NORMAL_SPEED);

            // now connect the sample playback rate to the samplePlayer
            samplePlayer.setRate(sampleSpeed);

            // we will need to set loop start and end points so when we go in reverse, we don't have silence

            samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
            // now set the loop start and end in the actual sample player
            final float LOOP_START = 0;
            final float LOOP_END = (float)sample.getLength();

            // create our looping objects
            Glide loopStart = new Glide(LOOP_START);
            Glide loopEnd = new Glide(LOOP_END);

            samplePlayer.setLoopStart(loopStart);
            samplePlayer.setLoopEnd(loopEnd);


            // now connect to an accelerometer
            /*****************************************************
             * Find an accelerometer sensor. If no sensor is found
             * you will receive a status message
             * accelerometer values typically range from -1 to + 1
             * to create this code, simply type accelerometerSensor
             *****************************************************/
            new AccelerometerListener(hb) {
                @Override
                public void sensorUpdated(float x_val, float y_val, float z_val) {
                    /******** Write your code below this line ********/
                    // we will make speed go double forward or backwards
                    sampleSpeed.setValue(x_val * 2);
                    /******** Write your code above this line ********/

                }
            };
            /*** End accelerometerSensor code ***/


            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME);
        }
        /*** End samplePlayer code ***/
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
