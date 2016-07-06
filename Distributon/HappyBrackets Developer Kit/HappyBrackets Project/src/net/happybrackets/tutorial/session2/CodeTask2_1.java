package net.happybrackets.tutorial.session2;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Created by ollie on 5/06/2016.
 *
 * Let's look at Envelopes!
 *
 * Run the following code. Then uncomment the line at the very end (shortcut for comment/uncomment = command-/) and run the code again.
 *
 * Duplicate that last line so that there are two copies of it, one after the other. Then change the 1000 in the second instance to 100. Run the code again. Notice that the segments run one after the other.
 *
 * Now complete the following tasks:
 * 1) Make the frequency rise from 500hz to 1000hz over 1 second, stay there for 1 second, then rise again to 2000hz over half a second.
 * 2) Add the additional argument 'new AudioContextStopTrigger(ac)' to the last segment of your freqEnv and see what happens.
 * 3) Comment out all of your freqEnv addSegment() statements again. Create a new Envelope that will be used to control the Gain object. This should be called 'gainEnv' and should be initialised to 0. In the constructor for Gain, you can replace the value 0.1f by gainEnv.
 * 4) Use your new gainEnv object to create an attack-decay-sustain-release envelope. Make your sound's gain increase from 0 to 0.5f over 50ms, then drop to 0.1f over 50ms, then remain at 0.1f for 1000ms, then decay to 0 over 5 seconds.
 * 5) Add the additional argument 'new KillTrigger(g)' to the last element of your gainEnv. You won't notice any difference because you already faded the volume to zero, but this last line will remove all of the audio processing elements from the signal chain.
 *
 */
public class CodeTask2_1 {

    public static void main(String[] args) {
        //Audio stuff, once again we play a simple sine tone, except this time its frequency is controlled by an Envelope
        AudioContext ac = new AudioContext();
        ac.start();
        //here is the Envelope, initialised to 500.
        Envelope freqEnv = new Envelope(ac, 500);
        //notice that the second argument to WavePlayer is no longer a number, but an object.
        WavePlayer wp = new WavePlayer(ac, freqEnv, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(wp);
        ac.out.addInput(g);
        g.setGain(0.1f);
        //once you've run the above code once, uncomment the following line and run it again
//        freqEnv.addSegment(1000, 4000);
    }
}
