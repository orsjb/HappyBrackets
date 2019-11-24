package net.happybrackets.core.instruments;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.DataBeadReceiver;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.device.HB;

/**
 * Class for building other basic instruments from.
 * Provides an encapsulation of {@link Gain} to allow treatment of a sound instrument as
 * a whole unit instead of the parts that create it. See {@link WaveModule} and {@link SampleModule} for examples.
 * <br>
 * The gain can be set to a {@link UGen} object via {@link #setGainObject(UGen)} or to a specific value
 * via {@link #setGainValue(double)}
 *
 */
abstract class BasicInstrument extends Bead {
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
     * Get the object we need to connect our kill trigger to.
     * For example, if we wanted to kill a {@link WaveModule} instrument and the end of an envelope
     * we might code the function as follows:
     * <br>
     * <pre>
     *   Envelope gainEnvelope = new Envelope(0);
     *
     *   WaveModule player = new WaveModule();
     *   player.setGain(gainEnvelope);
     *   // Now plug the  object into the audio output
     *   player.connectTo(HB.getAudioOutput());

     *   // first add a segment to progress to the higher volume
     *   gainEnvelope.addSegment(0.1f, 100);
     *
     *   // now add a segment to make the gainEnvelope stay at that volume
     *   // we do this by setting the start of the segment to the value as our MAX_VOLUME
     *   gainEnvelope.addSegment(0.1f, 1000);
     *
     *   //Now make our gain fade out to 0 and then kill it
     *   gainEnvelope.addSegment(0, 10, new KillTrigger(player));
     *</pre>
     * @deprecated use the class instead. Eg, instead of
     * <pre>    new KillTrigger(<b>player.getKillTrigger()</b></pre>
     * use
     * <pre>    new KillTrigger(waveModule)</pre>
     * @return the object to send our kill trigger to
     */
    public UGen getKillTrigger(){
        return gainAmplifier;
    }

    /**
     * Set an object to control the gain of this instrument
     * For example, the set the Gain of a {@link WaveModule} so it follows an {@link net.beadsproject.beads.ugens.Envelope} we would do as follows
     * <pre>
     * Envelope gainEnvelope = new Envelope(0);
     *
     * WaveModule player = new WaveModule();
     * <b>player.setGain(gainEnvelope);</b>
     * </pre>
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
     * Kill the current control and release resources
     * Enables the instrument to be added to a kill trigger
     */
    public void kill() {
        if (gainAmplifier != null) {
            gainAmplifier.kill();
        }
    }

    /**
     * Connect the output of this instrument to the input of another device
     * @param input_device the device we want to connect it to
     */
    protected void connectToDevice(UGen input_device){
        gainAmplifier.connectTo(input_device);
    }
}
