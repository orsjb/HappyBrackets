package net.happybrackets.device.network;

import de.sciss.net.OSCChannel;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCPacketCodec;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;

/**
 * Class used to reduce the amount of memory garbage by caching the messages
 */
public class UDPCachedMessage {

    private static ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);
    private static OSCPacketCodec codec = new OSCPacketCodec();

    DatagramPacket cachedPacket = null;
    OSCMessage cachedMessage = null;


    /**
     * Make a cached UDP message to stop having to allocate and deallocate. Also, makes faster re-sending
     * @param msg the OSC message we want to cache
     * @throws IOException
     */
    public UDPCachedMessage (OSCMessage msg) throws IOException {

        setMessage(msg);
    }

    /**
     * We will compare current message the new one. If message has changed, we will rebuild the message
     * @param msg the OSC Message
     * @return The cachedDatagram to send
     * @throws IOException
     */
    public DatagramPacket setMessage(OSCMessage msg) throws IOException {
        if (cachedMessage == null || !msg.equals(cachedMessage)) {
            cachedPacket = buildCachedMessage(msg);
            cachedMessage = msg;
        }

        return cachedPacket;
    }

    /**
     * Build the cached datagram. The function is snchronized so we can reduce memory fragmentation by using singleton buffers.
     * @param msg the OSC message we are encoding.
     * @return the Newely encoded datagram
     * @throws IOException
     */
    private synchronized static DatagramPacket buildCachedMessage (OSCMessage msg) throws IOException {
        byteBuf.clear();
        codec.encode(msg, byteBuf);
        byteBuf.flip();
        byte[] buff = new byte[byteBuf.limit()];
        byteBuf.get(buff);

        return new DatagramPacket(buff, buff.length);
    }

    /**
     * Get the cached packet
     * @return
     */
    public DatagramPacket getCachedPacket() {
        return cachedPacket;
    }

    /**
     * The cached OSC Message
     * @return the msg
     */
    public OSCMessage getCachedMessage() {
        return cachedMessage;
    }

}
