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

package net.happybrackets.device.config;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.network.UDPCachedMessage;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * A small class to keep our host controller's hostname, address and port together
 * We also use it to cache the OSC Akive Message we are sending back
 */
public class DeviceController {


    private String hostname;

    private InetSocketAddress socketAddress;

    int hash; // Unique hash

    static int deviceId; // the device ID we were when we were made

    static int connectPort; // The TCP port that clients need to connect to

    static private UDPCachedMessage cachedMessage = null;

    private long lastTimeSeen;

    ByteBuffer byteBuf; // Buffer we will need to make our cached message

    /**
     * Set the last time controller seen as now
     */
    public void controllerSeen(){
        lastTimeSeen = System.currentTimeMillis();
    }


    /**
     * Get the cached message we are going to send
     * @return the Cached message
     */
    static public UDPCachedMessage getCachedMessage(){
        if (cachedMessage == null)
        {
            rebuildCachedMessage();
        }

        return cachedMessage;
    }

    /**
     * Rebuild the cached message that we use to send messages to controller
     * @return the new cachedMessage
     */
    static UDPCachedMessage rebuildCachedMessage() {
        try {

            OSCMessage msg = new OSCMessage(
                    OSCVocabulary.Device.ALIVE,
                    new Object[]{
                            Device.getDeviceName(),
                            Device.getDeviceName(), //Device.selectHostname(ni),
                            "", //Device.selectIP(ni),
                            deviceId,
                            connectPort
                    }
            );

            if (cachedMessage == null) {
                cachedMessage = new UDPCachedMessage(msg);
            }
            else
            {
                cachedMessage.setMessage(msg);
            }

        } catch (Exception ex) {
        }


        return cachedMessage;
    }


    /**
     * Create a hash code we can use to compare that we are equal
     * @param address ip address of controller
     * @param port  port the controller UDP port
     * @param connectPort the Port we need to connect to TCP server
     * @return a hash code that combines these factors
     */
    public static int buildHashCode(String address, int port, int connectPort)
    {
        String hash_build = address + port + connectPort;
        return hash_build.hashCode();
    }
    /**
     * Create a controller based on ip address and port it wants to receive messages on
     * @param hostname hostname provided by controller
     * @param address ip address that we send messages to this controller
     * @param port the port we send messages on
     * @param device_id the device id we were when were made
     * @param connectPort the Port we need to connect to TCP server
     */
    public DeviceController(String hostname, String address, int port, int device_id, int connectPort) {
        byteBuf	= ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

        this.hostname = hostname;
        this.connectPort = connectPort;

        socketAddress = new InetSocketAddress(address, port);
        hash = buildHashCode(address, port, connectPort);
        lastTimeSeen = System.currentTimeMillis();

        deviceId = device_id;
        cachedMessage = rebuildCachedMessage();
    }


    /**
     * Sets the new device ID and rebuilds cached message for it if required
     * @param new_id the new ID
     */
    public static synchronized void setDeviceId (int new_id){
        if (new_id != deviceId)
        {
            deviceId = new_id;
            cachedMessage = rebuildCachedMessage();
        }
    }
    /**
     * Has code based on i[ address and port
     * @return the hashCode
     */
    public int hashCode() {
        return hash;
    }

    /**
     * The socket address we use to send messages t this controller
     * @return the socketAddress
     */
    public InetSocketAddress getAddress() {
        return socketAddress;
    }


    /**
     * The hostname of the controller
     * @return hostname
     */
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}

