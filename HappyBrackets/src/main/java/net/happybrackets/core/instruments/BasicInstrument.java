package net.happybrackets.core.instruments;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.device.HB;

/**
 * Class for building other basic instruments from
 */
abstract class BasicInstrument {
    UGen gainControl;
    Gain gainAmplifier;

    /**
     * Constructor
     * @param gain_control the object that will control the gain
     */
    BasicInstrument (UGen gain_control){

        int NUMBER_AUDIO_CHANNELS = HB.HBInstance.ac.out.getOuts();

        gainControl = gain_control;
        // set up a gain amplifier to control the volume
        gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, gainControl);
    }

    /**
     * Get the gain Control Object
     * @return the object that controls the gain
     */
    public UGen getGainControl() {
        return gainControl;
    }

    /**
     * Get the final gain Object
     * @return the  gainObject
     */
    public UGen getGainAmplifier() {
        return gainAmplifier;
    }


    /**
     * Get the object we need to connect our kill trigger to
     * @return the object to send our kill trigger to
     */
    public UGen getKillTrigger(){
        return gainAmplifier;
    }

    /**
     * Set an object to control the gain of this instrument
     * @param gain_control the new Object that will control the gain
     */
    protected void setGainObject(UGen gain_control){
        // assign our new gain to amplifier
        gainAmplifier.setGain(gain_control);

        // now store new gain
        gainControl = gain_control;

    }

    /**
     * Set the gain to a new value
     * @param new_gain the new gain to set this nstrument to
     */
    public void setGainValue(double new_gain){
        gainControl.setValue((float) new_gain);

    }

    /**
     * Connect the output of this instrument to the input of another device
     * @param input_device the device we want to connect it to
     */
    protected void connectToDevice(UGen input_device){
        gainAmplifier.connectTo(input_device);
    }
}
