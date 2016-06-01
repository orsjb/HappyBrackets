package net.happybrackets.compositions;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.DynamoAction;
import net.happybrackets.device.dynamic.HB;
import net.happybrackets.device.network.NetworkCommunication;

/**
 * Created by Ollie on 18/08/15.
 */
public class ATest implements DynamoAction {


    @Override
    public void action(final HB d) {
        System.out.println("Hello world");


        d.communication.addListener(new NetworkCommunication.Listener() {
            @Override
            public void msg(OSCMessage msg) {
                if (msg.getName().equals("on")) {


                    Envelope e = new Envelope(d.ac, 100);
                    d.sound(new WavePlayer(d.ac, e, Buffer.SINE));
                    e.addSegment(500, 10000);
                }
            }
        });
    }
}
