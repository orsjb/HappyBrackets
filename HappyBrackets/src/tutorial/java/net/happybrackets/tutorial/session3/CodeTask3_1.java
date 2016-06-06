package net.happybrackets.tutorial.session3;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;

/**
 * Created by ollie on 6/06/2016.
 *
 * Study the code below.
 *
 * Why doesn't the SamplePlayer need to be destroyed, as was the case with Noise and WavePlayer in previous examples?
 * Why doesn't the sample data get read every time the sound is played?
 *
 * Tasks:
 * 1) Loop the SamplePlayer so that you get an alternating (backwards-forwards) loop over the last 25% of the file. Now that you are looping the SamplePlayer, apply the ADSR envelope from the code tasks in the previous session, including killing the sound.
 * 2) Use the 'setPitch()' function on the SamplePlayer to play the same random pentatonic pattern as in the previous example. Note that the desired frequency is not the same as the playback rate. A playback rate of 1 will play the sound at its original frequency.
 * 3) Replace the fixed playback rate of the SamplePlayer with an Envelope, and randomly add a downward pitch bend to 1 in 5 notes.
 * 4) Switch the SamplePlayer to a GranularSamplePlayer.
 *
 */
public class CodeTask3_1 {

    public static void main(String[] args) {
        AudioContext ac = new AudioContext();
        ac.start();
        //clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (c.getCount() % 32 == 0) {
                    ac.out.addInput(new SamplePlayer(ac, SampleManager.sample("data/audio/guit.wav")));
                }
            }
        });
    }

}
