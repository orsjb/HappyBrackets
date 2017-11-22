package net.happybrackets.examples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

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
        hb.sound(g);

        //DynamicControl x_display = hb.createDynamicControl(ControlType.FLOAT, "X-count");
        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        mySensor.addListener(new SensorUpdateListener() {




            double x_vals = 1;


            @Override
            public void sensorUpdated() {

                // We are going to see if we have expired

                long expired = System.currentTimeMillis();


                // Get the data from Z.
                double zAxis = mySensor.getAccelerometerData()[2];
                double yAxis = mySensor.getAccelerometerData()[1];
                double xAxis = mySensor.getAccelerometerData()[0];


                x_vals += xAxis;

                //x_display.setValue((float)x_vals);
                baseFreq.setValue((float)x_vals *100);
            }
        });

    }
}