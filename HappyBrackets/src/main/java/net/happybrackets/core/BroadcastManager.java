package net.happybrackets.core;

import net.happybrackets.device.config.DeviceConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ollie on 1/06/2016.
 */
public class BroadcastManager {

    //TODO this could use proper implementation of OSC? To be seen if NetUtil can handle multicast connection. Also while we're at it, can NetUtil send generic objects?

    /**
     * Listener used to respond to incoming broadcasts.
     */
    public interface Listener {

        /**
         * Handle incoming broadcasts.
         * @param s incoming {@link String}.
         */
        public void messageReceived(String s);
    }

    private List<Listener> listeners = new ArrayList<Listener>();
    private MulticastSocket broadcastSocket;

    /**
     * Create a new BroadcastManager.
     */
    public BroadcastManager() {
        try {
        //set up listener
        setupListener();
        //setup sender
        broadcastSocket = new MulticastSocket();
        broadcastSocket.setTimeToLive(1);
    } catch(IOException e) {
        System.err.println("Warning: BroadcastManager can't use multicast. No broadcast functionality available in this session.");
    }
    }

    private void setupListener() throws IOException {
        MulticastSocket s = new MulticastSocket(DeviceConfig.getInstance().getBroadcastPort());
        s.joinGroup(InetAddress.getByName(DeviceConfig.getInstance().getMulticastAddr()));
        //start a listener thread
        Thread t = new Thread() {
            public void run() {
                while(true) {
                    if(s != null) {
                        try {
                            byte[] buf = new byte[512];
                            DatagramPacket pack = new DatagramPacket(buf, buf.length);
                            s.receive(pack);
                            String message = new String(buf, "US-ASCII");
                            messageReceived(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //s.close();
            }
        };
        t.start();
    }

    /**
     * Broadcast {@link String} s over the multicast group.
     *
     * @param s the message to send.
     */
    public void broadcast(String s) {
        byte buf[] = null;
        try {
            buf = s.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        // Create a DatagramPacket
        DatagramPacket pack = null;
        try {
            pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(DeviceConfig.getInstance().getMulticastAddr()), DeviceConfig.getInstance().getBroadcastPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            broadcastSocket.send(pack);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO should we be filtering out messages sent by ourselves? Or do we allow that? Else can we identify the sender in this function?
    private void messageReceived(String msg) {
        String[] parts = msg.split("[ ]");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        //Listeners
        for(Listener bl : listeners) {
            bl.messageReceived(msg);
        }
    }

    /**
     * Add a new {@link Listener}.
     * @param bl the new {@link Listener}.
     */
    public void addBroadcastListener(Listener bl) {
        listeners.add(bl);
    }

    /**
     * Remove the given {@link Listener}.
     * @param bl the {@link Listener} to remove.
     */
    public void removeBroadcastListener(Listener bl) {
        listeners.remove(bl);
    }

    /**
     * Clear all {@link Listener}s.
     */
    public void clearBroadcastListeners() {
        listeners.clear();
    }

}
