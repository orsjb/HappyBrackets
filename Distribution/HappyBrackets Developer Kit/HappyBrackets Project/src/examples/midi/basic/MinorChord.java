package examples.midi.basic;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
/**
 * This sketch will demonstrate converting a MIDI note number to a frequency for playback as a square wave
 * Additionally, the MIDI numbers for the minor third and perfect fifth are obtained using the Pitch.minor array
 * Each of these values are converted to a frequency and used as inputs to three wavePlayers
 * All three wavePlayers are connected to the input of the Gain object
 */
public class MinorChord implements HBAction {
    final int NUMBER_AUDIO_CHANNELS = 1; // define how many audio channels our device is using
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final int MIDI_NOTE =  60; // This is MIDI C3

        // convert our MIDI note to a frequency with Midi to frequency function: mtof
        float tonicFrequency = Pitch.mtof(MIDI_NOTE);
        
        final float INITIAL_VOLUME = 0.1f; // define how loud we want the sound
        Glide audioVolume = new Glide(INITIAL_VOLUME);

        // create a wave player to generate a waveform based on frequency and waveform type
        WavePlayer tonicWaveform = new WavePlayer(tonicFrequency, Buffer.SQUARE);

        // create a second wavePlayer for third
        final int MINOR_THIRD = Pitch.minor[2]; // Indexes are zero based, so a third is index 2
        float thirdFrequency =  Pitch.mtof(MIDI_NOTE + MINOR_THIRD);
        WavePlayer thirdWaveform = new WavePlayer(thirdFrequency, Buffer.SQUARE);

        // create a third wavePlayer for perfect fifth
        final int PERFECT_FIFTH = Pitch.minor[4]; // Indexes are zero based, so a fifth is index 4
        float fifthFrequency =  Pitch.mtof(MIDI_NOTE + PERFECT_FIFTH);
        WavePlayer fifthWaveform = new WavePlayer(fifthFrequency, Buffer.SQUARE);



        // set up a gain amplifier to control the volume
        Gain gainAmplifier = new Gain(NUMBER_AUDIO_CHANNELS, audioVolume);

        // connect our WavePlayer object into the Gain object
        gainAmplifier.addInput(tonicWaveform);
        gainAmplifier.addInput(thirdWaveform);
        gainAmplifier.addInput(fifthWaveform);

        // Now plug the gain object into the audio output
        hb.ac.out.addInput(gainAmplifier);

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
