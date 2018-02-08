package net.happybrackets.oldexamples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * Play a sine wave whose frequency is based on Gyroscope X value
 */
public class SimpleGyroscope implements HBAction {
    @Override
    public void action(HB hb) {

        // Define where we are going to set the frequency
        Glide freq = new Glide(hb.ac, 1000);

        // connect our frequency to our waveplayer
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(wp);
        hb.ac.out.addInput(g);

        Gyroscope sensor = (Gyroscope)hb.getSensor(Gyroscope.class);
        if (sensor != null){
            // add the listener
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    float pitch = (float)sensor.getPitch();

                    // Make a zero gyro value = 1 so we will move around 1Khz
                    pitch += 1;

                    //now set the frequency
                    freq.setValue(pitch * 1000);
                }
            });
        }
    }

}
