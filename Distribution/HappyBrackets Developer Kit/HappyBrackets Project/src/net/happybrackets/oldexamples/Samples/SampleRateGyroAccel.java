package net.happybrackets.oldexamples.Samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * A Gyroscope Yaw of +1 or greater makes sample play forward. -1 or less makes it play reverse
 * accelerometer X will make sample play faster or slower
 */
public class SampleRateGyroAccel implements HBAction {

    @Override
    public void action(HB hb) {
        // Define our sampler
        Sample sample = SampleManager.sample("data/audio/hiphop.wav");
        if (sample != null) {
            SamplePlayer sp = new SamplePlayer(hb.ac, sample);
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // define our sample rate
            Glide playback_rate = new Glide(hb.ac, 1, 50);       // we will control this with X
            Glide playback_direction = new Glide(hb.ac, 0, 50);  // We will control this with yaw

            Function calulated_rate = new Function(playback_rate, playback_direction) {
                @Override
                public float calculate() {
                    return x[0] * x[1];
                }
            };

            sp.setRate(calulated_rate);

            // Connect our sample player to audio
            Gain g = new Gain(hb.ac, 1, 1);
            g.addInput(sp);
            hb.ac.out.addInput(g);

            Gyroscope sensor = (Gyroscope)hb.getSensor(Gyroscope.class);
            if (sensor != null)
            {
                sensor.addListener(new SensorUpdateListener() {
                    @Override
                    public void sensorUpdated() {
                        double yaw = sensor.getYaw();

                        // see if we are above threshold
                        if (Math.abs(yaw) >= 1)
                        {
                            if (yaw < 0){
                                playback_direction.setValue(-1);
                            }
                            else{
                                playback_direction.setValue(1);
                            }
                        }
                    }
                });
            }

            Accelerometer accelerometer = (Accelerometer)hb.getSensor(Accelerometer.class);
            if (accelerometer != null){
                accelerometer.addListener(new SensorUpdateListener() {
                    @Override
                    public void sensorUpdated() {
                        float x_val = (float)accelerometer.getAccelerometerX();
                        // make 0 to 2
                        x_val +=1;
                        playback_rate.setValue(x_val);
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
