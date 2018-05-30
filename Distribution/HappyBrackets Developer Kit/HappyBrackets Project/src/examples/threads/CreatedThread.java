package examples.threads;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch plays a sound that will double in frequency on each iteration of the thread function
 * When the frequency is above the maximum, the frequency starts again
 */
public class CreatedThread implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

    /***** Type your HBAction code below this line ******/
    final float START_FREQUENCY = 200; // this is the frequency of the waveform we will make
    final float MAX_FREQUENCY = 15000; // This is the maximum frequency

    // This variable needs to be a class variable so we can access it inside the thread code
    float currentFrequency = START_FREQUENCY;
    final float DEFAULT_MULTIPLIER =  2;
    final int DEFAULT_SLEEP_TIME = 1000; // Define the default time our thread sleeps
    final int MINIMUM_SLEEP_TIME = 10;
    final int MAXIMUM_SLEEP_TIME = 1000;

    // add a multiplier to
    float frequencyMultiplier = DEFAULT_MULTIPLIER;

    // define a thread that we can start and stop
    Thread runningThread =  null;

    int threadSleepTime = DEFAULT_SLEEP_TIME;

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        Glide waveformFrequency = new Glide(START_FREQUENCY);

        
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);


        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        DynamicControl threadStarter = hb.createDynamicControl(this, ControlType.TRIGGER, "New Thread")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/

                    // first see if we have a thread running. If we do, then kill it
                    if (runningThread != null) {
                        runningThread.interrupt();
                        runningThread = null;
                    }

                    /***********************************************************
                     * Create a runnable thread object
                     * simply type threadFunction to generate this code
                     ***********************************************************/
                    runningThread = new Thread(() -> {
                        int SLEEP_TIME = 1000;
                        while (true) {
                            /*** write your code below this line ***/
                            // double our frequency
                            currentFrequency *= 2;
                            if (currentFrequency > MAX_FREQUENCY) {
                                currentFrequency = START_FREQUENCY;
                            }

                            waveformFrequency.setValue(currentFrequency);

                            // we will override the sleep time in the beginning of theis thread
                            SLEEP_TIME = threadSleepTime;
                            /*** write your code above this line ***/

                            try {
                                Thread.sleep(SLEEP_TIME);
                            } catch (InterruptedException e) {
                                /*** remove the break below to just resume thread or add your own action***/
                                break;
                                /*** remove the break above to just resume thread or add your own action ***/
                            }
                        }
                    });

                    /*** write your code you want to execute before you start the thread below this line ***/

                    /*** write your code you want to execute before you start the thread above this line ***/

                    runningThread.start();
                    /****************** End threadObject**************************/

                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        DynamicControl threadKiller = hb.createDynamicControl(this, ControlType.TRIGGER, "Kill Thread")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/
                    if (runningThread != null) {
                        runningThread.interrupt();
                        runningThread = null;
                    }
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

        // Create a control pair to set the sleep time of the thread
        /*************************************************************
         * Create an integer type Dynamic Control pair that displays as a slider and text box
         *
         * Simply type intBuddyControl to generate this code
         *************************************************************/
        DynamicControl threadSleepControl = hb.createControlBuddyPair(this, ControlType.INT, "Thread Sleep", DEFAULT_SLEEP_TIME, MINIMUM_SLEEP_TIME, MAXIMUM_SLEEP_TIME)
                .addControlListener(control -> {
                    int control_val = (int) control.getValue();

                    /*** Write your DynamicControl code below this line ***/
                    threadSleepTime = control_val;
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

        /***** Type your HBAction code below this line ******/
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
