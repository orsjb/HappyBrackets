package net.happybrackets.tutorial.session7;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by samferguson on 27/07/2016.
 */
public class OSCTest {

    public static void main(String[] args) throws IOException {
        OSCServer server = OSCServer.newUsing(OSCServer.UDP, 5555);
        server.start();
        server.addOSCListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                System.out.println("Message received: " + oscMessage.getName());
            }
        });
        new Thread() {
            public void run() {
                while(true) {
                    System.out.println("Sending message");
                    try {
                        server.send(new OSCMessage("hello_back"), new InetSocketAddress("localhost", 6666));
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
