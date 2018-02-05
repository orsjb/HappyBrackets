package net.happybrackets.v2examples.samples.sensorcontroller;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

/**
 * We will use the yaw value of a gyroscope to change the direction of playback by quickly turning clockwise or aniclockwise
 * When the yaw value >- 1, the sample plays forward. When it is <=-1, the sample plays backwards
 */
public class SampleDirectionGyroscope implements HBAction {
    @Override
    public void action(HB hb) {

        /**************************************************************
         * Load a sample and play it
         *
         * simply type samplePLayer-basic to generate this code and press <ENTER> for each parameter
         **************************************************************/
        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
        final float VOLUME = 1; // define how loud we want the sound

        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Roje/i-write.wav";

        // create our actual sample
        Sample sample = SampleManager.sample(SAMPLE_NAME);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player
            SamplePlayer samplePlayer = new SamplePlayer(hb.ac, sample);

            // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
            samplePlayer.setKillOnEnd(false);

            // Connect our sample player to audio
            Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, VOLUME);
            gainAmplifier.addInput(samplePlayer);
            hb.ac.out.addInput(gainAmplifier);

            /******** Write your code below this line ********/
            final int STATIONARY = 0;
            Glide sampleSpeed = new Glide(hb.ac, STATIONARY);

            // now connect the sample playback rate to the samplePlayer
            samplePlayer.setRate(sampleSpeed);

            // we will need to set loop start and end points so when we go in reverse, we don't have silence

            samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
            // now set the loop start and end in the actual sample player
            final float LOOP_START = 0;
            final float LOOP_END = (float)sample.getLength();

            // create our looping objects
            Glide loopStart = new Glide(hb.ac, LOOP_START);
            Glide loopEnd = new Glide(hb.ac, LOOP_END);

            samplePlayer.setLoopStart(loopStart);
            samplePlayer.setLoopEnd(loopEnd);

            // now connect to a gyroscope
            /*****************************************************
             * Find an gyroscope sensor. If no sensor is found
             * you will receive a status message
             *
             * to create this code, simply type gyroscopeSensor
             *****************************************************/
            try {
                hb.findSensor(Gyroscope.class).addValueChangedListener(sensor -> {
                    Gyroscope gyroscope = (Gyroscope) sensor;
                    float pitch = gyroscope.getPitch();
                    float roll = gyroscope.getRoll();
                    float yaw = gyroscope.getYaw();

                    /******** Write your code below this line ********/
                    final int FORWARDS = 1;
                    final int REVERSE = -1;

                    // we will only do a change if our yaw is >= 1 or <= -1
                    if (Math.abs(yaw) >=1){
                        if (yaw < 0) {
                            // must be negative
                            sampleSpeed.setValue(REVERSE);
                        }
                        else {
                            // must be positive
                            sampleSpeed.setValue(FORWARDS);
                        }
                    }

                    /******** Write your code above this line ********/

                });

            } catch (SensorNotFoundException e) {
                hb.setStatus("Unable to find gyroscope");
            }
            /*** End gyroscopeSensor code ***/

            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME);
        }
        /*** End samplePlayer code ***/
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