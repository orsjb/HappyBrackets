package examples.midi.basic;

import net.beadsproject.beads.data.Buffer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * This sketch will demonstrate converting a MIDI note number to a frequency for playback as a square wave
 * It uses function Pitch.mtof, which converts a Midi note number to a frequency
 */
public class SimpleMIDI implements HBAction {
    
    @Override
    public void action(HB hb) {

        // remove this code if you do not want other compositions to run at the same time as this one
        hb.reset();
        hb.setStatus(this.getClass().getSimpleName() + " Loaded");

        final int MIDI_NOTE =  60; // This is MIDI C3

        WaveModule waveModule = new WaveModule();

        waveModule.setMidiFrequency(MIDI_NOTE);
        waveModule.setBuffer(Buffer.SQUARE);
        waveModule.connectTo(HB.getAudioOutput());



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
