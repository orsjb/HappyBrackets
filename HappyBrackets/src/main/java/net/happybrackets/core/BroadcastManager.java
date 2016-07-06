package net.happybrackets.core;

import de.sciss.net.*;
import net.happybrackets.device.config.DeviceConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ollie on 1/06/2016.
 */
public class BroadcastManager {

    DeviceConfig config;
    OSCTransmitter transmitter;
    OSCReceiver receiver;
    List<OSCListener> listeners;

    /**
     * Create a new BroadcastManager.
     */
    public BroadcastManager(DeviceConfig config) {
        this.config = config;
        listeners = new ArrayList<>();
        try {
            //set up listener
            setupListener();
            //setup sender
            InetSocketAddress mcSocketAddr = new InetSocketAddress(config.getMulticastAddr(), config.getBroadcastPort());
            transmitter = OSCTransmitter.newUsing(OSCChannel.UDP);
            transmitter.setTarget(mcSocketAddr);
            transmitter.connect();
            //TODO how do we set time to live?
        } catch (IOException e) {
            System.err.println("Warning: BroadcastManager can't use multicast. No broadcast functionality available in this session.");         //TODO is this diagnosis still correct?
        }
    }

    private void setupListener() throws IOException {
        NetworkInterface ni = NetworkInterface.getByName(Device.getInstance().preferredInterface);
        InetAddress group = InetAddress.getByName(config.getMulticastAddr());
        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(config.getBroadcastPort()))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        MembershipKey key = dc.join(group, ni);
        receiver = OSCReceiver.newUsing(dc);
        receiver.startListening();
    }

    /**
     * Broadcast an {@link OSCMessage} msg over the multicast group.
     *
     * @param name the message string to send.
     * @param args the args to the message.
     */
    public void broadcast(String name, Object... args) {
        OSCMessage msg = new OSCMessage(name, args);
        try {
            transmitter.send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new {@link OSCListener}.
     *
     * @param bl the new {@link OSCListener}.
     */
    public void addBroadcastListener(OSCListener bl) {
        listeners.add(bl);
        receiver.addOSCListener(bl);
    }

    /**
     * Remove the given {@link OSCListener}.
     *
     * @param bl the {@link OSCListener} to remove.
     */
    public void removeBroadcastListener(OSCListener bl) {
        listeners.remove(bl);
        receiver.removeOSCListener(bl);
    }

    /**
     * Clear all {@link OSCListener}s.
     */
    public void clearBroadcastListeners() {
        for(OSCListener listener : listeners) {
            receiver.removeOSCListener(listener);
        }
        listeners.clear();
    }

}
