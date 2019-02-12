package net.happybrackets.core;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple OSC Receiver without all the overhead. Opens a socket and reads it
 */
public class OSCGenericReceiver {
    private final List collListeners   = new ArrayList();

    ServerSocket serverSocket;
    DatagramSocket receiver = null;
    OSCPacketCodec codec = new OSCPacketCodec();
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

    final Object listenerLock = new Object();

    volatile boolean doListen = false;
    /**
     * Create a Receiver port
     * @param port the port to receive
     * @throws SocketException Exception if Unable to open Socket
     */
    public OSCGenericReceiver(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        port = serverSocket.getLocalPort();
        receiver =  new DatagramSocket(port);
        receiver.setReuseAddress(true);
    }

    /**
     * Create a OSC Receiver finding own port
     * @throws SocketException Exception if unable to open
     */
    public OSCGenericReceiver() throws IOException {
        this(0);

    }

    /**
     *  Registers a listener that gets informed
     *  about incoming messages. You can call this
     *  both when listening was started and stopped.
     *
     *  @param  listener	the listener to register
     */
    public void addOSCListener( OSCListener listener )
    {
        synchronized( listenerLock) {
            collListeners.add( listener );
        }
    }

    /**
     *  Unregisters a listener that gets informed
     *  about incoming messages
     *
     *  @param  listener	the listener to remove from
     *						the list of notified objects.
     */
    public void removeOSCListener( OSCListener listener )
    {
        synchronized(listenerLock) {
            collListeners.remove( listener );
        }
    }

    /**
     * Return the port for this server
     * @return the receive port
     */
    public int getPort(){
        int ret = 0;
        if (receiver != null){
            ret = receiver.getLocalPort();

        }
        return ret;

    }

    /**
     * Do receive
     * @throws IOException
     */
    private void doReceive() throws IOException {
        byte[] receive_data = new byte[OSCChannel.DEFAULTBUFSIZE];

        int i = 0;
        while (doListen){
            DatagramPacket packet = new DatagramPacket(receive_data, receive_data.length);
            receiver.receive(packet);

            byteBuf.clear();
            byteBuf.put(packet.getData());
            final OSCPacket p;

            try {
                byteBuf.flip();

                p = codec.decode( byteBuf );
                // OSCBundles will override this dummy time tag

                dispatchPacket(p, packet.getSocketAddress(), OSCBundle.NOW);
            }
            catch( BufferUnderflowException e1 ) {
                    System.err.println( new OSCException( OSCException.RECEIVE, e1.toString() ));
            }
        }

    }


    /**
     * Start listening
     * @return true if started
     */
    public boolean start(){
        boolean ret = false;

        if (!doListen){
            doListen = true;

            Thread thread = new Thread(() -> {

                try {
                    doReceive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            thread.start();/* End threadFunction */
            ret = true;
        }
        return ret;
    }
    /**
     * Send packet
     * @param packet the OSC Packet
     * @param sender the sender
     * @param time when to send
     */
    private void dispatchPacket(OSCPacket packet, SocketAddress sender, long time )
    {
        if( packet instanceof OSCMessage ) {
            dispatchMessage( (OSCMessage) packet, sender, time );
        } else if( packet instanceof OSCBundle ) {
            final OSCBundle bndl	= (OSCBundle) packet;
            time					= bndl.getTimeTag();
            for( int i = 0; i < bndl.getPacketCount(); i++ ) {
                dispatchPacket( bndl.getPacket( i ), sender, time );
            }
        } else {
            assert false : packet.getClass().getName();
        }
    }

    /**
     * Send the message Out to all listeners
     * @param msg the OSC Message
     * @param sender tHE SENDER OF THE MESSAGE
     * @param time WHEN TO SEND
     */
    private void dispatchMessage( OSCMessage msg, SocketAddress sender, long time )
    {
        OSCListener listener;

        synchronized(listenerLock) {
            for( int i = 0; i < collListeners.size(); i++ ) {
                listener = (OSCListener) collListeners.get( i );
				try {
                listener.messageReceived( msg, sender, time );
				}
				catch( java.lang.RuntimeException e1 ) {
					e1.printStackTrace();
				}
            }
        }
    }

    public static void main(String[] args) {

        try {

            OSCGenericReceiver receiver = new OSCGenericReceiver(2222);

            receiver.doReceive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
