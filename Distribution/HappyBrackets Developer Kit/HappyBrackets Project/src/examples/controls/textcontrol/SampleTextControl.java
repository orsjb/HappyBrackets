package examples.controls.textcontrol;

import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.TextControl;
import net.happybrackets.core.control.TextControlSender;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class SampleTextControl implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    final String COMMAND_PLAY = "play";
    final String COMMAND_STOP = "stop";
    final String COMMAND_REVERSE = "reverse";
    final String COMMAND_FORWARD = "forwards";

    // Give some instructions to user on the page with a thread
    final String userInstructions [] = {"These are the Player Commands:", COMMAND_PLAY + " " + COMMAND_STOP + " " + COMMAND_REVERSE + " " + COMMAND_FORWARD};
    // create an index we can iterate our instructions with
    int instructionsIndex = 0;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        /* type basicSamplePLayer to generate this code */
        // define our sample name
        final String s = "data/audio/Roje/i-write.wav";
        SampleModule samplePlayer = new SampleModule();
        if (samplePlayer.setSample(s)) {/* Write your code below this line */
            samplePlayer.connectTo(hb.ac.out);

            /* Write your code above this line */
        } else {
            hb.setStatus("Failed sample " + s);
        }/* End samplePlayer code */

        // make our sample loop
        samplePlayer.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);

        // we need a glide to change our direction adn assign to our sample player
        final float FORWARDS_RATE = 1;
        final float REVERSE_RATE = -1;

        samplePlayer.setRate(FORWARDS_RATE);

        // let us create a text control to receive commands

        /* Type textControl to generate this code*/
        TextControl commandControl = new TextControl(this, "Player Commands", "") {
            @Override
            public void valueChanged(String control_val) { /* Write your DynamicControl code below this line */
                if (control_val.equalsIgnoreCase(COMMAND_PLAY)){
                    samplePlayer.pause(false);
                }
                else if (control_val.equalsIgnoreCase(COMMAND_STOP)){
                    samplePlayer.pause(true);
                }
                else if (control_val.equalsIgnoreCase(COMMAND_FORWARD)){
                    samplePlayer.pause(false);
                    samplePlayer.setRate(FORWARDS_RATE);
                }
                else if (control_val.equalsIgnoreCase(COMMAND_REVERSE)){
                    samplePlayer.pause(false);
                    samplePlayer.setRate(REVERSE_RATE);
                }

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl commandControl code */



        // now make our instructions display in a text box

        /* Type textControlSender to generate this code */
        TextControl instructionDisplay = new TextControlSender(this, "Command List", "");

        // Now let us just display the commands in a thread
        /* Type threadFunction to generate this code */
        Thread thread = new Thread(() -> {
            int SLEEP_TIME = 2000;
            while (!compositionReset) {/* write your code below this line */
                // get our next instruction
                String next_instruction = userInstructions[instructionsIndex % userInstructions.length];

                //  now display it
                instructionDisplay.setValue(next_instruction);

                // prepare next instructions
                instructionsIndex++;

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
        /***** Type your HBAction code above this line ******/
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
