package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.Bead;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

/**
 * Created by samferguson on 27/07/2016.
 */
public class NetworkingBasics2 implements HBAction {

    @Override
    public void action(HB hb) {

        hb.resetLeaveSounding();

//        hb.sendToController("/hello", 5, 0.7f, "yes", "no");
        int id = hb.myIndex();

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 16 == 0) {
                    hb.broadcast("/tick", id, hb.clock.getCount());
                }
            }
        });


        hb.addBroadcastListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                if((int)oscMessage.getArg(0) == id) {

                } else {

                }
                hb.setStatus("Received message: " + oscMessage.getName() + " " + oscMessage.getArg(0) + " " + oscMessage.getArg(1));
            }
        });



    }
}
