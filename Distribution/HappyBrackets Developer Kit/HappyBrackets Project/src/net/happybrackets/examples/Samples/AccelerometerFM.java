package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Basic FM wave generating a  1KHz carrier with a depth of 500Hz
 * Modulating at a rate of 1Hz
 * As we increase X axis, the carrier freq will increase
 * As we move away from centre Y, we will increase modulation depth
 * As we increase Z, starting at -1, we will increase modulation frequency
 */
public class AccelerometerFM implements HBAction {
    @Override
    public void action(HB hb) {

        final float CENTRE_MOD_FREQ = 1;
        final float CENTRE_MOD_DEPTH = 500;
        final float CENTRE_CARRIER_FREQ = 1000;

        // define our parameters
        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 0); // We want no modulation to start with
        Glide modFMDepth = new Glide(hb.ac, CENTRE_MOD_DEPTH);
        Glide baseFmFreq = new Glide(hb.ac, CENTRE_CARRIER_FREQ);

        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SINE);

        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, 0.1f);
        g.addInput(FM_carrier);
        hb.ac.out.addInput(g);

        //now add our sensor
        Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (sensor != null){
            // Add a Listener
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    // get our values from sensor
                    float x_val = (float)sensor.getAccelerometerX();
                    float y_val = (float)sensor.getAccelerometerY();
                    float z_val = (float)sensor.getAccelerometerZ();

                    // Let us make Carrier Freq dependant on x val
                    x_val += 1;
                    baseFmFreq.setValue(CENTRE_CARRIER_FREQ * x_val);

                    // Make Modulator Freq based on Z
                    z_val += 1;
                    modFMFreq.setValue(CENTRE_MOD_FREQ * z_val);

                    // Make depth based on Y
                    //We want level to be zero, so we will make value go from 0 to +1 with zero in centre
                    // Not really necessary to do this abs, however, for understanding we will do it
                    y_val = Math.abs(y_val);
                    modFMDepth.setValue(CENTRE_MOD_DEPTH * y_val);


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
