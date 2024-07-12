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

package net.happybrackets.intellij_plugin.controller.network;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.core.config.DefaultConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ControllerAdvertiser {

    final static Logger logger = LoggerFactory.getLogger(ControllerAdvertiser.class);
    private final Object cachedNetworkMessageLock = new Object();
    private final Object deviceAdvertisedMessageLock = new Object();
    int secondaryBroadcastPort = DefaultConfig.SECONDARY_BROADCAST_PORT;
    int replyPort; // leave it undefined so we can see a warning if it does not get assigned
    int broadcastPort;
    // if we set this false, we will also send broadcast messages
    boolean onlyMulticastMessages = true;
    CachedMessage cachedBroadcastMessage = null;
    CachedMessage cachedMulticastMessage = null;
    CachedMessage cachedLocalhostMessage = null;
    DatagramSocket advertiseTxSocket = null;
    boolean sendLocalHost = false;
    private Thread advertisementService;
    private boolean keepAlive = true;
    // we will do a network poll once if this flag becomes true
    private boolean probeBroadcast = false;
    private List<CachedMessage> cachedNetworkMessage = new ArrayList<>();
    // we will store any devices we see here and send to them specifically
    private Map<InetAddress, CachedMessage> deviceAdvertisedMessage = new Hashtable<>();

    /**
     * Create a controller advertiser that also tells the device what port to send reply to
     *
     * @param multicast_address the multicast address we will use
     * @param broadcast_port    The port we will send to
     * @param reply_port        The port we want the device to respond to
     */
    public ControllerAdvertiser(String multicast_address, int broadcast_port, int reply_port) {

        replyPort = reply_port;
        broadcastPort = broadcast_port;
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

        try {
            advertiseTxSocket = new DatagramSocket();
            advertiseTxSocket.setBroadcast(true);
            advertiseTxSocket.setReuseAddress(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        OSCMessage msg = new OSCMessage(
                OSCVocabulary.CONTROLLER.CONTROLLER,
                new Object[]{
                        Device.getDeviceName(),
                        replyPort
                }
        );
        OSCPacketCodec codec = new OSCPacketCodec();

        try {
            byteBuf.clear();
            codec.encode(msg, byteBuf);
            byteBuf.flip();
            byte[] buff = new byte[byteBuf.limit()];
            byteBuf.get(buff);
            InetAddress broadcast = InetAddress.getByName("255.255.255.255");
            InetAddress multicast = InetAddress.getByName(multicast_address);
            InetAddress localhost = InetAddress.getLoopbackAddress();

            // Now we are going to broadcast on network interface specific
            DatagramPacket packet = new DatagramPacket(buff, buff.length, broadcast, broadcastPort);
            cachedBroadcastMessage = new CachedMessage(msg, buff, packet, broadcast);

            DatagramPacket multicast_packet = new DatagramPacket(buff, buff.length, multicast, broadcastPort);
            cachedMulticastMessage = new CachedMessage(msg, buff, multicast_packet, multicast);

            DatagramPacket locahost_packet = new DatagramPacket(buff, buff.length, localhost, broadcastPort);
            cachedLocalhostMessage = new CachedMessage(msg, buff, locahost_packet, localhost);


            //Do not load these at startup - could be locking up
            //loadNetworkBroadcastAdverticements();
        } catch (Exception ex) {
            logger.error("Unable to create cached message", ex);
        }

        //set up an indefinite thread to advertise the controller
        advertisementService = new Thread() {
            public void run() {

                while (keepAlive) {

                    boolean reload_cached_messages = false;
                    if (!DeviceConnection.getDisabledAdvertise()) {
                        // first send to our multicast
                        try {
                            advertiseTxSocket.send(cachedMulticastMessage.cachedPacket);
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }


                        if (sendLocalHost) {
                            try {
                                advertiseTxSocket.send(cachedLocalhostMessage.cachedPacket);
                            } catch (IOException e) {
                                //e.printStackTrace();
                            }
                        }

                        synchronized (deviceAdvertisedMessageLock) {
                            for (CachedMessage message : deviceAdvertisedMessage.values()) {
                                if (message != null) {
                                    try {
                                        DatagramPacket cached_packet = message.getCachedPacket();
                                        advertiseTxSocket.send(cached_packet);
                                        //System.out.println("Send advertise to " + cached_packet.getAddress().toString());
                                    } catch (Exception ex) {
                                        System.out.println("Unable to send advertise to specific device");
                                        System.out.println(ex.getMessage());
                                    }
                                }
                            }

                        }

                        if (probeBroadcast || !onlyMulticastMessages) {
                            DatagramPacket packet = cachedBroadcastMessage.getCachedPacket();

                            // Now send a broadcast
                            try {
                                advertiseTxSocket.send(packet);
                            } catch (Exception ex) {
                                //System.out.println(ex.getMessage());
                            }

                            synchronized (cachedNetworkMessageLock) {
                                try {
                                    for (CachedMessage cachedMessage : cachedNetworkMessage) {
                                        DatagramPacket cached_packet = cachedMessage.cachedPacket;
                                        try {
                                            advertiseTxSocket.send(cached_packet);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            reload_cached_messages = true;
                                        }
                                    }

                                } catch (Exception ex_all) {

                                }

                                // we will turn off broadcast poll if we turned it on
                                probeBroadcast = false;
                            }


                            List<NetworkInterface> interfaces = Device.viableInterfaces();

                            // if our network has changed, do a reload of our cached messages
                            if (networkChanged()) {
                                reload_cached_messages = true;
                            }

                            if (reload_cached_messages) {
                                loadNetworkBroadcastAdverticements();
                            }

                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("Sleep was interrupted in ControllerAdvertiser thread", e);
                    }
                }
            }
        };
    }

    public void deviceAliveReceived(SocketAddress device_address) {
        try {
            InetAddress inetAddress = ((InetSocketAddress) device_address).getAddress();

            synchronized (deviceAdvertisedMessageLock) {
                if (!deviceAdvertisedMessage.containsKey(inetAddress)) {
                    deviceAdvertisedMessage.put(inetAddress, buildCachedMessage(inetAddress, broadcastPort));
                    deviceAdvertisedMessage.put(inetAddress, buildCachedMessage(inetAddress, secondaryBroadcastPort));

                }
            }
        } catch (Exception ex) {
        }

    }

    /**
     * Make the advertiser do one poll on broadcast
     */
    public void doBroadcastProbe() {
        loadNetworkBroadcastAdverticements();
        probeBroadcast = true;
    }

    /**
     * Are we only doing Multicast message
     *
     * @return true if we are only doing multicast and are not doing broadcast
     */
    public boolean isOnlyMulticastMessages() {
        return onlyMulticastMessages;
    }

    /**
     * Set if we are going to only allow multicast ot set to false if we will also put broadcast messages
     *
     * @param onlyMulticastMessages true if we are only going to allow multicast messages. Fals if we will also do broadcast messages
     */
    public void setOnlyMulticastMessages(boolean onlyMulticastMessages) {
        this.onlyMulticastMessages = onlyMulticastMessages;
    }

    /**
     * Set if we are going to advertise on localhost
     *
     * @param sendLocalHost true if we are going to advertise on localhost
     */
    public void setSendLocalHost(boolean sendLocalHost) {
        this.sendLocalHost = sendLocalHost;
    }

    /**
     * check if the number of network devices has changed from what we have as
     * our cached messages
     *
     * @return true if the number of networks with broadcast is not equal tro what we have cached
     */
    boolean networkChanged() {
        List<NetworkInterface> interfaces = Device.viableInterfaces();

        int num_interfaces = 0;
        for (NetworkInterface ni : interfaces) {
            InetAddress broadcast = BroadcastManager.getBroadcast(ni);


            if (broadcast != null) {
                num_interfaces++;
            }
        }
        ;

        synchronized (cachedNetworkMessageLock) {
            return num_interfaces != cachedNetworkMessage.size();
        }
    }


    /**
     * Build a cached message to send to a target address
     *
     * @param address the address to send to
     * @param port    the target address port
     * @return the cached message
     * @throws IOException if unable to create
     */
    CachedMessage buildCachedMessage(InetAddress address, int port) throws IOException {
        OSCMessage msg = new OSCMessage(
                OSCVocabulary.CONTROLLER.CONTROLLER,
                new Object[]{
                        Device.getDeviceName(),
                        replyPort
                }
        );
        OSCPacketCodec codec = new OSCPacketCodec();

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

        byteBuf.clear();
        codec.encode(msg, byteBuf);
        byteBuf.flip();
        byte[] buff = new byte[byteBuf.limit()];
        byteBuf.get(buff);

        DatagramPacket packet = new DatagramPacket(buff, buff.length, address, port);
        CachedMessage message = new CachedMessage(msg, buff, packet, address);
        return message;
    }


    /**
     * Load a set of Broadcast messages in the standard broadcast message fails
     */
    public void loadNetworkBroadcastAdverticements() {
        synchronized (cachedNetworkMessageLock) {
            cachedNetworkMessage.clear();

            try {

                List<NetworkInterface> interfaces = Device.viableInterfaces();

                interfaces.forEach(ni -> {

                    InetAddress broadcast = BroadcastManager.getBroadcast(ni);


                    if (broadcast != null) {
                        try {
                            // Now we are going to broadcast on network interface specific

                            cachedNetworkMessage.add(buildCachedMessage(broadcast, broadcastPort));


                            // now we need to add another one for non multicast port
                            cachedNetworkMessage.add(buildCachedMessage(broadcast, secondaryBroadcastPort));


                        } catch (Exception ex) {
                            logger.error("Unable to create cached message", ex);
                        }

                    }

                });

            } catch (Exception ex) {
                logger.error("Unable to create cached message", ex);
            }
        }

    }

    public void start() {
        keepAlive = true;
        advertisementService.start();
    }

    public void stop() {
        keepAlive = false;
    }

    public void interrupt() {
        advertisementService.interrupt();
    }

    public boolean isAlive() {
        return advertisementService.isAlive();
    }

    /**
     * Class that contains a cached message to send to UDP to reduce garbage
     */
    private class CachedMessage {
        DatagramPacket cachedPacket;
        OSCMessage cachedMessage;
        InetAddress broadcastAddress;
        byte[] msgBuff; // Just the data inside the packet

        /**
         * Create a cached message to send
         *
         * @param msg               The OSC Message
         * @param msg_buff          The Bytes inside OSC Message
         * @param packet            The Message as a datagram packet
         * @param broadcast_address The address were packet is going
         */
        public CachedMessage(OSCMessage msg, byte[] msg_buff, DatagramPacket packet, InetAddress broadcast_address) {
            cachedPacket = packet;
            cachedMessage = msg;
            msgBuff = msg_buff;
            broadcastAddress = broadcast_address;
        }

        /**
         * Get the cached packet
         *
         * @return
         */
        public DatagramPacket getCachedPacket() {
            return cachedPacket;
        }

        public byte[] getMsgBuff() {
            return msgBuff;
        }

        /**
         * The cached OSC Message
         *
         * @return the msg
         */
        public OSCMessage getCachedMessage() {
            return cachedMessage;
        }
    }

}
