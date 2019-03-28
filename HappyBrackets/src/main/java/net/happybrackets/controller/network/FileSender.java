package net.happybrackets.controller.network;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCClient;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class for sending files to device
 */
public class FileSender {

    Set<FileSendStreamer> fileSendStreamerList = new HashSet<>();
    FileSendStreamer currentFile = null;

    // we will synchronise this object
    final Object fileSendEvent = new Object();

    private final Object filePortLock = new Object();
    int fileSendPort =  0; // This is TCP port for sending files to device
    private OSCClient fileSendClient = null;

    /**
     * Add a file to be sent to device
     * After adding, will automatically start to send to device
     * @param source_file source file
     * @param target_file target file
     * @return true if we start the send
     */
    boolean addFile (String source_file, String target_file){
       boolean ret = false;

        System.out.println("Send " + source_file + " to " + target_file);
       FileSendStreamer fileSendStreamer = new FileSendStreamer(source_file, target_file);
       if (!fileSendStreamerList.contains(fileSendStreamer)){
           fileSendStreamerList.add(fileSendStreamer);

           synchronized (fileSendEvent){
               fileSendEvent.notify();
           }
           ret = true;
       }

       return ret;
    }

    /**
     * Close the TCP port for sending File data to client
     */
    void closeFileSendClientPort() {
        synchronized (filePortLock) {
            if (fileSendClient != null) {
                try {
                    if(fileSendClient.isConnected()) {
                        fileSendClient.stop();
                        fileSendClient.dispose();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                fileSendClient = null;
            }
        }
    }

    /**
     * Open TCP Client and assign listeners
     * @param address the address of the device
     * @param port  Port to connect to
     * @return  true on success
     */
    boolean openFileSendClientPort(String address, int port){
        boolean ret = false;
        synchronized (filePortLock) {
            if (fileSendClient == null) {

                try {
                    fileSendClient = OSCClient.newUsing(OSCChannel.TCP);
                    fileSendClient.setTarget(new InetSocketAddress(address, port));
                    fileSendClient.start();

                    ret = true;

                    fileSendClient.addOSCListener(new OSCListener() {
                        public void messageReceived(OSCMessage m, SocketAddress addr, long time) {
                            incomingFileMessage(m, addr);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    fileSendClient.dispose();
                    fileSendClient = null;
                }

            }
        }

        return ret;
    }

    /**
     * Perform actions on File Messages
     * @param m The OSC message containing message
     * @param addr address of sender
     */
    void incomingFileMessage(OSCMessage m, SocketAddress addr){

    }

    /**
     * Set the port we need to connect our File sender to to communicate via TCP
     * @param port remote port number
     */
    public synchronized void setFileSendServerPort(int port){

        if (fileSendPort != port){
            closeFileSendClientPort();
            fileSendPort = port;
        }

    }

}
