package pi.test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SenderTest {

	public static void main(String[] args) throws IOException {
		MulticastSocket s = new MulticastSocket();
		byte buf[] = "This is my message!".getBytes();
		// Create a DatagramPacket 
		DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName("225.2.2.5"), 2225); 
		s.send(pack); 
		// And when we have finished sending data close the socket
		s.close();
	}

}
