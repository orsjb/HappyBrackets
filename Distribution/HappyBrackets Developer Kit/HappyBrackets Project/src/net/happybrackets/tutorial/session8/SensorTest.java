package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.MiniMU;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 */
public class SensorTest implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        hb.masterGainEnv.setValue(0.1f);
        Glide freq = new Glide(hb.ac, 500, 500);
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
        hb.sound(wp);
        MiniMU mm = (MiniMU)hb.getSensor(MiniMU.class);
        mm.clearListeners();
        mm.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                double x = mm.getAccelerometerData()[0];
                x = Math.abs(x) / 400f;
                System.out.println(x);
                freq.setValue((float)x * 5000f + 200f);
            }
        });
    }

}
