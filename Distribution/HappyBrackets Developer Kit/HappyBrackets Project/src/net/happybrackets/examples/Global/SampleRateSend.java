package net.happybrackets.examples.Global;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * A Gyroscope Yaw of +1 or greater makes sample play forward. -1 or less makes it play reverse
 */
public class SampleRateSend implements HBAction {

    @Override
    public void action(HB hb) {

        DynamicControl send_control =  hb.createDynamicControl(ControlType.FLOAT, "Play").setControlScope(ControlScope.GLOBAL);

        DynamicControl yaw_control =  hb.createDynamicControl(ControlType.FLOAT, "Yaw", 0, -1, 1);

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
            hb.ac.out.addInput(g);

            Gyroscope sensor = (Gyroscope)hb.getSensor(Gyroscope.class);
            if (sensor != null)
            {
                sensor.addListener(new SensorUpdateListener() {
                    @Override
                    public void sensorUpdated() {
                        double yaw = sensor.getYaw();

                        yaw_control.setValue(yaw);
                        // see if we are above threshold
                        if (Math.abs(yaw) >= 1)
                        {

                            if (yaw < 0){
                                send_control.setValue(-1);
                                //playback_rate.setValue(-1);
                            }
                            else{
                                send_control.setValue(1);
                                //playback_rate.setValue(1);
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
