package compositions;

import core.PIPO;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;
import pi.dynamic.DynamoPI;
import pi.network.NetworkCommunication;

/**
 * Created by Ollie on 18/08/15.
 */
public class ATest implements PIPO {


    @Override
    public void action(final DynamoPI d) {
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
