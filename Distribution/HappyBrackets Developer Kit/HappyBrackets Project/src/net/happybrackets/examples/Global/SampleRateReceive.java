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

import java.lang.invoke.MethodHandles;

/**
 * A Gyroscope Yaw of +1 or greater makes sample play forward. -1 or less makes it play reverse
 */
public class SampleRateReceive implements HBAction {

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
            hb.ac.out.addInput(g);

            hb.createDynamicControl(ControlType.FLOAT, "Play").setControlScope(ControlScope.GLOBAL)
                    .addControlListener(new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl dynamicControl) {
                            float rate = (float)dynamicControl.getValue();
                            playback_rate.setValue(rate);
                        }
                    });

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
