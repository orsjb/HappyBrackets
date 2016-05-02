package pi.test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastTest {

	//http://docs.oracle.com/javase/tutorial/networking/datagrams/broadcasting.html	
	
	public static void main(String[] args) throws Exception {
		new BroadcastTest();
	}
	
	public BroadcastTest() throws Exception {
		MulticastSocket socket = new MulticastSocket(4446);
		InetAddress group = InetAddress.getByName("203.0.113.0");
		socket.joinGroup(group);

		DatagramPacket packet;
		for (int i = 0; i < 5; i++) {
		    byte[] buf = new byte[256];
		    packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);

		    String received = new String(packet.getData());
		    System.out.println("Quote of the Moment: " + received);
		}

		socket.leaveGroup(group);
		socket.close();
	}
}
