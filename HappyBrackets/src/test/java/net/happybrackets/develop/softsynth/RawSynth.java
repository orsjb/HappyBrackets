package net.happybrackets.develop.softsynth;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;

import javax.sound.midi.*;
import java.lang.invoke.MethodHandles;

// You can run this from classes folder using java -cp . net.happybrackets.develop.softsynth.RawSynth
public class RawSynth {
    // Change to the number of audio Channels on your device

    static public void runProgram() {

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



    //<editor-fold defaultstate="collapsed" desc="Debug Start">

    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            runProgram();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>
}
