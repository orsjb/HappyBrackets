package net.happybrackets.tutorial.session5;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by ollie on 22/06/2016.
 *
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 * Ensure you have a device set up and connected. It should be visible in the devices list in the HappyBrackets plugin.
 * From the dropdown menu in the plugin select HappyBracketsHelloWorld and click send.
 *
 */
public class HappyBracketsHelloWorld implements HBAction {

    @Override
    public void action(HB hb) {
        WavePlayer wp = new WavePlayer(hb.ac, 1000, Buffer.SINE);
        Envelope e = new Envelope(hb.ac, 0.1f);
        Gain g = new Gain(hb.ac, 1, e);
        e.addSegment(0, 5000, new KillTrigger(g));
        g.addInput(wp);
        hb.sound(g);
    }

}
