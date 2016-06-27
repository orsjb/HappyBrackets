package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;

/**
 * Created by ollie on 26/06/2016.
 */
public class TestMulticastOSCTransmitter {

    public static void main(String[] args) throws IOException {

        MulticastSocket ms = new MulticastSocket(5002);
        ms.joinGroup(InetAddress.getByName("225.2.2.5")); //<--- getting "can't assign requested address error here.
        ms.setTimeToLive(1);

        MulticastOSCTransmitter transmitter = new MulticastOSCTransmitter(ms);
        transmitter.connect();

//        System.exit(0);

        new Thread() {
            int count = 0;
            public void run() {
                while(true) {
                    try {
                        transmitter.send(new OSCMessage("tick_" + count++));
                        Thread.sleep(1000);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //create the listener
        OSCServer server = OSCServer.newUsing(OSCServer.UDP, 5002);
        server.start();
        server.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println("Received message: " + msg.getName());
            }
        });


    }
}
