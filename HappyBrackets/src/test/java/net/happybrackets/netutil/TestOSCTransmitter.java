package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;

/**
 * Created by ollie on 26/06/2016.
 */
public class TestOSCTransmitter {

    public static void main(String[] args) throws IOException {

        NetworkInterface ni = NetworkInterface.getByName("en0");

        //create the multicaster (this is all from standard examples of DatagramChannel)
//        InetSocketAddress isa = new InetSocketAddress("225.2.2.5", 5002);
//        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
//                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
//                .bind(isa)
//                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
//        InetAddress group = InetAddress.getByName("225.2.2.5");
//        MembershipKey key = dc.join(group, ni);
//        System.out.println("DatagramChannel=" + dc);
//
//        SocketAddress addr = isa;     //not at all sure about this!?!
//        System.out.println("SocketAddress=" + addr);
//
//        OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
//        transmitter.setTarget(addr);    //must do this
//        transmitter.connect();          //probably need to do this

//        System.exit(0);

        //this code works... unicast to self...
//        OSCTransmitter transmitter = OSCTransmitter.newUsing(OSCChannel.UDP);
//        transmitter.setTarget(new InetSocketAddress("localhost", 5002));
//        transmitter.connect();

        OSCTransmitter transmitter = OSCTransmitter.newUsing(OSCChannel.UDP);
        transmitter.setTarget(new InetSocketAddress("localhost", 5002));
        transmitter.connect();

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
