package net.happybrackets.core;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Simple OSC Receiver without all the overhead. Opens a socket and reads it
 */
public class OSCUDPReceiver implements StaticUDPReceiver.UDPPacketListener{
    private final List collListeners   = new ArrayList();

    static Map<Integer, StaticUDPReceiver> activeSockets = new Hashtable<>();

    static Set<OSCUDPReceiver> resetableReceivers = new HashSet<>();

    StaticUDPReceiver udpReceiver;

    OSCPacketCodec codec = new OSCPacketCodec();
    ByteBuffer byteBuf = ByteBuffer.allocateDirect(OSCChannel.DEFAULTBUFSIZE);

    final boolean disableReset;

    final Object listenerLock = new Object();

    /**
     * Resets all listeners
     */
    public static void resetListeners(){
        for (OSCUDPReceiver receiver:
                resetableReceivers) {

            receiver.eraseListeners();
            receiver.udpReceiver.removeListener(receiver::messageReceived);
        }
        resetableReceivers.clear();
    }

    volatile boolean doListen = false;
    /**
     * Create a Receiver port
     * @param port the port to receive
     * @param disableReset setting true will disable reset ability
     * @throws SocketException Exception if Unable to open Socket
     */
    public OSCUDPReceiver(int port, boolean disableReset) throws IOException {
        this.disableReset = disableReset;
        if (activeSockets.containsKey(port)){
            udpReceiver = activeSockets.get(port);
        }
        else
        {
            udpReceiver = new StaticUDPReceiver(port);
            activeSockets.put(udpReceiver.getPort(), udpReceiver);
            udpReceiver.start();
        }

        resetableReceivers.add(this);
        udpReceiver.addListener(this::messageReceived);
    }

    /**
     * Create a Receiver port
     * @param port the port to receive
     * @throws SocketException Exception if Unable to open Socket
     */
    public OSCUDPReceiver(int port) throws IOException {
        this(port, false);
    }
    /**
     * Create a OSC Receiver finding own port
     * @param disableReset setting true will disable reset ability
     * @throws SocketException Exception if unable to open
     */
    public OSCUDPReceiver(boolean disableReset) throws IOException {
        this(0, disableReset);

    }

    /**
     * Create a OSC Receiver finding own port
     * @throws SocketException Exception if unable to open
     */
    public OSCUDPReceiver() throws IOException {
        this(0, false);

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
     * Erases listener list
     */
    public void eraseListeners()
    {
        synchronized(listenerLock) {
            collListeners.clear();
        }
    }

    /**
     * Return the port for this server
     * @return the receive port
     */
    public int getPort(){
        int ret = 0;
        if (udpReceiver != null){
            ret = udpReceiver.getPort();

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

            OSCUDPReceiver receiver = new OSCUDPReceiver(2222, true);

            receiver.addOSCListener((msg, sender, time) -> {
                System.out.println(msg.getName());
            });
            //receiver.doReceive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(DatagramPacket packet, SocketAddress sender) {
        byteBuf.clear();
        byteBuf.put(packet.getData(), 0, packet.getLength());
        final OSCPacket p;

        try {
            byteBuf.flip();

            try {
                p = codec.decode( byteBuf );
                dispatchPacket(p, packet.getSocketAddress(), OSCBundle.NOW);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // OSCBundles will override this dummy time tag


        }
        catch( BufferUnderflowException e1 ) {
            System.err.println( new OSCException( OSCException.RECEIVE, e1.toString() ));
        }
    }
}
