package net.happybrackets.core;

import de.sciss.net.*;

import java.io.IOException;
import java.net.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Simple UDP Receiver without all the overhead. Opens a socket and reads it
 */
public class StaticUDPReceiver {
    private final List collListeners   = new ArrayList();

    interface UDPPacketListener{
        void messageReceived(DatagramPacket packet, SocketAddress sender );
    }

    ServerSocket serverSocket;
    DatagramSocket receiver = null;

    final Object listenerLock = new Object();

    volatile boolean doListen = false;
    /**
     * Create a Receiver port
     * @param port the port to receive
     * @throws SocketException Exception if Unable to open Socket
     */
    public StaticUDPReceiver(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        port = serverSocket.getLocalPort();
        receiver =  new DatagramSocket(port);
        receiver.setReuseAddress(true);
    }

    public void close() throws IOException {
        if(doListen) {
            doListen = false;
            serverSocket.close();
            receiver.close();
        }
    }

    /**
     * Create a OSC Receiver finding own port
     * @throws SocketException Exception if unable to open
     */
    public StaticUDPReceiver() throws IOException {
        this(0);

    }

    /**
     *  Registers a listener that gets informed
     *  about incoming messages. You can call this
     *  both when listening was started and stopped.
     *
     *  @param  listener	the listener to register
     */
    public void addListener( UDPPacketListener listener )
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
    public void removeListener( UDPPacketListener listener )
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


            dispatchPacket(packet, packet.getSocketAddress());

        }

        serverSocket.close();
        receiver.close();

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
                } catch (SocketException e){
                    if(e.getMessage().equals("Socket closed")) {
                        // When the connection is closed, do nothing
                    } else {
                        e.printStackTrace();
                    }
                }
                catch (IOException e) {
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
     * @param packet the Datagram Packet
     * @param sender the sender
     */
    private void dispatchPacket(DatagramPacket packet, SocketAddress sender)
    {
        synchronized(listenerLock) {
            for( int i = 0; i < collListeners.size(); i++ ) {
                UDPPacketListener listener = (UDPPacketListener) collListeners.get( i );
                try {
                    listener.messageReceived( packet, sender);
                }
                catch( RuntimeException e1 ) {
                    e1.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {

        try {

            StaticUDPReceiver receiver = new StaticUDPReceiver(2222);

            receiver.start();
            receiver.addListener((packet, sender) -> {
                System.out.println("Message received from " + sender.toString());
            });

            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
