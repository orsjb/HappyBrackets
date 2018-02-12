package examples.controls.globalcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch creates a simple sine wave whose frequency switches through a range of frequencies
 * by a dynamicControl that will display as a button
 *
 * Each time the button is pressed, the trigger event occurs which sets the value of the global control
 * The global control then sends its value to all listeners on the network
 *
 * Additionally, changing the DynamicControl via GUI will also send global value across network
 *
 * Run this on two different devices
 */
public class GlobalControl implements HBAction {
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

        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        Glide waveformFrequency = new Glide(hb.ac, INITIAL_FREQUENCY);
        Glide gainVolume = new Glide(hb.ac, MAX_VOLUME);

        final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer waveformGenerator = new WavePlayer(hb.ac, waveformFrequency, Buffer.SINE);

        // set up a gain amplifier to control the volume. We are using the glide object to control this value
        Gain gainAmplifier = new Gain(hb.ac, NUMBER_AUDIO_CHANNELS, gainVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);


        // Make an array of frequencies to switch between
        float frequencyList [] = {500, 1000, 1500, 2000};




        /*************************************************************
         * Create a Float type buddy Dynamic Control
         *
         * Simply type globalFloatControl to generate this code
         *************************************************************/
        DynamicControl globalFrequencyControl = hb.createControlBuddyPair(this, ControlType.FLOAT, "global frequency control", INITIAL_FREQUENCY, 0, INITIAL_FREQUENCY * 3)
                .setControlScope(ControlScope.GLOBAL)
                .addControlListener(control -> {
                    float control_val = (float) control.getValue();

                    /*** Write your DynamicControl code below this line ***/
                    // this value has been received either from thr trigger below
                    // or over the network
                    waveformFrequency.setValue(control_val);
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/


        /*************************************************************
         * Create a Boolean type Dynamic Control pair that displays as a check box
         *
         * Simply type globalBooleanControl to generate this code
         *************************************************************/
        DynamicControl globalOnOff = hb.createDynamicControl(this, ControlType.BOOLEAN, "On / Off", true)
                .setControlScope(ControlScope.GLOBAL)
                .addControlListener(control -> {
                    boolean control_val = (boolean) control.getValue();

                    /*** Write your DynamicControl code below this line ***/
                    if (control_val){
                        gainVolume.setValue(MAX_VOLUME);
                    }
                    else {
                        gainVolume.setValue(0);
                    }
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/

        // Now add a dynamicControl to switch the frequency
        // Note that this is not a global control, however, it sets the
        // value of a global control

        /*************************************************************
         * Create a Trigger type Dynamic Control that displays as a button
         *
         * Simply type triggerControl to generate this code
         *************************************************************/
        hb.createDynamicControl(this, ControlType.TRIGGER, "Change Frequency")
                .addControlListener(control -> {

                    /*** Write your DynamicControl code below this line ***/

                    // get our next frequency
                    float freq = frequencyList[counter % frequencyList.length];

                    // send our new value to the global DynamicControl
                    globalFrequencyControl.setValue(freq);
                    counter++;
                    /*** Write your DynamicControl code above this line ***/
                });
        /*** End DynamicControl code ***/


    }

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
}
