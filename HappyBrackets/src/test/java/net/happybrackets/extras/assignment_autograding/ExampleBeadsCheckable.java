package net.happybrackets.extras.assignment_autograding;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Created by ollie on 11/08/2016.
 */
public class ExampleBeadsCheckable implements BeadsChecker.BeadsCheckable {

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... args) {

        //create a wave player
        WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
        ac.out.addInput(wp);

        //write something to the text buffer
        buf.append("Hello World");

    }

}
