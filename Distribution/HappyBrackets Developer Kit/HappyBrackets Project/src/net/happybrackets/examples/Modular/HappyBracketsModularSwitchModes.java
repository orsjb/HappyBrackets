package net.happybrackets.examples.Modular;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.examples.Bounce.HBPermBouncer;
import net.happybrackets.examples.FM.HBPermFM;

import java.lang.invoke.MethodHandles;

public class HappyBracketsModularSwitchModes implements HBAction {
    float initialFreq = 500;

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {
        HBPermBouncer bouncer = new HBPermBouncer(hb, initialFreq);
        HBPermFM fm = new HBPermFM(hb, initialFreq);

        hb.createDynamicControl(ControlType.FLOAT, "Z", 1, -1, 1).addControlListener(new DynamicControl.DynamicControlListener() {
            @Override
            public void update(DynamicControl dynamicControl) {

                float zAxis = (float)dynamicControl.getValue();
                boolean bounce_mode = zAxis < 0;

                if (bounce_mode)
                {
                    fm.setGainLevel(0);
                    bouncer.play();
                }
                else
                {
                    fm.setGainLevel(1);
                    bouncer.stop();
                }
            }
        });

        LSM9DS1 mySensor = (LSM9DS1) hb.getSensor(LSM9DS1.class);
        if (mySensor != null) {
            mySensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    double zAxis = mySensor.getAccelerometerZ();
                    double yAxis = mySensor.getAccelerometerY();
                    double xAxis = mySensor.getAccelerometerX();

                    // X was Freq
                    float x_val = (float) xAxis;
                    float base_freq = (float) Math.pow(100, x_val + 1) + 50; // this will give us values from 50 to 10050
                    bouncer.setBaseFreq(base_freq);

                    // Y was Speed
                    float y_val = (float) yAxis;
                    // we want to make it an int ranging from 8 to 512
                    float speed = (float) Math.pow(2, (y_val + 2) * 3);
                    bouncer.setSpeed((int)speed);

                    // Z was Modulation
                    float z_val = (float) zAxis;

                    float mod_freq = (float) xAxis * 1000;


                    // we will swap Modes Based on Z Value
                    boolean bounce_mode = zAxis < 0;

                    if (bounce_mode)
                    {
                        fm.setGainLevel(0);
                        bouncer.play();
                    }
                    else
                    {
                        fm.setGainLevel(1);
                        bouncer.stop();
                    }

                    // anything off zero will give us a value
                    //Ranging from 0 to 1
                    float abs_val = Math.abs(z_val);
                    float depth_freq = abs_val * 5000;
                    mod_freq = abs_val * 10;
                    bouncer.setModDepth(depth_freq);
                    bouncer.setModFreq(mod_freq);

                    // Do FM Parameters Now
                    float fm_freq = (float) Math.pow(100, x_val + 1) + 50; // this will give us values from 50 to 10050
                    fm.setBaseFreq(fm_freq);
                    float fm_depth_freq = Math.abs(z_val) * 5000;
                    fm.setModDepth(fm_depth_freq);

                    // we want to make it an int ranging from 8 to 512

                    float freq = (float) Math.pow(2, (y_val + 2) * 3);
                    fm.setModFreq(freq);
                }
            });
        }

    }
}

