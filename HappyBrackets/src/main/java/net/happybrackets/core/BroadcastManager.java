/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * This class manages broadcast communication - i.e., one to all comms. You would mainly use it to send broadcast messages and listen to broadcast messages. However, these functions are wrapped in the {@link net.happybrackets.device.HB} class and should be accessed there.
 *
 * Created by ollie on 1/06/2016.
 *  Multiple interface behaviour add by Sam Gillespie on 11/08/2016.
 */
public class BroadcastManager {

    final static Logger logger = LoggerFactory.getLogger(BroadcastManager.class);

    String                                        address;
    int                                           port;
    List<NetworkInterfacePair<OSCTransmitter>>    transmitters;
    List<NetworkInterfacePair<OSCReceiver>>       receivers;
    List<OSCListener>                             listeners;
    List<OSCListener>                             peristentListeners = new ArrayList<>();
    List<OnListener>                              interfaceListeners; //listeners who care what interface the message arrived at.
    List<NetworkInterface>                        netInterfaces;


    boolean disableSend = false;

    // we will set this flag if we are running from plugin
    boolean waitForStart = false;

    /**
     * Enable setting of sleep time
     * @param threadSleepTime the amount of time we want our broadcaster to sleep
     */
    public void setThreadSleepTime(int threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }

    private int threadSleepTime = 5000;
    /**
     * Set to disable sending messages from this broadcaster
     * @param disable disable sending
     */
    public void setDisableSend(boolean disable)
    {
        disableSend = disable;
    }


    /**
     * Create a new BroadcastManager.
     *
     * @param address should be a multicast address.
     * @param port The port we are sending on
     */
    public BroadcastManager(String address, int port) {
        this.address = address;
        this.port = port;
        initBroadcaster(address, port);
        //automatically refresh the broadcaster every second
    }

    /**
     * Cause the BroadcastManager to wait before testing the network ports
     * We need to set this here so we can do it from pluging, otherwise, tests will faile
     * @param wait set to true if we will wait 5 seconds for a start
     */
    public void setWaitForStart(boolean wait) {
        this.waitForStart = wait;
    }


    /**
     * Returns the port we are configured for
     * @return the port configured
     */
    public int getPort() {
        return port;}

    public Thread startRefreshThread() {
        logger.debug("creating broadcast refresh thread...");
        Thread t = new Thread() {
            public void run() {

                if (waitForStart)
                {
                    try {
                        Thread.sleep(threadSleepTime);
                    } catch (InterruptedException e) {
                        logger.error("Broadcast manager poll interval interrupted!", e);
                    }
                }

                while(true) {
                    if (!disableSend) {
                        logger.debug("refresh loop...");
                        refreshBroadcaster();
                    }
                    else
                    {
                        logger.debug("disableSend");
                    }

                    try {
                        Thread.sleep(threadSleepTime);
                    } catch (InterruptedException e) {
                        logger.error("Broadcast manager poll interval interrupted!", e);
                    }
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Initialises this BroadcastManager instance.
     */
    private void initBroadcaster(String address, int port) {
        listeners               = new ArrayList<>();
        interfaceListeners      = new ArrayList<>();
        receivers               = new ArrayList<>();
        transmitters            = new ArrayList<>();
        netInterfaces           = new ArrayList<>();
        //  Defer the fiding of interfaces to the refresh cycle so that we don't block the thread loading the BroadcastManager
//        netInterfaces           = Device.viableInterfaces();
//        netInterfaces.forEach( ni -> {
//            try {
//                InetAddress group = InetAddress.getByName(address);
//                //set up a listener and receiver for our broadcast address on this interface
//                DatagramChannel dc = null;
//                // Try creating IPv6 channel first.
//                try {
//                    dc = DatagramChannel.open(StandardProtocolFamily.INET6)
//                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
//                            .bind(new InetSocketAddress(port))
//                            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
//                    dc.join(group, ni);
//                }
//                catch (Exception ex) {
//                    // If creating IPv6 channel doesn't work try IPv4.
//                    dc = DatagramChannel.open(StandardProtocolFamily.INET)
//                            .setOption(StandardSocketOptions.SO_REUSEADDR, true)
//                            .bind(new InetSocketAddress(port))
//                            .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
//                    if(dc != null) {
//                        dc.join(group, ni);
//                    }
//                }
//                if (dc != null) {
//                    //add receivers
//                    OSCReceiver receiver = OSCReceiver.newUsing(dc);
//                    receiver.startListening();
//                    receiver.addOSCListener(new MessageAggregator(ni));
//                    receivers.add(new NetworkInterfacePair<OSCReceiver>(ni, receiver));
//                    // add transmitters
//                    OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
//                    transmitter.setTarget(new InetSocketAddress(group.getHostAddress(), port));
//                    transmitters.add(new NetworkInterfacePair<OSCTransmitter>(ni, transmitter));
//                    logger.debug("Broadcasting on interface: {}", ni.getName());
//                }
//            } catch (IOException e) {
//                logger.error("BroadcastManager encountered an IO exception when creating a listener socket on interface {}! This interface will not be used.", ni.getName());
//            }
//        });
    }

    /**
     * Calls dispose on all receivers (OSCReceiver) and transmitters (OSCTransmitter).
     */
    public void dispose() {
//        These calls take an unusually long time and may not be necessary? Hammering the tests.
        receivers.forEach(r -> r.value.dispose());
        transmitters.forEach(t -> t.value.dispose());
    }

    /**
     * Rebuilds this BroadcastManager. Effectively deletes this instance and creates a new BroadcastManager.
     */
    public void refreshBroadcaster() {
//        statefulRefreshHard(listeners, interfaceListeners);
        statefulRefreshSoft(listeners, interfaceListeners);
    }

    /**
     * Rebuilds a listener with the state provided. This is aggressive and does a complete dispose and rebuild.
     * @param listeners
     * @param interfaceListeners
     */
    private void statefulRefreshHard(List<OSCListener> listeners, List<OnListener> interfaceListeners) {
        // cleanup
        dispose();
        // rebuild
        initBroadcaster(address, port);
        listeners.forEach(l -> addBroadcastListener(l));            //Re add the old listeners to the new lists created by initBroadcaster
        interfaceListeners.forEach(l -> addOnMessage(l));           //
    }

    String wifiIPaddress;

    /**
     * Rebuilds a listener with the state provided. This is gentle and only rebuilds what needs to be rebuilt.
     * @param listeners
     * @param interfaceListeners
     */
    private void statefulRefreshSoft(List<OSCListener> listeners, List<OnListener> interfaceListeners) {
        //Soft refresh - only fix connections that are broken.
        //iterate through existing network interfaces, destroy as required
        List<NetworkInterface> toRemove = new ArrayList<>();
        netInterfaces.forEach( ni -> {
            String name = ni.getName();
            if(!Device.isViableNetworkInterface(ni)) {
                toRemove.add(ni);
                logger.debug("The network interface " + ni + " is no longer valid! Removing it!");
                if(name.equals("en0")) {
                    wifiIPaddress = null;
                }
            }
        });
        //we now have a to-remove list
        //clean up the removes
        receivers.forEach(r -> {
            if(toRemove.contains(r.networkInterface)) {
                r.value.dispose();
            }
        });
        transmitters.forEach(t -> {
            if(toRemove.contains(t.networkInterface)) {
                t.value.dispose();
            }
        });
        netInterfaces.removeAll(toRemove);
        //iterate through the viable interfaces to see if new interfaces have become viable.
        List<NetworkInterface> tempInterfaces = Device.viableInterfaces();

        for (NetworkInterface newInterface : tempInterfaces)
        {
            boolean network_already_exists = false;

            // check if this interface is in existing interfaces
            for(NetworkInterface existingInterface : netInterfaces){
                if(newInterface.getName().equals(existingInterface.getName())) {
                    network_already_exists = true;
                    break;
                }
            }

            // now if network device is not in there yet, we should add it

            if (!network_already_exists){
                logger.debug("The network interface " + newInterface + " has become valid! Adding it!");
                try {
                    InetAddress group = InetAddress.getByName(address);
                    //set up a listener and receiver for our broadcast address on this interface
                    DatagramChannel dc = null;
                    // Try creating IPv6 channel first.
                    try {
                        dc = DatagramChannel.open(StandardProtocolFamily.INET6)
                                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                                .bind(new InetSocketAddress(port))
                                .setOption(StandardSocketOptions.IP_MULTICAST_IF, newInterface);
                        dc.join(group, newInterface);
                    }

                    catch (Exception ex) {
                        // If creating IPv6 channel doesn't work try IPv4.
                        logger.debug("IPv6 failed, falling back to IPv4 for interface {}", newInterface.getName());
                        dc = DatagramChannel.open(StandardProtocolFamily.INET)
                                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                                .bind(new InetSocketAddress(port))
                                .setOption(StandardSocketOptions.IP_MULTICAST_IF, newInterface);

                        if(dc != null) {
                            dc.join(group, newInterface);
                        }
                    }

                    if (dc != null) {
                        //add receivers
                        OSCReceiver receiver = OSCReceiver.newUsing(dc);
                        receiver.startListening();
                        receiver.addOSCListener(new MessageAggregator(newInterface));
                        receivers.add(new NetworkInterfacePair<OSCReceiver>(newInterface, receiver));
                        // add transmitters
                        OSCTransmitter transmitter = OSCTransmitter.newUsing(dc);
                        transmitter.setTarget(new InetSocketAddress(group.getHostAddress(), port));
                        transmitters.add(new NetworkInterfacePair<OSCTransmitter>(newInterface, transmitter));

                        netInterfaces.add(newInterface);
                        logger.debug("Broadcasting on interface: {}", newInterface.getName());
                    }

                } catch (IOException e) {
                    logger.warn("BroadcastManager encountered an IO exception when creating a listener socket on interface {}! Trying again next refresh.", newInterface.getName());
                    logger.debug("Stacktrace:", e);
                }
            }
        }

    }

    /**
     * Broadcast an {@link OSCMessage} msg over the multicast group.
     *
     * @param name the message string to send.
     * @param args the args to the message.
     */
    public void broadcast(String name, Object... args) {

        try {
            if (!disableSend) {
                OSCMessage msg = new OSCMessage(name, args);
                ArrayList<NetworkInterfacePair<OSCTransmitter>> removal_list = new ArrayList<>(); // do not intantiate unless we actually need one

                transmitters.stream().map(pair -> pair.value).forEach(transmitter -> {
                    try {
                        transmitter.send(msg);
                    } catch (IOException e) {
                        logger.warn("Removing broadcaster interface due to error:", e);

                        transmitters.stream().filter(t -> transmitter.equals((OSCTransmitter) t.value)).forEach(match -> {
                            removal_list.add(match);
                        });

                    }
                });

                removal_list.forEach(pair -> {
                    netInterfaces.remove(pair.networkInterface);
                    transmitters.remove(pair);
                    pair.value.dispose();
                });

                removal_list.clear();
            }
        }
        catch (Exception ex)
        {
            logger.warn("Removing broadcaster interface due to error:", ex );
        }
    }

    /**
     * Execute onTransmitter.cb(NetworkInterface, OSCTransmitter) for all transmitters.
     * @param onTransmitter Interface message
     */
    public void forAllTransmitters(OnTransmitter onTransmitter) {
        if (!disableSend) {
            try {
                ArrayList<NetworkInterfacePair<OSCTransmitter>> removal_list = new ArrayList<>();


                transmitters.forEach(pair -> {
                    try {
                        onTransmitter.cb(pair.networkInterface, pair.value);
                    } catch (Exception e) {
                        logger.error("Error executing call back on transmitter for interface {}, removing interface", pair.networkInterface.getDisplayName(), e);

                        removal_list.add(pair);

                    }
                });
                removal_list.forEach(pair -> {
                    netInterfaces.remove(pair.networkInterface);
                    transmitters.remove(pair);
                    pair.value.dispose();
                });

                removal_list.clear();
            } catch (Exception ex) {
                logger.error("Error executing forAllTransmitters " + ex.getMessage());
            }
        }
    }

    /**
     * Add Listeners that do not get cleared when HB is reset
     * @param bl The broadcast listener
     */
    public void addPersistentBroadcastListener(OSCListener bl){

        peristentListeners.add(bl);
        listeners.add(bl);
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
     * Get the broadcast address for a particular network interface
     * @param ni Network Intervace. If we set as NULL, we will use 255.255.255.255
     * @return brodcast address if it exists
     */
    public static InetAddress getBroadcast(NetworkInterface ni)
    {
        InetAddress broadcast = null;

        try {
            if (ni == null)
            {
                broadcast = InetAddress.getByName("255.255.255.255");
            }
            else {
                if (ni.isLoopback()) {
                    broadcast = InetAddress.getByName("localhost");
                } else {
                    for (InterfaceAddress interface_address : ni.getInterfaceAddresses()) {
                        broadcast = interface_address.getBroadcast();
                        if (broadcast != null) {
                            break;
                        }
                    }
                }
            }
        }catch (Exception ex)
        {

        }
        return broadcast;
    }

    /**
     * Add a new interface aware listener
     * @param onListener the listener
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
        for (OSCListener listener : peristentListeners) {
            listeners.add(listener);

        }
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
