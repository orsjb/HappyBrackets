package net.happybrackets.core.instruments;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * An encapsulated class for generating a basic wave player with functions to set
 * the frequency, gain, and {@link Buffer} type.
 <pre>
 WaveModule player = new WaveModule();
 player.setFrequency(440);
 player.setGain(0.1f);
 player.setBuffer(Buffer.SQUARE);
 player.connectTo(HB.getAudioOutput());
 </pre>

 The WaveModule can be killed using the standad {@link Bead#kill()} function. For example, to kill the
 player at the end of an envelope, one would add the WaveModule to the {@link net.beadsproject.beads.events.KillTrigger}
 For example:
<pre>
 *   Envelope gainEnvelope = new Envelope(0);
 *
 *   WaveModule player = new WaveModule();
 *   player.setGain(gainEnvelope);
 *   // Now plug the  object into the audio output
 *   player.connectTo(HB.getAudioOutput());

 *   // first add a segment to progress to the higher volume
 *   gainEnvelope.addSegment(0.1f, 100);
 *
 *   //Now make our gain fade out to 0 and then kill it
 *   gainEnvelope.addSegment(0, 10, <b>new KillTrigger(player))</b>;
 </pre>
 <br> The gain can be set to a {@link UGen} object via {@link #setGainObject(UGen)} or to a specific value
 * via {@link #setGainValue(double)}
 <br>
 <br> The frequency control can be set to a {@link UGen} object via {@link #setFrequency(UGen)}, a specific value
 * via {@link #setFrequency(double)} (double)}, or to a converted Midi value via {@link #setMidiFrequency(int)}
 <br> The type of waveform {@link Buffer} is set via {@link #setBuffer(Buffer)}. The waveform can be paused through {@link #pause(boolean)}
 */
public class WaveModule extends BasicInstrument{

    UGen frequencyControl;
    WavePlayer waveformGenerator;

    final static float DEFAULT_GAIN = 0.1f;
    final static float DEFAULT_FREQ = 440;
    final static Buffer DEFAULT_BUFFER = Buffer.SINE;


    /**
     * Get the Frequency Control {@link UGen}
     * @return the object that controls the frequency
     */
    public UGen getFrequencyControl() {
        return frequencyControl;
    }


    /**
     * Create a {@link WaveModule} with default parameters of 440hz, 0.1 gain with a sine wave buffer
     */
    public WaveModule(){
        this(DEFAULT_FREQ, DEFAULT_GAIN, DEFAULT_BUFFER);
    }

    /**
     * Create a basic {@link WaveModule} for generating simple wave playback
     * @param frequency the initial frequency
     * @param gain the initial volume. A value of 0.1 is a typical value for playback
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public WaveModule(double frequency, double gain, Buffer waveform_type){
        this(new Glide((float)frequency), new Glide((float)gain), waveform_type);
    }

    /**
     * Create a basic {@link WaveModule} for generating simple wave playback
     * @param frequency_control the {@link UGen} Object that will control the frequency
     * @param gain the initial volume. A value of 0.1 is a typical value for playback
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public WaveModule(UGen frequency_control, double gain, Buffer waveform_type){
        this(frequency_control, new Glide((float)gain), waveform_type);
    }

    /**
     * Create a basic {@link WaveModule} for generating simple wave playback
     * @param frequency the initial frequency
     * @param gain_control the Object that will control the gain
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public WaveModule(double frequency, UGen gain_control, Buffer waveform_type){
        this(new Glide((float)frequency), gain_control, waveform_type);
    }


    /**
     * Create a basic {@link WaveModule} for generating simple wave playback
     * @param frequency_control the {@link UGen} Object that will control the frequency
     * @param gain_control the {@link UGen}  Object that will control the gain
     * @param waveform_type the type of {@link Buffer} to use, eg, sine, square, etc...
     */
    public WaveModule(UGen frequency_control, UGen gain_control, Buffer waveform_type){
        super(gain_control);

        frequencyControl = frequency_control;

        // create a wave player to generate a waveform based on frequency and waveform type
        waveformGenerator = new WavePlayer(frequencyControl, waveform_type);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

    }


    /**
     * Pause the waveform playback. More details at {@link WavePlayer#pause(boolean)}
     * @param pause set true to pause
     */
    public void pause(boolean pause){
        waveformGenerator.pause(pause);
    }

    /**
     * Test if waveform generator ia paused. More details at {@link WavePlayer#isPaused()}
     * @return true if paused
     */
    public boolean isPaused(){
        return waveformGenerator.isPaused();
    }

    /**
     * Set the gain to a new value
     * Eg
     <pre>
     WaveModule waveModule = new WaveModule();
     waveModule.setGain(0.3);
     </pre>
     * @param new_gain the new gain to set this player to
     * @return this
     */
    public WaveModule setGain(double new_gain){
        setGainValue(new_gain);
        return this;
    }


    /**
     * Get the UGen {@link WavePlayer} object
     * @return the {@link WavePlayer} object
     */
    public WavePlayer getWavePlayer(){
        return waveformGenerator;
    }

    /**
     * Set an object to control the gain of this {@link WaveModule}.
     * For example, to set the gain to follow and {@link net.beadsproject.beads.ugens.Envelope}
     * you could do the following
     <pre>
     Envelope outputEnvelope = new Envelope(0.1f);

     // type basicWavePlayer to generate this code
     WaveModule waveModule = new WaveModule();
     waveModule.setGain(outputEnvelope);
     </pre>
     * @param gain_control the new Object that will control the gain
     * @return this
     */
    public WaveModule setGain(UGen gain_control){
        // assign our new gain to amplifier
        setGainObject(gain_control);
        return this;
    }


    /**
     * set a {@link UGen} to control the frequency of the {@link WavePlayer}.
     * An example of using an {@link net.beadsproject.beads.ugens.Envelope} to control
     * the frequency is as follows:
     <pre>
     final float INITIAL_VOLUME = 0.1f; // Define how loud we want our sound
     // define the different frequencies we will be using in our envelope
     final float LOW_FREQUENCY = 500;   // this is the low frequency of the waveform we will make
     final float HIGH_FREQUENCY = 2000; // This is the high frequency of the waveform we will make

     // define the times it takes to reach the points in our envelope
     final float RAMP_UP_FREQUENCY_TIME = 1000; // 1 second (time is in milliseconds)
     final float HOLD_FREQUENCY_TIME = 3000; // 3 seconds
     final float RAMP_DOWN_FREQUENCY_TIME = 5000; // 5 seconds

     // Create our envelope using LOW_FREQUENCY as the starting value
     Envelope frequencyEnvelope = new Envelope(LOW_FREQUENCY);

     WaveModule player = new WaveModule(frequencyEnvelope, INITIAL_VOLUME, Buffer.SINE);
     player.connectTo(HB.getAudioOutput());


     // Now start changing the frequency of frequencyEnvelope
     // first add a segment to progress to the higher frequency over 5 seconds
     frequencyEnvelope.addSegment(HIGH_FREQUENCY, RAMP_UP_FREQUENCY_TIME);

     // now add a segment to make the frequencyEnvelope stay at that frequency
     // we do this by setting the start of the segment to the value as our HIGH_FREQUENCY
     frequencyEnvelope.addSegment(HIGH_FREQUENCY, HOLD_FREQUENCY_TIME);

     //Now make our frequency go back to the lower frequency
     frequencyEnvelope.addSegment(LOW_FREQUENCY, RAMP_DOWN_FREQUENCY_TIME);

     //Now make our frequency hold to the lower frequency, and after holding, kill our WaveModule
     frequencyEnvelope.addSegment(LOW_FREQUENCY, HOLD_FREQUENCY_TIME, new KillTrigger(player));
     </pre>
     * @param new_frequency_control the new object that will control the frequency
     * @return this
     */
    public WaveModule setFrequency(UGen new_frequency_control){
        // assign new frequency object to wave player
        waveformGenerator.setFrequency(new_frequency_control);
        // now assign new one
        frequencyControl = new_frequency_control;

        return this;
    }


    /**
     * set the frequency of the {@link WavePlayer} to this frequency.
     * For example, to set the frequency to 1KHz, one could do the following:
     <pre>
     WaveModule waveModule = new WaveModule();

     waveModule.setFrequency(1000);
     </pre>
     * @param new_frequency the new frequency
     * @return this
     */
    public WaveModule setFrequency(double new_frequency){
        frequencyControl.setValue((float)new_frequency);

        return this;
    }


    /**
     * Set the frequency based on Midi Note Number. For example, to set frequency to Middle C
     * one could do the following:
     *
     <pre>
     WaveModule waveModule = new WaveModule();

     waveModule.setMidiFrequency(60); // 60 is Middle C

     </pre>
     * @param midi_note_number the Midi Note number
     * @return this
     */
    public WaveModule setMidiFrequency(int midi_note_number){
        return setFrequency(Pitch.mtof(midi_note_number));
    }

    /**
     * set the new Buffer type for waveplayer
     * @param new_buffer the new buffer type that will be played
     * @return this
     */
    public WaveModule setBuffer(Buffer new_buffer){
        // assign new frequency object to wave player
        waveformGenerator.setBuffer(new_buffer);

        return this;
    }

    /**
     * Connect the output of this instrument to the input of another device
     * @param input_device the device we want to connect it to
     * @return this
     */
    public WaveModule connectTo(UGen input_device){
        connectToDevice(input_device);
        return this;
    }

}
