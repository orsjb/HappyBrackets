package examples.sensor.accelerometer;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.AccelerometerListener;


import java.lang.invoke.MethodHandles;

/**
 * This sketch will play a sine wave whose frequency is dependant upon x axis of accelerometer
 * accelerometer values typically range from -1 to +1, however, we are scaling it from 0 to 2000
 */
public class SimpleAccelerometer implements HBAction{
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        WaveModule player = new WaveModule();

        // Map a scaled value of Accelerometer X to the frequency
        player.setFequency(hb.getAccelerometer_X(0, 2000));
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
