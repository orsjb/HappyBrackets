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

package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

/**
 * In this example, we play a random sound whenever the command "/play" is received from the controller.
 */
public class Example7_2 implements HBAction {

    @Override
    public void action(HB hb) {
        hb.reset();
        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");
        hb.addControllerListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if(msg.getName().equals("/play")) {
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    hb.sound(new SamplePlayer(hb.ac, s));
                }
            }
        });
    }

}
