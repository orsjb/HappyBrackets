package net.happybrackets.examples;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * Plays Middle C
 */
public class PitchGeneration implements HBAction {
    @Override
    public void action(HB hb) {

        final int NOTE_SPACE = 300;

        int midi_number = 60;
        // convert our MIDI not number to frequency
        float freq = Pitch.mtof(midi_number);

        WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SQUARE);
        Envelope e = new Envelope(hb.ac, 0.1f);
        Gain g = new Gain(hb.ac, 1, e);

        e.addSegment(0.1f, NOTE_SPACE, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // play major third Higher
                int major_third = midi_number + Pitch.major[2];
                WavePlayer wp = new WavePlayer(hb.ac, Pitch.mtof(major_third), Buffer.SQUARE);
                e.addSegment(0.1f, NOTE_SPACE);
                g.addInput(wp);

                e.addSegment(0.1f, NOTE_SPACE, new Bead() {
                    @Override
                    protected void messageReceived(Bead bead) {
                        // play major third Higher
                        int major_fifth = midi_number + Pitch.major[4];
                        WavePlayer wp = new WavePlayer(hb.ac, Pitch.mtof(major_fifth), Buffer.SQUARE);
                        //e.addSegment(0.1f, NOTE_SPACE);
                        g.addInput(wp);
                    }
                });
            }
        });

        g.addInput(wp);

        hb.ac.out.addInput(g);


    }

    /**
     * This function is used when running sketch in IntelliJ for debugging or testing
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
