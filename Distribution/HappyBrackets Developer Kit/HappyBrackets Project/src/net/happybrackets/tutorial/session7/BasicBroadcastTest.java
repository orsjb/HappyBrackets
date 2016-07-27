package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.Bead;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

/**
 * Created by ollie on 26/07/2016.
 */
public class BasicBroadcastTest implements HBAction {

    @Override
    public void action(HB hb) {

        hb.reset();

        hb.addBroadcastListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                System.out.println("Message received: " + oscMessage.getName() + " myindex = " + hb.myIndex());
            }
        });
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 32 == 0) {
                    hb.broadcast.broadcast("/test" + hb.myIndex());
                }
            }
        });
    }

}
