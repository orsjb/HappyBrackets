package examples.threads;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.*;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch plays a sound that will double in frequency on each iteration of the thread function
 * When the frequency is above the maximum, the frequency starts again
 *
 * The sleep time in the thread can be varied with a DynamicControl
 * The thread can also be killed
 */
public class ControlledThread implements HBAction, HBReset {
    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    /***** Type your HBAction code below this line ******/
    final float START_FREQUENCY = 100; // this is the frequency of the waveform we will make
    final float MAX_FREQUENCY = 15000; // This is the maximum frequency

    // This variable needs to be a class variable so we can access it inside the thread code
    float currentFrequency = START_FREQUENCY;
    final float DEFAULT_MULTIPLIER =  2;

    final int DEFAULT_SLEEP_TIME = 1000; // Define the default time our thread sleeps
    final int MINIMUM_SLEEP_TIME = 10;
    final int MAXIMUM_SLEEP_TIME = 1000;

    // define how log our thread will sleep. We will change this with a dynamic Control
    int threadSleepTime = DEFAULT_SLEEP_TIME;

    // add a multiplier to
    float frequencyMultiplier = DEFAULT_MULTIPLIER;

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        WaveModule waveModule = new WaveModule(START_FREQUENCY, 0.1f, Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());

        /* Type threadFunction to generate this code */
        Thread thread = new Thread(() -> {
            int SLEEP_TIME = 1000;
            while (!compositionReset) {/* write your code below this line */
                // double our frequency
                currentFrequency *= 2;
                if (currentFrequency > MAX_FREQUENCY) {
                    currentFrequency = START_FREQUENCY;
                }

                waveModule.setFrequency(currentFrequency);
                // we will override the sleep time in the beginning of theis thread
                SLEEP_TIME = threadSleepTime;
                /* write your code above this line */
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {/* remove the break below to just resume thread or add your own action */

                    waveModule.kill();
                    break;

                }
            }
        });

        /*  write your code you want to execute before you start the thread below this line */

        /* write your code you want to execute before you start the thread above this line */

        thread.start();/* End threadFunction */


        // Type triggerControl to generate this code 
        TriggerControl threadKiller = new TriggerControl(this, "Kill Thread") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line 
                thread.interrupt();
                // Write your DynamicControl code above this line 
            }
        };// End DynamicControl threadKiller code 

        // Create a control pair to set the sleep time of the thread
        // Type intBuddyControl to generate this code
        IntegerControl threadSleepControl = new IntegerControl(this, "Thread Sleep", DEFAULT_SLEEP_TIME) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                threadSleepTime = control_val;
                // Write your DynamicControl code above this line
            }
        }.setDisplayRange(MINIMUM_SLEEP_TIME, MAXIMUM_SLEEP_TIME, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);// End DynamicControl threadSleepControl code


        /***** Type your HBAction code below this line ******/
    }

    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
        compositionReset = true;
        /***** Type your HBReset code below this line ******/

        /***** Type your HBReset code above this line ******/
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
