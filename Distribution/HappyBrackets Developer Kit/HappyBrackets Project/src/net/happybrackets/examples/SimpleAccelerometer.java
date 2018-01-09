package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Play a sine wave whose frequency is based on Accelerometer X value
 */
public class SimpleAccelerometer implements HBAction {
    @Override
    public void action(HB hb) {

        // Define where we are going to set the frequency
        Glide freq = new Glide(hb.ac, 2000);

        // connect our frequency to our waveplayer
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(wp);
        hb.sound(g);

        Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (sensor != null){
            // add the listener
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    float x_val = (float)sensor.getAccelerometerX();

                    // convert to 1 to 3
                    x_val += 2;

                    //now set the frequency
                    freq.setValue(x_val * 1000);
                }
            });
        }
    }

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
