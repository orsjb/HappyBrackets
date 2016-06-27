package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;

/**
 * Created by ollie on 27/06/2016.
 *
 * This one works!
 */
public class TestUsingStandardClasses {

    public static void main(String[] args) throws IOException {

        String multicastIP = "225.2.2.5";
        int port = 5002;
        String networkInterfaceName = "en0";    //works with en0 on Mac, but not lo0.

        //set up the sender

        InetSocketAddress mcSocketAddr = new InetSocketAddress(multicastIP, port);
        OSCTransmitter transmitter = OSCTransmitter.newUsing(OSCChannel.UDP);
        transmitter.setTarget(mcSocketAddr);
        transmitter.connect();

        //start sending a test signal

        new Thread() {
            int count = 0;
            public void run() {
                while(true) {
                    try {
                        transmitter.send(new OSCMessage("tick_" + count++));
                        Thread.sleep(500);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //start listening

        NetworkInterface ni = NetworkInterface.getByName(networkInterfaceName);
        InetAddress group = InetAddress.getByName(multicastIP);
        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(port))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        MembershipKey key = dc.join(group, ni);
        OSCReceiver receiver = OSCReceiver.newUsing(dc);
        receiver.startListening();

        //test that this works

        receiver.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println(msg.getName());
            }
        });

    }
}
