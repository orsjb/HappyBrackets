package net.happybrackets.assignment_tasks.session5;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by ollie on 24/06/2016.
 *
 * In this example, we load some of the audio that is preloaded on the Pi, the same set of samples found in previous tutorials.
 *
 * Note that the first time you send this to the Pi it will load the samples, resulting in considerable overhead, with possible glitch and delay. However, when you send the code again, the SampleManager knows not to attempt to reload the audio, which is already stored in memory.
 *
 */
public class CodeTask5_2 implements HBAction {

    @Override
    public void action(HB hb) {

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        //play a new random sound from the group
        Sample s = SampleManager.randomFromGroup("Guitar");
        hb.sound(new SamplePlayer(hb.ac, s));

    }

}
