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
import net.beadsproject.beads.core.Bead;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.net.SocketAddress;

/**
 * In this example the device both broadcasts messages off its clock, and also prints out any broadcast messages.
 * You need to be looking at the console output in order to see this printout. You can do that by either running this in
 * the terminal with output when logged into the device, or looking at the output in the IntelliJ plugin.
 */
public class Example7_1 implements HBAction {

    @Override
    public void action(HB hb) {

        hb.reset();

        hb.addBroadcastListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                if (!oscMessage.getName().startsWith("/device")) {
                    hb.setStatus("Message received: " + oscMessage.getName() + " myindex = " + hb.myIndex());
                }
            }
        });
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (hb.clock.getCount() % 32 == 0) {
                    hb.broadcast.broadcast("/test" + hb.myIndex());
                }
            }
        });
    }

}
