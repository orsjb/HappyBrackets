package net.happybrackets.tutorial.session8;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * Created by ollie on 6/06/2016.
 *
 * Do pythagoras to
 *
 *
 */
public class CodeTask8_2 implements HBAction {


    @Override
    public void action(HB hb) {

        // reset HB
        hb.reset();

        //load a set of sounds
        Envelope freq = new Envelope(hb.ac, 440);

        LSM9DS1 lsm = (LSM9DS1)hb.getSensor(LSM9DS1.class);

        // Sine Generator
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);

        // Gain control
        Gain g = new Gain(hb.ac, 1, 0.1f);

        // Hook up the UGens
        g.addInput(wp);
        hb.ac.out.addInput(g);

        lsm.addListener(new SensorUpdateListener() {

            @Override
            public void sensorUpdated() {

                // get the accelerometer data
                double x = lsm.getAccelerometerData()[0];

                // change the frequency
                freq.addSegment(10, (float) x);

            }

        });

    }
}
