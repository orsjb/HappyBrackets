package pi.test;

import java.io.IOException;
import java.net.Socket;

public class SendToPITest {

	public static void main(String[] args) {
		String hostname = "raspberrypi.local";
		try {
			Socket s = new Socket(hostname, 1234);
			s.getOutputStream().write(new byte[]{0,1});
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
