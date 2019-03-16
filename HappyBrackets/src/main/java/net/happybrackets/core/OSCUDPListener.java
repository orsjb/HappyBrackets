package net.happybrackets.core;

import de.sciss.net.OSCMessage;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Creates an OSC Listener abstract class. Implement the OSCReceived to create class
 */
abstract public class OSCUDPListener {
    OSCUDPReceiver oscSource = null;
    abstract public void OSCReceived(OSCMessage msg, SocketAddress sender, long time);
    private String lastError = "";

    /**
     * Get the last error caused by an action
     * @return the last error
     */
    public String getLastError(){return lastError;}

    /**
     * Change the port we are listening to
     * Will disconnect from other port but not close it
     * @param port new port number
     * @return true on success. If failed, call getLastError
     */
    boolean setPort(int port){
        boolean ret = false;

        if (oscSource != null){
            oscSource.eraseListeners();
            oscSource = null;
        }

        try {
            oscSource = new OSCUDPReceiver(port);
            oscSource.addOSCListener(this::OSCReceived);
        } catch (IOException e) {
            lastError = e.getMessage();
        }


        return ret;
    }

    /**
     * Constructor
     * @param port the port to open. Using zero will open first available port
     */
    public OSCUDPListener(int port){
        setPort(port);
    }

    /**
     * Get the port we have open. If no port is open will return -1
     * @return the port number that is open
     */
    public int getPort(){
        int ret = -1;
        if (oscSource != null){
            ret = oscSource.getPort();
        }

        return  ret;
    }
}
