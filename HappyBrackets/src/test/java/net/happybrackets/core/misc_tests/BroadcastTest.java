package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.controller.config.ControllerConfig;

import java.net.SocketAddress;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import static org.junit.Assert.*;

public class BroadcastTest {
	protected BroadcastManager broadcastManager;
  protected ControllerConfig config;
	boolean receivedMulticastMessage; // for testSendReceive()

	@Before
	public void setUp() throws Exception {
		config            				= new ControllerConfig();
		config							= config.load("src/test/config/test-controller-config.json", config);
		broadcastManager  				= new BroadcastManager(config.getMulticastAddr(), config.getBroadcastPort());
		receivedMulticastMessage 		= false;
	}

	@After
	public void tearDown() throws Exception {
		broadcastManager.clearBroadcastListeners();
	}

	@Test
	public void testSendReceive() {
		int	timeOut	= 0;

		//prepare to recieve a message
		broadcastManager.addBroadcastListener(new OSCListener() {
      @Override
      public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
          if(msg.getName().equals("/test")) {
              receivedMulticastMessage = true;
							System.out.println("Recieved test message");
          }
      }
    });

		//send messages until we catch one
		while (!receivedMulticastMessage && timeOut < 30) {
			timeOut++;
			System.out.println("Sending test broadcast " + timeOut);
			broadcastManager.broadcast("/test");
			//sleep for 100ms
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				System.err.println("Sleep was interupted during BroadcastTest.");
				e.printStackTrace();
			}
		}

		assertTrue( receivedMulticastMessage );
	}

}
