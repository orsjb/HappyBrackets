package net.happybrackets.device.network;

import de.sciss.net.OSCServer;

import java.io.IOException;

/**
 * Class that receives files from network so we can store them
 * onto our filesystem instead of using scp
 *
 */
public class FileReceiver {
    // A TCP server to get our file send data from
    private OSCServer controllerOscServer;
    private int oscPort = 0;

    /**
     * Get the OSC port we are using for File reception
     * @return the tcpPort
     */
    public int getReceiverPort(){
        return oscPort;
    }

    /**
     * Constructor opens first available TCP port for OSC Messages
     */
    public FileReceiver(){
        try {
            controllerOscServer =  OSCServer.newUsing(OSCServer.TCP, 0);
            oscPort =  controllerOscServer.getLocalAddress().getPort();

            // Now add a listener
            controllerOscServer.addOSCListener((msg, sender, time) -> {

                System.out.println("FIle Send Message " + msg.getName());
            });

            controllerOscServer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
