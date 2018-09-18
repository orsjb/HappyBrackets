package net.happybrackets.develop.instruments.sample;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class BasicSamplePlayerChangeSample implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    // This variable will become true when the composition is reset
    boolean compositionReset = false;

    boolean playingRoje = true;
    
    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        
        SampleModule player = new SampleModule();
        player.setRate(1);

        if (!player.setSample(player.EXAMPLE_SAMPLE_NAME))
        {
            hb.setStatus("Failed to load " + player.EXAMPLE_SAMPLE_NAME);
        }

        player.connectTo(hb.ac.out);


        /*** Simply type triggerControl to generate this code ***/
        TriggerControl switchSample = new TriggerControl(this, "Switch Sample") {
            @Override
            public void triggerEvent() {
                /*** Write your DynamicControl code below this line ***/

                // let us also make play in reverse

                if (playingRoje) {
                    player.setRate(-1);
                    player.setSample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
                }
                else {
                    player.setRate(-1);
                    player.setSample(player.EXAMPLE_SAMPLE_NAME);
                }
                playingRoje = !playingRoje;

                /*** Write your DynamicControl code above this line ***/
            }
        };/*** End DynamicControl switchSample code ***/

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
