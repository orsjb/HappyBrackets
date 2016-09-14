package net.happybrackets.core;

import de.sciss.net.*;
import net.happybrackets.core.config.EnvironmentConfig;
import net.happybrackets.core.Device;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by ollie on 1/06/2016.
 *  Multiple interface behaviour add by Sam on 11/08/2016
 */
public class BroadcastManager {

    EnvironmentConfig config;
    List<OSCTransmitter> transmitters;
    List<OSCReceiver> receivers;
    List<OSCListener> listeners;

    /**
     * Create a new BroadcastManager.
     *
     * @param config must be a reference to an environment config class implimentation
     */
    public BroadcastManager(EnvironmentConfig config) {
        this.config = config;
        initBroadcaster(config);
    }

    private void initBroadcaster(EnvironmentConfig config) {
        listeners = new ArrayList<OSCListener>();
        receivers = new ArrayList<OSCReceiver>();
        transmitters = new ArrayList<OSCTransmitter>();
        MessageAggregater messageAggregater = new MessageAggregater();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while ( interfaces.hasMoreElements() ) {
                NetworkInterface netInterface = interfaces.nextElement();

                if ( Device.isViableNetworkInterface(netInterface) ) {
                    try {
                        //set up listener
                        setupListener(netInterface, messageAggregater);
                        //setup sender
                        InetSocketAddress mcSocketAddr = new InetSocketAddress(config.getMulticastAddr(), config.getBroadcastPort());
                        InetAddress group = InetAddress.getByName(config.getMulticastAddr());
                        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                                .bind(new InetSocketAddress(config.getBroadcastPort()))
                                .setOption(StandardSocketOptions.IP_MULTICAST_IF, netInterface);
                        dc.join(group, netInterface);
                        OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
                        transmitter.setTarget(mcSocketAddr);
                        transmitters.add(transmitter);
                        //TODO how do we set time to live?
                        System.out.println("Broadcasting on interface: " + netInterface.getName());
                    } catch (IOException e) {
                        System.err.println("Error: BroadcastManager encountered an IO exception when creating a listener socket.");
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println( "Skipped interface: " + netInterface.getName() );
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void setupListener(NetworkInterface ni, MessageAggregater listener) throws IOException {
        InetAddress group = InetAddress.getByName(config.getMulticastAddr());
        DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .bind(new InetSocketAddress(config.getBroadcastPort()))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
        //MembershipKey key = dc.join(group, ni);
        dc.join(group, ni);
        OSCReceiver receiver = OSCReceiver.newUsing(dc);
        receiver.startListening();
        receiver.addOSCListener(new MessageAggregater());
        receivers.add(receiver);
    }

    public void refreshBroadcaster() {
        // cleanup:
        receivers.forEach(r -> r.dispose());
        transmitters.forEach(t -> t.dispose());
        initBroadcaster(config);
    }

    /**
     * Broadcast an {@link OSCMessage} msg over the multicast group.
     *
     * @param name the message string to send.
     * @param args the args to the message.
     */
    public void broadcast(String name, Object... args) {
        OSCMessage msg = new OSCMessage(name, args);
        for (OSCTransmitter transmitter : transmitters) {
          try {
              transmitter.send(msg);
          } catch (IOException e) {
              e.printStackTrace();

              System.out.println("Removing broadcaster interface due to error: " + e.getLocalizedMessage());
              transmitters.remove(transmitter);
              transmitter.dispose();
          }
        }
    }

    /**
     * Add a new {@link OSCListener}.
     *
     * @param bl the new {@link OSCListener}.
     */
    public void addBroadcastListener(OSCListener bl) {
        listeners.add(bl);
        // receiver.addOSCListener(bl);
    }

    /**
     * Remove the given {@link OSCListener}.
     *
     * @param bl the {@link OSCListener} to remove.
     */
    public void removeBroadcastListener(OSCListener bl) {
        listeners.remove(bl);
        // receiver.removeOSCListener(bl);
    }

    /**
     * Clear all {@link OSCListener}s.
     */
    public void clearBroadcastListeners() {
        // for(OSCListener listener : listeners) {
        //     receiver.removeOSCListener(listener);
        // }
        listeners.clear();
    }

    /**
     * An OSCLisenter for aggregting various broadcast streams for out listeners
     */
    private class MessageAggregater implements OSCListener {
      public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
        listeners.forEach(l -> l.messageReceived(msg, sender, time));
      }
    }

}
