package net.happybrackets.examples.Siren;

import javafx.scene.transform.Scale;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Accelerometer;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

import java.lang.invoke.MethodHandles;

import static java.lang.Math.abs;

public class HappyBracketsSirenAccelerate implements HBAction {
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(HB hb) {

        final float MAX_GAIN = 1;
        final float MAX_RANGE = 1.5f;
        final float BASE_FREQ = 800;
        final float MAX_BASE = BASE_FREQ * MAX_RANGE;
        final float MIN_BASE = BASE_FREQ / MAX_RANGE;
        final float MOD_DEPTH = 200;
        final float MAX_MOD = MOD_DEPTH * MAX_RANGE;
        final float MIN_MOD = MOD_DEPTH / MAX_RANGE;

        // we can expect our gyro to go between +- MAX_GYRO
        final float MAX_GYRO = 4;

        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, 0.5f);
        Glide modFMDepth = new Glide(hb.ac, MOD_DEPTH);
        Glide baseFmFreq = new Glide(hb.ac, BASE_FREQ);
        Glide FmGain = new Glide(hb.ac, MAX_GAIN / 2);


        //this is the FM synth
        WavePlayer FM_modulator = new WavePlayer(hb.ac, modFMFreq, Buffer.SQUARE);
        Function modFunction = new Function(FM_modulator, modFMDepth, baseFmFreq) {
            @Override
            public float calculate() {
                return x[0] * x[1] + x[2];
            }
        };
        WavePlayer FM_carrier = new WavePlayer(hb.ac, modFunction, Buffer.SINE);
        Gain g = new Gain(hb.ac, 1, FmGain);
        g.addInput(FM_carrier);
        hb.ac.out.addInput(g);
        Accelerometer accelerometer = (Accelerometer) hb.getSensor(Accelerometer.class);
        if (accelerometer != null){
            accelerometer.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    double x_accel =  accelerometer.getAccelerometerX();
                    double accel_range = (x_accel + 1) / (2);

                    // Let us adjust the gain now
                    double new_gain = accel_range * MAX_GAIN;
                    FmGain.setValue((float) new_gain);

                }
            });
        }
        else{
            hb.setStatus("Unable to load Accelerometer");
        }

        Gyroscope gyroscope = (Gyroscope)hb.getSensor(Gyroscope.class);
        if (gyroscope != null){
            gyroscope.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    // it appears that Gyro is opposite to Aceleration
                    double gyro_pitch = gyroscope.getPitch() * -1;
                    double gyro_range = (gyro_pitch + MAX_GYRO) / 2;
                    // scale the acceleration to the new value
                    double new_freq = gyro_range * (MAX_BASE - MIN_BASE) + MIN_BASE;

                    double new_mod = gyro_range * (MAX_MOD - MIN_MOD) + MIN_MOD;

                    baseFmFreq.setValue((float)new_freq);

                    //modFMDepth.setValue((float) new_mod);
                }
            });
        }
    }
}
