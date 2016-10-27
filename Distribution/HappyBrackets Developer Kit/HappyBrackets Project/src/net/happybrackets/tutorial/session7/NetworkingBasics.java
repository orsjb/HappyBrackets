package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

/**
 * Created by samferguson on 27/07/2016.
 */
public class NetworkingBasics implements HBAction {

    float globalBaseRate = 1;
    float guitarBaseRate = 1;

    @Override
    public void action(HB hb) {

        hb.resetLeaveSounding();

        hb.addControllerListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                if(oscMessage.getName().toLowerCase().equals("/guitar_pluck")) {

                    float thisRate = 1;
                    float gain = 1;

                    for(int i = 0; i < oscMessage.getArgCount(); i++) {
                        String argContent = (String)oscMessage.getArg(i);
                        String[] argElements = argContent.split("=");
                        if(argElements[0].equals("rate")) {
                            thisRate = Float.parseFloat(argElements[1]);
                        } else if(argElements[0].equals("gain")) {
                            gain = Float.parseFloat(argElements[1]);
                        }
                    }

//                    if(oscMessage.getArgCount() > 0) {
//                        thisRate = hb.getFloatArg(oscMessage, 0);
//                    }
//                    if(oscMessage.getArgCount() > 1) {
//                        gain = hb.getFloatArg(oscMessage, 1);
//                    }

                    SamplePlayer sp = new SamplePlayer(hb.ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav"));
                    sp.getRateUGen().setValue(globalBaseRate * guitarBaseRate * thisRate);
                    Gain g = new Gain(hb.ac, 1, gain);
                    g.addInput(sp);
                    sp.setKillListener(new KillTrigger(g));
                    hb.sound(g);


                } else if(oscMessage.getName().equals("/guitar/base_rate")) {
                    String[] messageElements = oscMessage.getName().split("/");
                    hb.setStatus(messageElements[2]);
                    if(oscMessage.getArgCount() > 0) {
                        guitarBaseRate = hb.getFloatArg(oscMessage, 0);
                    } else {
                        guitarBaseRate = 1;
                    }
                } else if(oscMessage.getName().equals("/base_rate")) {
                    if(oscMessage.getArgCount() > 0) {
                        globalBaseRate = hb.getFloatArg(oscMessage, 0);
                    } else {
                        globalBaseRate = 1;
                    }
                }
            }
        });
    }

}
