package net.happybrackets.compositions;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.network.NetworkCommunication;

import java.net.SocketAddress;

/**
 * Created by Ollie on 18/08/15.
 */
public class ATest implements HBAction {


    @Override
    public void action(final HB d) {
        System.out.println("Hello world");


        d.controller.addListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if (msg.getName().equals("on")) {
                    Envelope e = new Envelope(d.ac, 100);
                    d.sound(new WavePlayer(d.ac, e, Buffer.SINE));
                    e.addSegment(500, 10000);
                }
            }
        });
    }
}
