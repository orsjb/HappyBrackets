package net.happybrackets.core;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ollie on 1/06/2016.
 *  Multiple interface behaviour add by Sam on 11/08/2016
 */
public class BroadcastManager {

    final static Logger logger = LoggerFactory.getLogger(BroadcastManager.class);

    String                                        address;
    int                                           port;
    List<NetworkInterfacePair<OSCTransmitter>>    transmitters;
    List<NetworkInterfacePair<OSCReceiver>>       receivers;
    List<OSCListener>                             listeners;
    List<OnListener>                              interfaceListeners; //listeners who care what interface the message arrived at.
    List<NetworkInterface>                        netInterfaces;

    /**
     * Create a new BroadcastManager.
     *
     * @param address should be a multicast address.
     */
    public BroadcastManager(String address, int port) {
        this.address = address;
        this.port    = port;
        initBroadcaster(address, port);
        //automatically refresh the broadcaster every second
        new Thread() {
            public void run() {
                while(true) {
                    refreshBroadcaster();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                }
            }
        }.start();
    }

    /**
     * Initialises this BroadcastManager instance.
     */
    private void initBroadcaster(String address, int port) {
        listeners               = new ArrayList<>();
        interfaceListeners      = new ArrayList<>();
        receivers               = new ArrayList<>();
        transmitters            = new ArrayList<>();
        netInterfaces           = Device.viableInterfaces();

        netInterfaces.forEach( ni -> {
            try {
                InetAddress group = InetAddress.getByName(address);
                //set up a listener and receiver for our broadcast address on this interface
                DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)
                        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                        .bind(new InetSocketAddress(port))
                        .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
                //MembershipKey key = dc.join(group, ni);
                dc.join(group, ni);

                //add receivers
                OSCReceiver receiver = OSCReceiver.newUsing(dc);
                receiver.startListening();
                receiver.addOSCListener(new MessageAggregator(ni));
                receivers.add(new NetworkInterfacePair<OSCReceiver>(ni, receiver));

                // add transmitters
                OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
                transmitter.setTarget( new InetSocketAddress(group.getHostAddress(), port) );
                transmitters.add(new NetworkInterfacePair<OSCTransmitter>(ni, transmitter));

                logger.debug("Broadcasting on interface: {}", ni.getName());
            } catch (IOException e) {
//                logger.error("BroadcastManager encountered an IO exception when creating a listener socket on interface {}!", ni.getName(), e);
                logger.error("BroadcastManager encountered an IO exception when creating a listener socket on interface {}! This interface will not be used.", ni.getName());
            }
        });
    }


    /**
     * Calls dispose on all receivers (OSCReceiver) and transmitters (OSCTransmitter).
     */
    public void dispose() {
//        These calls take an unusually long time and ma not be necessary?
//        receivers.forEach(r -> r.value.dispose());
//        transmitters.forEach(t -> t.value.dispose());
    }

    /**
     * Rebuilds this BroadcastManager. Effectively deletes this instance and creates a new BroadcastManager.
     */
    public void refreshBroadcaster() {
        statefulRefresh(listeners, interfaceListeners);
    }

    /**
     * Rebuilds a listener with the state provided.
     * @param listeners
     * @param interfaceListeners
     */
    private void statefulRefresh(List<OSCListener> listeners, List<OnListener> interfaceListeners) {
        // cleanup:
        logger.debug("Refreshing the broadcaster");
        dispose();
        initBroadcaster(address, port);
        listeners.forEach(l -> addBroadcastListener(l));
        interfaceListeners.forEach(l -> addOnMessage(l));
    }

    /**
     * Broadcast an {@link OSCMessage} msg over the multicast group.
     *
     * @param name the message string to send.
     * @param args the args to the message.
     */
    public void broadcast(String name, Object... args) {
        OSCMessage msg = new OSCMessage(name, args);
        transmitters.stream().map(pair -> pair.value).forEach( transmitter -> {
          try {
              transmitter.send(msg);
          } catch (IOException e) {
              logger.warn("Removing broadcaster interface due to error:", e);
              transmitters.remove(transmitter);
              transmitter.dispose();
          }
        });
    }

    /**
     * Execute onTransmitter.cb(NetworkInterface, OSCTransmitter) for all transmitters.
     * @param onTransmitter
     */
    public void forAllTransmitters(OnTransmitter onTransmitter) {
        transmitters.forEach(pair -> {
            try {
                onTransmitter.cb(pair.networkInterface, pair.value);
            } catch (Exception e) {
                logger.error("Error executing call back on transmitter for interface {}", pair.networkInterface.getDisplayName(), e);
            }
        });
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
     * Add a new interface aware listener
     */
    public void addOnMessage(OnListener onListener) {
        interfaceListeners.add(onListener);
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

    private class NetworkInterfacePair<T> {
        T value;
        NetworkInterface networkInterface;

        public NetworkInterfacePair(NetworkInterface networkInterface, T value) {
            this.value = value;
            this.networkInterface = networkInterface;
        }
    }

    /**
     * An OSCListener for aggregating various broadcast streams for out listeners
     * Enable trace level logging for detailed OSC events
     */
    private class MessageAggregator implements OSCListener {
        NetworkInterface networkInterface;

        public MessageAggregator(NetworkInterface networkInterface) {
            this.networkInterface = networkInterface;
        }

        public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
            if (logger.isTraceEnabled()) {
                String args = " ";
                for(int i = 0; i < msg.getArgCount(); i++) {
                    args += msg.getArg(i) + " ";
                }
                logger.trace("Received broadcast message {} with {} args [{}] from {}", new Object[]{msg.getName(), msg.getArgCount(), args, sender.toString()});
            }
            listeners.forEach(l -> l.messageReceived(msg, sender, time));
            interfaceListeners.forEach(l -> l.cb(networkInterface, msg, sender, time));
        }
    }

    /**
     * Call back interface for sending to all interfaces where the specific interface matters
     */
    public interface OnTransmitter {
        void cb(NetworkInterface ni, OSCTransmitter transmitter) throws IOException;
    }

    /**
     * Call back interface for listening to all interfaces where the specific interface matters
     */
    public interface OnListener {
        void cb(NetworkInterface ni, OSCMessage msg, SocketAddress sender, long time);
    }

}
