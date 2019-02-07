package net.happybrackets.core;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Class for encoding OSC Messages and sending via a UDP Port
 * Removes all the unneccesary server implementation
 */
public class OSCGenericSender {
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);
    OSCPacketCodec codec = new OSCPacketCodec();
    DatagramSocket sendingSocket = null;

    public synchronized void send(OSCMessage msg, InetSocketAddress address) throws IOException {
        byteBuf.clear();
        codec.encode(msg, byteBuf);
        byteBuf.flip();
        byte[] buff = new byte[byteBuf.limit()];
        byteBuf.get(buff);
        DatagramPacket packet = new DatagramPacket(buff, buff.length, address.getAddress(), address.getPort());
        if (sendingSocket == null){
            sendingSocket =  new DatagramSocket();
        }

        sendingSocket.send(packet);
    }
}
