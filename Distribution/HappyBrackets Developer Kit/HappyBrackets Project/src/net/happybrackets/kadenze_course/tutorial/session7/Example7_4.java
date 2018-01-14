/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.kadenze_course.tutorial.session7;

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
 * In this example, a listener is set up to listen to commands from the controller.
 * Messages:
 *
 * /guitar_pluck <key=float_value>...
 * keys include rate and gain.
 *
 * /guitar/base_rate <float:rate>
 * /base_rate <float:rate>
 */
public class Example7_4 implements HBAction {

    float globalBaseRate = 1;
    float guitarBaseRate = 1;

    @Override
    public void action(HB hb) {

        hb.resetLeaveSounding();

        hb.addControllerListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                if (oscMessage.getName().toLowerCase().equals("/guitar_pluck")) {
                    float thisRate = 1;
                    float gain = 1;
                    for (int i = 0; i < oscMessage.getArgCount(); i++) {
                        String argContent = (String) oscMessage.getArg(i);
                        String[] argElements = argContent.split("=");
                        if (argElements[0].equals("rate")) {
                            thisRate = Float.parseFloat(argElements[1]);
                        } else if (argElements[0].equals("gain")) {
                            gain = Float.parseFloat(argElements[1]);
                        }
                    }
                    SamplePlayer sp = new SamplePlayer(hb.ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav"));
                    sp.getRateUGen().setValue(globalBaseRate * guitarBaseRate * thisRate);
                    Gain g = new Gain(hb.ac, 1, gain);
                    g.addInput(sp);
                    sp.setKillListener(new KillTrigger(g));
                    hb.ac.out.addInput(g);
                } else if (oscMessage.getName().equals("/guitar/base_rate")) {
                    String[] messageElements = oscMessage.getName().split("/");
                    hb.setStatus(messageElements[2]);
                    if (oscMessage.getArgCount() > 0) {
                        guitarBaseRate = hb.getFloatArg(oscMessage, 0);
                    } else {
                        guitarBaseRate = 1;
                    }
                } else if (oscMessage.getName().equals("/base_rate")) {
                    if (oscMessage.getArgCount() > 0) {
                        globalBaseRate = hb.getFloatArg(oscMessage, 0);
                    } else {
                        globalBaseRate = 1;
                    }
                }
            }
        });
    }

}
