package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

/**
 * Play a sine wave whose frequency is based on accelerometer X value
 */
public class SimpleAccelerometerDisplay implements HBAction {
    @Override
    public void action(HB hb) {

        DynamicControl freq_display = hb.createDynamicControl(ControlType.FLOAT, "Freq", 0, 0, 2000);
        // Define where we are going to set the frequency
        Glide freq = new Glide(hb.ac, 1000);

        // connect our frequency to our waveplayer
        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(wp);
        hb.ac.out.addInput(g);

        Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (sensor != null){
            // add the listener
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    float x_val = (float)sensor.getAccelerometerX();
                    // convert to 0 to 2
                    x_val += 1;

                    //now set the frequency
                    freq.setValue(x_val * 1000);
                    freq_display.setValue(freq.getValue());
                }
            });
        }
    }


}
