package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.network.NetworkCommunication;

import java.net.SocketAddress;

/**
 * Created by ollie on 24/06/2016.
 *
 *
 */
public class CodeTask7_2 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
         //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        hb.controller.addListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if(msg.getName().equals("/play")) {
                    float speed = 1;
                    if(msg.getArgCount() > 0) {
                        try {
                            speed = (float)msg.getArg(0);
                        } catch(Exception e) {}
                    }
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    SamplePlayer sp = new SamplePlayer(hb.ac, s);
                    sp.getRateUGen().setValue(speed);
                    hb.sound(sp);
                }
            }
        });
    }

}
