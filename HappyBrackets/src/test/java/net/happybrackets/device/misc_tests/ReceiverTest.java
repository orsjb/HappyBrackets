package net.happybrackets.device.misc_tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReceiverTest {

	public static void main(String[] args) throws IOException {
		MulticastSocket s = new MulticastSocket(2225);
		System.out.println("Created socket");
		s.joinGroup(InetAddress.getByName("225.2.2.5"));
		System.out.println("Joined group");
		byte[] buf = new byte[1024];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		s.receive(pack);
		String response = new String(buf);
		System.out.println("got data: " + response);
		s.close();
	}
	
}
