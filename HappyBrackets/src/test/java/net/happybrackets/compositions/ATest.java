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

package net.happybrackets.compositions;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCVocabulary;
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
                if (OSCVocabulary.match(msg, "on")) {
                    Envelope e = new Envelope(d.ac, 100);
                    d.sound(new WavePlayer(d.ac, e, Buffer.SINE));
                    e.addSegment(500, 10000);
                }
            }
        });
    }
}
