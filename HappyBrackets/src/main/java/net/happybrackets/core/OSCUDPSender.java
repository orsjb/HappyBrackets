package net.happybrackets.core;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacket;
import de.sciss.net.OSCPacketCodec;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * Class for encoding OSC Messages and sending via a UDP Port
 * Removes all the unnecessary server implementation
 */
public class OSCUDPSender {
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);
    OSCPacketCodec codec = new OSCPacketCodec();
    DatagramSocket sendingSocket = null;

    private String lastError = "";

    /**
     * Get the last error caused by an action
     * @return the last error
     */
    public String getLastError(){return lastError;}

    /**
     * Send OSC given a OSC Message or packet and Socket address
     * @param msg OSC Message
     * @param inetSocketAddress SocketAddress
     * @return true on success. If failed, call getLastError
     */
    public synchronized boolean send(OSCPacket msg, InetSocketAddress inetSocketAddress) {
        boolean ret = false;

        byteBuf.clear();
        try {
            codec.encode(msg, byteBuf);
            byteBuf.flip();
            byte[] buff = new byte[byteBuf.limit()];
            byteBuf.get(buff);
            DatagramPacket packet = new DatagramPacket(buff, buff.length, inetSocketAddress.getAddress(), inetSocketAddress.getPort());
            if (sendingSocket == null){
                sendingSocket =  new DatagramSocket();
            }

            sendingSocket.send(packet);
            ret = true;
        } catch (IOException e) {
            lastError = e.getMessage();

        }

        return ret;
    }

    /**
     * Send OSC given a OSC Message or packet and iNetAddress and port
     * @param msg  OSC Message
     * @param inetAddress iNetAddress
     * @param port UDP Port
     * @return true on success. If failed, call getLastError
     */
    public synchronized boolean send(OSCPacket msg, InetAddress inetAddress, int port){

        InetSocketAddress osc_target = new InetSocketAddress(inetAddress, port);
        return send(msg, osc_target);
    }

    /**
     * Send OSC given a OSC Message or packet and iNetAddress and port
     * @param msg  OSC Message
     * @param hostaddress host address
     * @param port UDP Port
     * @return true on success. If failed, call getLastError
     */
    public synchronized boolean send(OSCPacket msg, String hostaddress, int port)  {
        boolean ret = false;
        InetSocketAddress osc_target = null;
        try {
            osc_target = new InetSocketAddress(InetAddress.getByName(hostaddress), port);
            ret = send(msg, osc_target);
        } catch (UnknownHostException e) {
            lastError = e.getMessage();
        }

        return ret;

    }
}
