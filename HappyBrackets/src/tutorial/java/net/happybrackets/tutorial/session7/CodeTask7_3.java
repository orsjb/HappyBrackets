package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.network.NetworkCommunication;

/**
 * Created by ollie on 24/06/2016.
 *
 * TODO network broadcast
 *
 */
public class CodeTask7_3 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
         //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        //create a listener that listens to messages from the controller
        hb.controller.addListener(new NetworkCommunication.Listener() {
            @Override
            public void msg(OSCMessage msg) {
                if(msg.getName().equals("/play")) {
                    float speed = 1;
                    if(msg.getArgCount() > 0) {
                        try {
                            speed = (float)msg.getArg(0);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    SamplePlayer sp = new SamplePlayer(hb.ac, s);
                    sp.getRateUGen().setValue(speed);
                    hb.sound(sp);
                }
            }
        });
        //set up a clock that will broadcast network messages
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 64 == 0) {
                    //play a burst of noise
                    Noise n = new Noise(hb.ac);
                    Envelope e = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    g.addInput(n);
                    hb.sound(g);
                    e.addSegment(0, 100, new KillTrigger(g));
                    //send message
                    hb.broadcast.broadcast("/ping");
                }
            }
        });

    }

}
