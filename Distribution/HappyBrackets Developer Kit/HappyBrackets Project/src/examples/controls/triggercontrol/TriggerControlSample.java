package examples.controls.triggercontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.control.FloatControlSender;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency switches through a range of frequencies
 * by a dynamicControl that will display as a button
 *
 * Each time the button is pressed, the trigger event occurs and the frequency changes
 *
 */
public class TriggerControlSample implements HBAction {

    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    /**********************************************
     We need to make our counter a class variable so
     it can be accessed within the message handler
     ***********************************************/
    // Now create an index counter to select a frequency
    int counter = 0;

    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        // Make an array of frequencies to switch between
        float frequencyList [] = {500, 1000, 1500, 2000};


        WaveModule player = new WaveModule();
        player.setFequency(frequencyList[0]);
        player.connectTo(hb.ac.out);



        /* Simply type floatControlSender to generate this code */
        FloatControl frequencyDisplay = new FloatControlSender(this, "Current Frequency", frequencyList[0]);


        // Now add a dynamicControl to switch the frequency

        /* Type triggerControl to generate this code */
        TriggerControl triggerControl = new TriggerControl(this, "Change Frequency") {
            @Override
            public void triggerEvent() {/* Write your DynamicControl code below this line */
                // get our next frequency
                counter++;
                float freq = frequencyList[counter % frequencyList.length];

                player.setFequency(freq);
                // now display that value
                frequencyDisplay.setValue(freq);

                /* Write your DynamicControl code above this line */
            }
        };/* End DynamicControl triggerControl code */

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
