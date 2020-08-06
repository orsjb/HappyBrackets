package examples.controls.globalcontrol;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.core.instruments.WaveModule;
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
 * The name of the device that sent the message will be displayed in the sendingDevice text Control
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
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");


        final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make
        final float MAX_VOLUME = 0.1f; // define how loud we want the sound

        WaveModule waveModule = new WaveModule(INITIAL_FREQUENCY, MAX_VOLUME, Buffer.SINE);
        waveModule.connectTo(HB.getAudioOutput());

        // This will display the sending device
        TextControl sendingDevice = new TextControl(this, "Sending Device", "");


        // Make an array of frequencies to switch between
        float frequencyList [] = {500, 1000, 1500, 2000};


        /* Type globalFloatControl to generate this code */
        FloatControl globalFrequencyControl = new FloatControl(this, "global frequency control", INITIAL_FREQUENCY) {
            @Override
            public void valueChanged(double control_val) { /* Write your DynamicControl code below this line */
                // this value has been received either from the trigger below
                // or over the network
                waveModule.setFrequency(control_val);

                // now display the sending device
                sendingDevice.setValue(getSendingDevice());
                /* Write your DynamicControl code above this line */
            }
        }.setDisplayRange(0, INITIAL_FREQUENCY * 3, DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY);
        globalFrequencyControl.setControlScope(ControlScope.GLOBAL);
        /* End DynamicControl globalFrequencyControl code */


        /* Type globalBooleanControl to generate this code */
        BooleanControl globalOnOff = new BooleanControl(this, "On / Off", true) {
            @Override
            public void valueChanged(Boolean control_val) { /* Write your DynamicControl code below this line */
                if (control_val){
                    waveModule.setGain(MAX_VOLUME);
                }
                else {
                    waveModule.setGain(0);
                }
                // now display the sending device
                sendingDevice.setValue(getSendingDevice());

                /* Write your DynamicControl code above this line */
            }
        };
        globalOnOff.setControlScope(ControlScope.GLOBAL);
        /* End DynamicControl globalOnOff code */


        // Now add a dynamicControl to switch the frequency
        // Note that this is not a global control, however, it sets the
        // value of a global control

        /* Type triggerControl to generate this code */
        TriggerControl triggerControl = new TriggerControl(this, "Change Frequency") {
            @Override
            public void triggerEvent() {  /* Write your DynamicControl code below this line */
                // get our next frequency
                float freq = frequencyList[counter % frequencyList.length];

                // send our new value to the global DynamicControl
                globalFrequencyControl.setValue(freq);
                counter++;
                /*** Write your DynamicControl code above this line ***/
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
