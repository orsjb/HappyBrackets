package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.SensorUpdateListener;

import java.lang.invoke.MethodHandles;

/**
 * Basic FM wave generating a  1KHz carrier with a depth of 500Hz
 * Modulating at a rate of 1Hz
 * As we change Pitch, the carrier freq will increase
 * As we change roll, we will change modulation depth
 * As we increase Yaw, we will change modulation frequency
 */
public class GyroscopeFM implements HBAction {
    @Override
    public void action(HB hb) {

        final float CENTRE_MOD_FREQ = 1;
        final float CENTRE_MOD_DEPTH = 500;
        final float CENTRE_CARRIER_FREQ = 1000;

        // define our parameters
        //these are the parameters that control the FM synth
        Glide modFMFreq = new Glide(hb.ac, CENTRE_MOD_FREQ);
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
        hb.sound(g);

        //now add our sensor
        Gyroscope sensor = (Gyroscope)hb.getSensor(Gyroscope.class);
        if (sensor != null){
            // Add a Listener
            sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    // get our values form sensor
                    float pitch = (float)sensor.getPitch();
                    float roll = (float)sensor.getRoll();
                    float yaw = (float)sensor.getYaw();

                    // Let us make Carrier Freq dependant on pitch val
                    pitch += 1;
                    baseFmFreq.setValue(CENTRE_CARRIER_FREQ * pitch);

                    // Make Modulator Freq based on yaw
                    yaw += 1;
                    modFMFreq.setValue(CENTRE_MOD_FREQ * yaw);

                    // Make depth based on roll
                    roll += 1;
                    modFMDepth.setValue(CENTRE_MOD_DEPTH * roll);


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
