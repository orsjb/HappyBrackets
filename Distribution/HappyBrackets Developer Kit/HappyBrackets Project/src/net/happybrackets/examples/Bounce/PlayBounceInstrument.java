package net.happybrackets.examples.Bounce;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

public class PlayBounceInstrument implements HBAction {
    final float INITIAL_FREQ = 500;
    float multiplier = 0;


    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {
        HBPermBouncerInstrument bouncer = new HBPermBouncerInstrument(hb, INITIAL_FREQ);

        bouncer.play();
        // Create Our Sound Controls

        // First Freq
        DynamicControl freq_control = hb.createDynamicControl(ControlType.FLOAT, "Base Freq", INITIAL_FREQ)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setBaseFreq((float) control.getValue());
                    }
                });

        // Next Speed
        DynamicControl speed_control = hb.createDynamicControl( ControlType.INT, "Bounce speed", 0)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setSpeed((int) control.getValue());
                    }
                });

        DynamicControl range_control = hb.createDynamicControl( ControlType.FLOAT, "Mod Freq Multiplier", multiplier)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setModFreqFactor((float)control.getValue());
                    }
                });

        DynamicControl depth_control = hb.createDynamicControl(this, ControlType.FLOAT, "Mod depth multiplier", multiplier)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        bouncer.setModDepthMultiplier((float)control.getValue());
                    }
                });

        // add an On / Off switch
        hb.createDynamicControl(this, ControlType.BOOLEAN, "On", true)
                .addControlListener(new DynamicControl.DynamicControlListener() {
                    @Override
                    public void update(DynamicControl control) {
                        boolean playSound = (Boolean) control.getValue();
                        if (playSound)
                        {
                            bouncer.play();
                        }
                        else
                        {
                            bouncer.stop();
                        }

                    }
                });


        // We will Connect the accelerometer here

        Accelerometer sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (sensor != null)
        {
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    // We said X would be Pitch
                    float x_val = (float) sensor.getAccelerometerX();
                    float base_freq = (float) Math.pow(100, x_val + 1) + 50; // this will give us values from 50 to 10050
                    freq_control.setValue((base_freq));

                    // Y will control the speed
                    float y_val = (float) sensor.getAccelerometerY();
                    // we want to make it an int ranging from 8 to 512
                    y_val += 2; // Now it is 1 t0 3
                    float speed = (float) Math.pow(2, y_val * 3);
                    speed_control.setValue(speed);

                    // Z axis will Change mod speed and depth
                    // anything off zero will give us a value
                    //Ranging from 0 to 1
                    float Z_val = (float) sensor.getAccelerometerZ();

                    float abs_val = Math.abs(Z_val);
                    float depth_freq = abs_val * 5000;
                    float mod_freq = abs_val * 10;

                    range_control.setValue(mod_freq);
                    depth_control.setValue(depth_freq);
                }
            });
        }


    }
}