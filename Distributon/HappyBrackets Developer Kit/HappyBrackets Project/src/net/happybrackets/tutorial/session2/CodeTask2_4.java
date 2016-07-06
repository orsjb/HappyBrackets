package net.happybrackets.tutorial.session2;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

/**
 * Created by ollie on 5/06/2016.
 *
 * The following code plays a regular noise burst through a simple delay.
 *
 * 1) Identify what number below indicates the delay time of the delay and speed up the delay.
 * 2) Make the delay feedback on itself by connecting two UGens together in a single line of code, and identify which number is responsible for the delay feedback level.
 * 3) Now transform this delay into a ping-pong delay, in which the sound 'ping-pongs' from the left channel to the right channel and back again, with feedback as above. The time it takes to get from left to right is controlled separately from the time from right to left. Set your ping-pong delay so that the left channel echo comes 125ms after the original sound, and then the right channel echo comes 250ms after that, followed again by an attenuated delay in the left channel 125ms later, and so on.
 *
 *
 */
public class CodeTask2_4 {

    public static void main(String[] args) {
        //audio stuff
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        //create a delay line
        TapIn tin = new TapIn(ac, 10000);
        Envelope delayTime = new Envelope(ac, 333);
        TapOut tout = new TapOut(ac, tin, delayTime);
        Gain delayGain = new Gain(ac, 1, 0.5f);
        delayGain.addInput(tout);
        ac.out.addInput(delayGain);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(c.isBeat()) {
                    Noise n = new Noise(ac);
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(n);
                    ac.out.addInput(g);
                    tin.addInput(g);
                }
            }
        });
    }
}
