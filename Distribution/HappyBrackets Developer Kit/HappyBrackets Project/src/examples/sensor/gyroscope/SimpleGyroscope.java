package examples.sensor.gyroscope;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.Gyroscope;
import net.happybrackets.device.sensors.GyroscopeListener;
import net.happybrackets.device.sensors.SensorNotFoundException;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a sine wave whose frequency is dependant upon yaw of gyroscope
 * gyroscope value is zero when device is stationary, so the sound will change pitch and then go back
 * Rotate device to change gyroscope value
 * we are using a function to calculate the frequency
 */
public class SimpleGyroscope implements HBAction{
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // define the centre frequency we will use
        final float CENTRE_FREQUENCY = 1000;

        // define the amount we will change the waveformFrequency by based on gyroscope value
        final float MULTIPLIER_FREQUENCY = 500;

        WaveModule player = new WaveModule();
        player.setFequency(new Function(hb.getGyroscopeYaw()) {
            @Override
            public float calculate() {
                float yaw = x[0];
                float frequency_deviation = yaw * MULTIPLIER_FREQUENCY;
                return CENTRE_FREQUENCY + frequency_deviation;
            }
        });

        player.connectTo(hb.ac.out);
    }


    //<editor-fold defaultstate="collapsed" desc="Debug Start">
    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
