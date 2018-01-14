package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Play a simple sample with a loop. Accelerometer X defines Start of Loop, Y defines duration
 */
public class SampleLoopAccelerometer implements HBAction {
    float sampleDuration = 500;

    @Override
    public void action(HB hb) {
        // Define our sampler
        Sample sample = SampleManager.sample("data/audio/hiphop.wav");
        if (sample != null) {
            final float maxSamplePosition = (float)sample.getLength();

            SamplePlayer sp = new SamplePlayer(hb.ac, sample);

            // define Loop Direction
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            Glide gainEnv = new Glide(hb.ac, 1f, 500);

            // define our start and end points
            Glide  loop_start = new Glide(hb.ac, 0);
            Glide looop_end = new Glide(hb.ac, sampleDuration);
            sp.setLoopStart(loop_start);
            sp.setLoopEnd(looop_end);

            // Connect our sample player to audio
            Gain g = new Gain(hb.ac, 1, gainEnv);
            g.addInput(sp);
            hb.ac.out.addInput(g);

            Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
            if (sensor != null){
                sensor.addListener(new SensorUpdateListener() {
                    @Override
                    public void sensorUpdated() {
                        // get our values from sensor
                        float x_val = (float)sensor.getAccelerometerX();
                        float y_val = (float)sensor.getAccelerometerY();
                        float z_val = (float)sensor.getAccelerometerZ();

                        // convert x_val to 0 to 1
                        x_val = (x_val + 1) / 2;
                        float new_loop_start = x_val *  maxSamplePosition;


                        sampleDuration = 500 + (Math.abs(y_val) * 5000);

                        float new_loop_end = new_loop_start + sampleDuration;

                        // our loop end must be inside sample length
                        if (new_loop_end > maxSamplePosition){
                            new_loop_end = maxSamplePosition;
                        }


                        // If our loop start or end is outside the loop, we need to set the position
                        float current_audio_position = (float)sp.getPosition();
                        if (current_audio_position < new_loop_start || current_audio_position > new_loop_end){

                            // stop popping by setting gain to zero and then back
                            gainEnv.setValue(0);
                            sp.setPosition(new_loop_start);
                            gainEnv.setValue(1);
                        }

                        loop_start.setValue(new_loop_start);
                        looop_end.setValue(new_loop_end);
                    }
                });
            }
        }
    }

    /**
     * This function is used when running sketch in IntelliJ for debugging or testing
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
