package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;

/**
 * Created by ollie on 26/06/2016.
 */
public class TestMulticastOSCTransmitter {

    public static void main(String[] args) throws IOException {

        int mcPort = 50023;
        String mcGroup = "225.2.2.7";

        MulticastSocket ms = new MulticastSocket(mcPort);
        ms.setNetworkInterface(NetworkInterface.getByName("eth3"));
        ms.joinGroup(InetAddress.getByName(mcGroup)); //<--- getting "can't assign requested address error here.
        ms.setTimeToLive(1);

        MulticastOSCTransmitter transmitter = new MulticastOSCTransmitter(ms, mcGroup, mcPort);
        transmitter.connect();

//        System.exit(0);

        new Thread() {
            int count = 0;

            public void run() {
                while (true) {
                    try {
                        transmitter.send(new OSCMessage("tick_" + count++));
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        MulticastOSCReceiver mcListener = new MulticastOSCReceiver(ms, mcGroup, mcPort);
//        mcListener.run();
        mcListener.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println(msg.getName());
            }
        });
        mcListener.startListening();

//        //create the listener
//        OSCServer server = OSCServer.newUsing(OSCServer.UDP, mcPort);
//        server.start();
//        server.addOSCListener(new OSCListener() {
//            @Override
//            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
//                System.out.println("Received message: " + msg.getName());
//            }
//        });

        //test larger message
        // limit appears to be set at 8192 bytes
        // this includes the overhead for the path, ',' separators, null padding and type string.
        String testMessage = "This is a long test message, I repeat: ";
        String longMessage = "";
        for (int i = 0; i < 8187; i++) {
            longMessage += testMessage.charAt(i % testMessage.length());
        }

        transmitter.send(new OSCMessage(longMessage));


    }
}
