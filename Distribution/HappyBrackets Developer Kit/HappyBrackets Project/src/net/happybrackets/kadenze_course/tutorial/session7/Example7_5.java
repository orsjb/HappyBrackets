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
import de.sciss.net.OSCServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * This example is completely standalone (doesn't use Beads or HappyBrackets, but it does use the NetUtil library).
 * It creates one network listener, listening on port 6666, and then it sets up a thread to send messages on port 5555,
 * once per second.
 * The destination is "localhost", but you can change it to the IP address or hostname of another computer.
 * Try running this twice on two computers. You will need to adjust the send and receive ports on one machine, and also
 * the destination on both machines, so that they speak to each other.
 */
public class Example7_5 {

    public final static int SEND_PORT = 5555;
    public final static int RECEIVE_PORT = 6666;
    public final static String DESTINATION = "localhost";

    public static void main(String[] args) throws IOException {
        OSCServer server = OSCServer.newUsing(OSCServer.UDP, RECEIVE_PORT);
        server.start();
        server.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                System.out.println("Message received: " + oscMessage.getName());
            }
        });
        new Thread() {
            public void run() {
                while (true) {
                    System.out.println("Sending message");
                    try {
                        server.send(new OSCMessage("/world/hello"), new InetSocketAddress(DESTINATION, SEND_PORT));
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
