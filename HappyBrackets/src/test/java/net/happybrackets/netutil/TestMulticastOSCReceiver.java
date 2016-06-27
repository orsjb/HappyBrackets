package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

/**
 * Created by ollie on 27/06/2016.
 */
public class TestMulticastOSCReceiver {

    public static void main(String[] args) throws IOException {

        //set the sender

        InetSocketAddress mcSocketAddr = new InetSocketAddress("225.2.2.5", 5002);

        OSCTransmitter transmitter = OSCTransmitter.newUsing(OSCChannel.UDP);
        transmitter.setTarget(mcSocketAddr);
        transmitter.connect();

        //starting sending

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


        //start listening

        MulticastSocket ms = new MulticastSocket();
        ms.joinGroup(InetAddress.getByName("225.2.2.5"));
        ms.setTimeToLive(1);

        MulticastOSCReceiver receiver = new MulticastOSCReceiver(OSCPacketCodec.getDefaultCodec(), ms);
        receiver.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                System.out.println(msg.getName());
            }
        });
        receiver.startListening();

    }


}
