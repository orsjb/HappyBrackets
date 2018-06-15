package examples.controls.textcontrol;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch receives commands to control a SamplePlayer via a text Dynamic control
 * When the user types a command and presses enter, the listener actions the command
 *
 * A thread also runs that displays the command list to the user.
 */
public class SampleTextControl implements HBAction {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    // define our text commands
    final String COMMAND_PLAY = "play";
    final String COMMAND_STOP = "stop";
    final String COMMAND_REVERSE = "reverse";
    final String COMMAND_FORWARD = "forwards";

    // Give some instructions to user on the page with a thread
    final String userInstructions [] = {"These are the Player Commands:", COMMAND_PLAY + " " + COMMAND_STOP + " " + COMMAND_REVERSE + " " + COMMAND_FORWARD};
    // create an index we can iterate our instructions with
    int instructionsIndex = 0;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /**************************************************************
         * Load a sample and play it
         *
         * simply type samplePLayer-basic to generate this code and press <ENTER> for each parameter
         **************************************************************/
        
        final float INITIAL_VOLUME = 1; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // Define our sample name
        final String SAMPLE_NAME = "data/audio/Roje/i-write.wav";

        // create our actual sample
        Sample sample = SampleManager.sample(SAMPLE_NAME);

        // test if we opened the sample successfully
        if (sample != null) {
            // Create our sample player
            SamplePlayer samplePlayer = new SamplePlayer(sample);
            // Samples are killed by default at end. We will stop this default actions so our sample will stay alive
            samplePlayer.setKillOnEnd(false);

            // Connect our sample player to audio
            Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);
            gainAmplifier.addInput(samplePlayer);
            hb.ac.out.addInput(gainAmplifier);

            /******** Write your code below this line ********/

            // make our sample loop
            samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

            // we need a glide to change our direction adn assign to our sample player
            final float FORWARDS_RATE = 1;
            final float REVERSE_RATE = -1;

            Glide playbackDirection = new Glide(FORWARDS_RATE);
            samplePlayer.setRate(playbackDirection);

            // let us create a text control to receive commands
            /*************************************************************
             * Create a string type Dynamic Control that displays as a text box
             * Simply type textControl to generate this code
             *************************************************************/
            TextControl commandControl = new TextControl(this, "Player Commands", "") {
                @Override
                public void valueChanged(String control_val) {
                    /*** Write your DynamicControl code below this line ***/
                    if (control_val.equalsIgnoreCase(COMMAND_PLAY)){
                        samplePlayer.pause(false);
                    }
                    else if (control_val.equalsIgnoreCase(COMMAND_STOP)){
                        samplePlayer.pause(true);
                    }
                    else if (control_val.equalsIgnoreCase(COMMAND_FORWARD)){
                        samplePlayer.pause(false);
                        playbackDirection.setValue(FORWARDS_RATE);
                    }
                    else if (control_val.equalsIgnoreCase(COMMAND_REVERSE)){
                        samplePlayer.pause(false);
                        playbackDirection.setValue(REVERSE_RATE);
                    }

                    /*** Write your DynamicControl code above this line ***/
                }
            };/*** End DynamicControl commandControl code ***/



            // now make our instructions display in a text box
            /*************************************************************
             * Create a string type Dynamic Control that displays as a text box
             * Simply type textControl to generate this code
             *************************************************************/
            TextControl instructionDisplay = new TextControl(this, "Command List", "") {
                @Override
                public void valueChanged(String control_val) {
                    /*** Write your DynamicControl code below this line ***/

                    /*** Write your DynamicControl code above this line ***/
                }
            };/*** End DynamicControl instructionsDisplay code ***/



            /***********************************************************
             * Create a runnable thread object
             * simply type threadFunction to generate this code
             ***********************************************************/
            Thread instructionsThread = new Thread(() -> {
                int SLEEP_TIME = 2000;
                while (true) {
                    /*** write your code below this line ***/

                    // get our next instruction
                    String next_instruction = userInstructions[instructionsIndex % userInstructions.length];

                    //  now display it
                    instructionDisplay.setValue(next_instruction);

                    // prepare next instructions
                    instructionsIndex++;

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

            instructionsThread.start();
            /****************** End threadFunction **************************/
            /******** Write your code above this line ********/
        } else {
            hb.setStatus("Failed sample " + SAMPLE_NAME);
        }
        /*** End samplePlayer code ***/
        /***** Type your HBAction code above this line ******/
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
