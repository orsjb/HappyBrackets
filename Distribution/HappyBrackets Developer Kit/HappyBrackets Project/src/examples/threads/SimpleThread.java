package examples.threads;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch plays a sound that will double in frequency on each iteration of the thread function
 * When the frequency is above the maximum, the frequency starts again
 */
public class SimpleThread implements HBAction, HBReset {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    final float START_FREQUENCY = 100; // this is the frequency of the waveform we will make
    final float MAX_FREQUENCY = 15000; // This is the maximum frequency

    // This variable needs to be a class variable so we can access it inside the thread code
    float currentFrequency = START_FREQUENCY;

    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule player = new WaveModule(START_FREQUENCY, 0.1f, Buffer.SINE);
        player.connectTo(hb.ac.out);

        /* Type threadFunction to generate this code */
        Thread thread = new Thread(() -> {
            int SLEEP_TIME = 1000;
            while (!compositionReset) {/* write your code below this line */
                // double our frequency
                currentFrequency *= 2;
                if (currentFrequency > MAX_FREQUENCY) {
                    currentFrequency = START_FREQUENCY;
                }

                player.setFrequency(currentFrequency);

                /* write your code above this line */
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {/* remove the break below to just resume thread or add your own action */
                    break;

                }
            }
        });

        /*  write your code you want to execute before you start the thread below this line */

        /* write your code you want to execute before you start the thread above this line */

        thread.start();/* End threadFunction */



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

    @Override
    public void doReset() {
        compositionReset = true;
    }
    //</editor-fold>
}
