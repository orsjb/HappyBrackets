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

package net.happybrackets.netutil;

import de.sciss.net.NetUtil;
import de.sciss.net.OSCPacketCodec;
import de.sciss.net.OSCReceiver;

import java.io.IOException;
import java.net.*;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

/**
 * Created by ollie on 26/06/2016.
 */
public class MulticastOSCReceiver
        extends OSCReceiver {

    private MulticastSocket ms;

    public MulticastOSCReceiver(OSCPacketCodec c, InetSocketAddress localAddress)
            throws IOException {
        super(c, UDP, localAddress, true);
    }

    public MulticastOSCReceiver(MulticastSocket ms, String group, int port)
            throws IOException {
        super(OSCPacketCodec.getDefaultCodec(), UDP, new InetSocketAddress(ms.getLocalAddress(), ms.getLocalPort()), false);

        this.ms = ms;
    }

    protected void setChannel(SelectableChannel ch)
            throws IOException {
        throw new IllegalStateException( "setChannel() not available in MulticastOSCReceiver" );
//        synchronized( generalSync ) {
//            if( isListening ) throw new IllegalStateException( NetUtil.getResourceString( "errNotWhileActive" ));
//
//            dch	= (DatagramChannel) ch;
//            if( !dch.isBlocking() ) {
//                dch.configureBlocking( true );
//            }
//            if( dch.isConnected() ) throw new IllegalStateException( NetUtil.getResourceString( "errChannelConnected" ));
//        }
    }

    public InetSocketAddress getLocalAddress()
            throws IOException {
        synchronized (generalSync) {
            if (ms != null) {
                final DatagramSocket ds = ms;
                return getLocalAddress(ds.getLocalAddress(), ds.getLocalPort());
            } else {
                return getLocalAddress(localAddress.getAddress(), localAddress.getPort());
            }
        }
    }

    public void setTarget(SocketAddress target) {
        this.target = target;
    }

    public void connect()
            throws IOException {
//        synchronized( generalSync ) {
//            if( isListening ) throw new IllegalStateException( NetUtil.getResourceString( "errNotWhileActive" ));
//            if( (ms != null) && !ms.isConnected() ) {
//                if( !revivable ) throw new IOException( NetUtil.getResourceString( "errCannotRevive" ));
//                ms = null;
//            }
//            if( ms == null ) {
////                final DatagramChannel newCh = DatagramChannel.open();
////                newCh.socket().bind( localAddress );
//////					dch = newCh;
////                setChannel( newCh );
//                ms.connect(localAddress);       //TODO check
//            }
//        }
    }

    public boolean isConnected() {
        synchronized (generalSync) {
            return ((ms != null) && ms.isConnected());
        }
    }

    protected void closeChannel()
            throws IOException {
        if (ms != null) {
            try {
                ms.close();
            } finally {
                ms = null;
            }
        }
    }

    /**
     * This is the body of the listening thread
     */
    public void run() {
        SocketAddress sender;

        checkBuffer();

        try {
            listen:
            while (isListening) {
                try {
                    byteBuf.clear();

                    //temporary until we work out a better way...
                    // Fixed buffer size limits the maximum size of received packets
                    // Seems there are always limitations and a default going back to the constant DEFAULTBUFSIZE
                    byte[] buffer = new byte[getBufferSize()];
                    DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

                    ms.receive(dp);
                    byteBuf.put(buffer);
                    sender = dp.getSocketAddress();

                    if (!isListening) break listen;
                    if ((target != null)) continue listen;

                    flipDecodeDispatch(sender);
                } catch (ClosedChannelException e1) {    // bye bye, we have to quit
                    if (isListening) {
//							System.err.println( e1 );
                        System.err.println("OSCReceiver.run : " + e1.getClass().getName() + " : " + e1.getLocalizedMessage());
                    }
                    return;
                } catch (IOException e1) {
                    if (isListening) {
                        System.err.println("OSCReceiver.run : " + e1.getClass().getName() + " : " + e1.getLocalizedMessage());
//							System.err.println( new OSCException( OSCException.RECEIVE, e1.toString() ));
                    }
                }
            } // while( isListening )
        } finally {
            synchronized (threadSync) {
                thread = null;
                threadSync.notifyAll();   // stopListening() might be waiting
            }
        }
    }

    protected void sendGuardSignal()
            throws IOException {
        final DatagramSocket guard;
        final DatagramPacket guardPacket;

        guard = new DatagramSocket();
        guardPacket = new DatagramPacket(new byte[0], 0);
        guardPacket.setSocketAddress(getLocalAddress());
        guard.send(guardPacket);
        guard.close();
    }
}