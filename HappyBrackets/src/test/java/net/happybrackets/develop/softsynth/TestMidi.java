package net.happybrackets.develop.softsynth;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;

import javax.sound.midi.*;
import java.lang.invoke.MethodHandles;

public class TestMidi implements HBAction, HBReset {
    // Change to the number of audio Channels on your device
    final int NUMBER_AUDIO_CHANNELS = 1;

    @Override
    public void action(HB hb) {
        /***** Type your HBAction code below this line ******/
        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        try {
            Synthesizer midiSynth = MidiSystem.getSynthesizer();

            midiSynth.open();
            Instrument [] instr = midiSynth.getDefaultSoundbank().getInstruments();
            MidiChannel[] channels = midiSynth.getChannels();

            midiSynth.loadInstrument(instr[0]);
            channels[0].noteOn(60, 100);

            try {
                Thread.sleep( 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            channels[0].noteOff(60);

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }


        /***** Type your HBAction code above this line ******/
    }


    /**
     * Add any code you need to have occur when a reset occurs
     */
    @Override
    public void doReset() {
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
