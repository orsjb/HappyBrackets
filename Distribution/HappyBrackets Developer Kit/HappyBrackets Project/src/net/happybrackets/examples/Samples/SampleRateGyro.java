package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * A Gyroscope Yaw of +1 or greater makes sample play forward. -1 or less makes it play reverse
 */
public class SampleRateGyro implements HBAction {

    @Override
    public void action(HB hb) {
        // Define our sampler
        Sample sample = SampleManager.sample("data/audio/hiphop.wav");
        if (sample != null) {
            SamplePlayer sp = new SamplePlayer(hb.ac, sample);
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // define our sample rate
            Glide playback_rate = new Glide(hb.ac, 0, 50);
            sp.setRate(playback_rate);

            // Connect our sample player to audio
            Gain g = new Gain(hb.ac, 1, 1);
            g.addInput(sp);
            hb.sound(g);

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
                                playback_rate.setValue(-1);
                            }
                            else{
                                playback_rate.setValue(1);
                            }
                        }
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
