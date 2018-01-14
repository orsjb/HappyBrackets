package net.happybrackets.examples.Advanced;

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
 * This sketch plays a sound whereby the pitch changes based on the X value of the accelerometer
 * As X value is ositive, pitch increases, and as accelerometer goes negative pitch has lower frequency
 */
public class IncrementAccelerometer implements HBAction {
    @Override
    public void action(HB hb) {

        Glide modFreq = new Glide(hb.ac, 1);
        Glide modDepth = new Glide(hb.ac, 100);
        Glide baseFreq = new Glide(hb.ac, 100);
        Glide gain = new Glide(hb.ac, 0.1f);

        //this is the FM synth
        WavePlayer modulator = new WavePlayer(hb.ac, modFreq, Buffer.SINE);
        Function modFunction = new Function(modulator, modDepth, baseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, gain);
        g.addInput(carrier);
        hb.ac.out.addInput(g);

        //DynamicControl x_display = hb.createDynamicControl(ControlType.FLOAT, "X-count");
        Accelerometer mySensor = (Accelerometer) hb.getSensor(Accelerometer.class);
        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {

                double x_vals = 1;


                @Override
                public void sensorUpdated() {

                    // We are going to see if we have expired

                    long expired = System.currentTimeMillis();


                    // Get the data from Z.

                    float xAxis = (float) mySensor.getAccelerometerX();


                    x_vals += xAxis;

                    //x_display.setValue((float)x_vals);
                    baseFreq.setValue((float) x_vals * 100);
                }
            });
        }
        else
        {
            System.out.println("Unable to Load LSM9DS1");
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