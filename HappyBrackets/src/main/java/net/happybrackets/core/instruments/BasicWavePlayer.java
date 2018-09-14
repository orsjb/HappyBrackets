package net.happybrackets.core.instruments;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.device.HB;

/**
 * An encapsulated class for generating a basic wave player
 */
public class BasicWavePlayer extends BasicInstrument{

    UGen frequencyControl;
    WavePlayer waveformGenerator;

    final static float DEFAULT_GAIN = 0.1f;
    final static float DEFAULT_FREQ = 440;
    final static Buffer DEFAULT_BUFFER = Buffer.SINE;


    /**
     * Get the gain Control Object
     * @return the object that controls the frequency
     */
    public UGen getFrequencyControl() {
        return frequencyControl;
    }


    /**
     * Create a Wavelplayer with default parameters of 440hz, 0.1 gain with a sine wave buffer
     */
    public BasicWavePlayer(){
        this(DEFAULT_FREQ, DEFAULT_GAIN, DEFAULT_BUFFER);
    }

    /**
     * Create a basic waveplayer for generating simple wave playback
     * @param frequency the initial frequency
     * @param gain the initial volume. A value of 0.1 is a good value
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public BasicWavePlayer(double frequency, double gain, Buffer waveform_type){
        this(new Glide((float)frequency), new Glide((float)gain), waveform_type);
    }

    /**
     * Create a basic waveplayer for generating simple wave playback
     * @param frequency_control the Object that will control the frequency
     * @param gain the initial volume. A value of 0.1 is a good value
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public BasicWavePlayer(UGen frequency_control, double gain, Buffer waveform_type){
        this(frequency_control, new Glide((float)gain), waveform_type);
    }

    /**
     * Create a basic waveplayer for generating simple wave playback
     * @param frequency the initial frequency
     * @param gain_control the Object that will control the gain
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public BasicWavePlayer(double frequency, UGen gain_control, Buffer waveform_type){
        this(new Glide((float)frequency), gain_control, waveform_type);
    }


    /**
     * Create a basic waveplayer for generating simple wave playback
     * @param frequency_control the Object that will control the frequency
     * @param gain_control the Object that will control the gain
     * @param waveform_type the type of waveform to use, eg, sine, square, etc...
     */
    public BasicWavePlayer(UGen frequency_control, UGen gain_control, Buffer waveform_type){
        super(gain_control);

        frequencyControl = frequency_control;

        // create a wave player to generate a waveform based on frequency and waveform type
        waveformGenerator = new WavePlayer(frequencyControl, waveform_type);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(waveformGenerator);

    }



    /**
     * Set the gain to a new value
     * @param new_gain the new gain to set this player to
     * @return this
     */
    public BasicWavePlayer setGain(double new_gain){
        setGainValue(new_gain);
        return this;
    }

    /**
     * Set an object to control the gain of this waveplayer
     * @param gain_control the new Object that will control the gain
     * @return this
     */
    public BasicWavePlayer setGain(UGen gain_control){
        // assign our new gain to amplifier
        setGainObject(gain_control);
        return this;
    }



    /**
     * set an object to control the frequency of the waveplayery
     * @param new_frequency_control the new object that will control the frequency
     * @return this
     */
    public BasicWavePlayer setFequency(UGen new_frequency_control){
        // assign new frequency object to wave player
        waveformGenerator.setFrequency(new_frequency_control);
        // now assign new one
        frequencyControl = new_frequency_control;

        return this;
    }

    /**
     * set the frequency of the waveplayer to this frequency
     * @param new_frequency the new frequency
     * @return this
     */
    public BasicWavePlayer setFequency(double new_frequency){
        frequencyControl.setValue((float)new_frequency);

        return this;
    }

    /**
     * Set the frequency based on Midi Note Number
     * @param midi_note_number the Midi Note number
     * @return this
     */
    public BasicWavePlayer setMidiFequency(int midi_note_number){
        return setFequency(Pitch.mtof(midi_note_number));
    }

    /**
     * set the new Buffer type for waveplayer
     * @param new_buffer the new buffer type that will be played
     * @return this
     */
    public BasicWavePlayer setBuffer(Buffer new_buffer){
        // assign new frequency object to wave player
        waveformGenerator.setBuffer(new_buffer);

        return this;
    }


}
