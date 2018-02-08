package net.happybrackets.oldexamples.Modular;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

public class HappyBracketsSwitchModes  implements HBAction {
    float initialFreq = 500;

    Clock clock;

    boolean playSound = false;

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {


        clock = new Clock(hb.ac, 500);


        Glide bouncerModFreq = new Glide(hb.ac, 1);
        Glide bouncerModDepth = new Glide(hb.ac, 0);
        Glide bouncerBaseFreq = new Glide(hb.ac, initialFreq);

        WavePlayer bouncerModulator = new WavePlayer(hb.ac, bouncerModFreq, Buffer.SINE);
        Function bouncerModFunction = new Function(bouncerModulator, bouncerModDepth, bouncerBaseFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };


        // Players for second sound
        //this is the FM synth
        Glide modFMFreq = new Glide(hb.ac, 666);
        Glide modFMDepth = new Glide(hb.ac, 100);
        Glide baseFmFreq = new Glide(hb.ac, 1000);
        Glide FmGain = new Glide(hb.ac, 0.1f);
        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SINE);
        Function modFMFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };

        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFMFunction, Buffer.SINE);

        Gain g = new Gain(hb.ac, 1, FmGain);
        g.addInput(FM_carrier);
        hb.ac.out.addInput(g);

        hb.ac.out.addDependent(clock);



        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    double zAxis = mySensor.getAccelerometerData()[2];
                    double yAxis = mySensor.getAccelerometerData()[1];
                    double xAxis = mySensor.getAccelerometerData()[0];

                    // X was Freq
                    float x_val = (float) xAxis;
                    float base_freq = (float) Math.pow(100, x_val + 1) + 50; // this will give us values from 50 to 10050
                    bouncerBaseFreq.setValue(base_freq);

                    // Y was Speed
                    float y_val = (float) yAxis;
                    // we want to make it an int ranging from 8 to 512
                    float speed = (float) Math.pow(2, (y_val + 2) * 3);
                    clock.setTicksPerBeat((int) speed);

                    // Z was Modulation
                    float z_val = (float) zAxis;

                    float mod_freq = (float) xAxis * 1000;


                    // we will swap Modes Based on Z Value
                    boolean bounce_mode = zAxis < 0;
                    FM_carrier.pause(bounce_mode);
                    playSound = bounce_mode;

                    // anything off zero will give us a value
                    //Ranging from 0 to 1
                    float abs_val = Math.abs(z_val);
                    float depth_freq = abs_val * 5000;
                    mod_freq = abs_val * 10;
                    bouncerModDepth.setValue(depth_freq);
                    bouncerModFreq.setValue(mod_freq);

                    // Do FM Parameters Now
                    float fm_freq = (float) Math.pow(100, x_val + 1) + 50; // this will give us values from 50 to 10050
                    baseFmFreq.setValue(fm_freq);

                    float fm_depth_freq = Math.abs(z_val) * 5000;
                    bouncerModDepth.setValue(fm_depth_freq);

                    // we want to make it an int ranging from 8 to 512

                    float freq = (float) Math.pow(2, (y_val + 2) * 3);
                    modFMDepth.setValue(freq);
                }
            });
        }

        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (clock.getCount() % 16 == 0 && playSound) {
                    //add the waveplayer

                    WavePlayer wp = new WavePlayer(hb.ac, bouncerModFunction, Buffer.SINE);
                    //add the gain
                    Envelope e = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 200, new KillTrigger(g));
                    //connect together
                    g.addInput(wp);
                    hb.ac.out.addInput(g);
                }
            }
        });
    }
}

