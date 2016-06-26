package net.happybrackets.netutil;

import de.sciss.net.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;
import java.nio.channels.SelectableChannel;

/**
 * Created by ollie on 26/06/2016.
 */
public class MulticastOSCTransmitter extends OSCTransmitter {

    boolean connected = false;
    MulticastSocket ms;

    public MulticastOSCTransmitter(MulticastSocket ms) {
        this(OSCPacketCodec.getDefaultCodec(), OSCChannel.UDP, null, false);
        this.ms = ms;
    }

    protected MulticastOSCTransmitter(OSCPacketCodec c, String protocol, InetSocketAddress localAddress, boolean revivable) {
        super(c, protocol, localAddress, revivable);
    }

    @Override
    public InetSocketAddress getLocalAddress() throws IOException {
        return localAddress;
    }

    @Override
    public void connect() throws IOException {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void send(OSCPacketCodec c, OSCPacket p, SocketAddress target) throws IOException {
        try {
                checkBuffer();
                byteBuf.clear();
                c.encode( p, byteBuf );
                byteBuf.flip();
                if( dumpMode != kDumpOff ) {
                    printStream.print( "s: " );
                    if( (dumpMode & kDumpText) != 0 ) OSCPacket.printTextOn( printStream, p );
                    if( (dumpMode & kDumpHex)  != 0 ) {
                        OSCPacket.printHexOn( printStream, byteBuf );
                        byteBuf.flip();
                    }
                }
                ms.send(new DatagramPacket(byteBuf.array(), byteBuf.array().length));
            }
        catch( BufferOverflowException e1 ) {
            throw new OSCException( OSCException.BUFFER,
                    p instanceof OSCMessage ? ((OSCMessage) p).getName() : p.getClass().getName() );
        }
    }

    @Override
    public void send(OSCPacketCodec c, OSCPacket p) throws IOException {
        send(c, p, target);
    }

    @Override
    protected SelectableChannel getChannel() {
        return null;
    }

}
