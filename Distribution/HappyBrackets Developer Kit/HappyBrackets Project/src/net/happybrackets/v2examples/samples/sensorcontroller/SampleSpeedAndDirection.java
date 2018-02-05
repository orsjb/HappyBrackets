package net.happybrackets.v2examples.samples.sensorcontroller;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will modify the speed of playback based on x value of accelerometer
 * and the direction by the yaw of gyroscope
 * Moving accelerometer x from min to max will change speed from 0 to 2
 * Moving yaw >=1 will cause sample to play forward. Moving yaw <= -1 will make sample play backwards
 */
public class SampleSpeedAndDirection implements HBAction {
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


            // define our directions and what our normal speed is
            final int FORWARDS = 1;
            final int REVERSE = -1;
            final float NORMAL_SPEED = 1;

            // define an object to change the speed
            Glide sampleSpeed = new Glide(hb.ac, NORMAL_SPEED);

            // define the object for the direction
            Glide sampleDirection = new Glide(hb.ac, FORWARDS);

            // we need to define a function that will set the playback rate based on speed and direction
            Function playbackRate = new Function( sampleSpeed, sampleDirection) {
                @Override
                public float calculate() {
                    return
                            x[0]    // sampleSpeed - the first parameter of this function
                            * x[1]; // sampleDirection - the second parameter of this function
                }
            };


            // Now set our samplePlayer playback rate based on the return value of that function
            samplePlayer.setRate(playbackRate);

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

                    // we will only do a change if our yaw is >= 1 or <= -1
                    if (Math.abs(yaw) >=1){
                        if (yaw < 0) {
                            // must be negative
                            sampleDirection.setValue(REVERSE);
                        }
                        else {
                            // must be positive
                            sampleDirection.setValue(FORWARDS);
                        }
                    }

                    /******** Write your code above this line ********/

                });

            } catch (SensorNotFoundException e) {
                hb.setStatus("Unable to find gyroscope");
            }
            /*** End gyroscopeSensor code ***/


            // now connect to an accelerometer
            /*****************************************************
             * Find an accelerometer sensor. If no sensor is found
             * you will receive a status message
             * accelerometer values typically range from -1 to + 1
             * to create this code, simply type accelerometerSensor
             *****************************************************/
            try {
                hb.findSensor(Accelerometer.class).addValueChangedListener(sensor -> {
                    Accelerometer accelerometer = (Accelerometer) sensor;
                    float x_val = accelerometer.getAccelerometerX();
                    float y_val = accelerometer.getAccelerometerY();
                    float z_val = accelerometer.getAccelerometerZ();

                    /******** Write your code below this line ********/

                    // we will make range go from stopped to double
                    sampleSpeed.setValue(x_val + 1);

                    /******** Write your code above this line ********/

                });

            } catch (SensorNotFoundException e) {
                hb.setStatus("Unable to create Accelerometer");
            }
            /*** End accelerometerSensor code ***/

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