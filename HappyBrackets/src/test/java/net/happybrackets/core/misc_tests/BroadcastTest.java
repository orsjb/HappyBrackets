package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.happybrackets.BroadcastManager;
import net.happybrackets.controller.config.ControllerConfig;

import de.sciss.net.OSCListener;

public class BroadcastTest {
	protected BroadcastManager broadcastManager;
  protected ControllerConfig config;

	@Before
	public void setUp() throws Exception {
		config            = new ControllerConfig();
    broadcastManager  = new BroadcastManager(config);
	}

	@After
	public void tearDown() throws Exception {
		broadcastManager.clearBroadcastListeners();
	}

	@Test
	public void testSendReceive() {
    boolean receivedMulticastMessage 	= false;
		int 		timeOut 									= 0;

		//prepare to recieve a message
		broadcastManager.addBroadcastListener(new OSCListener() {
      @Override
      public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
          if(msg.getName().equals("/test")) {
              receivedMulticastMessage == true;
          }
      }
    });

		//send messages until we catch one
		while (!receivedMulticastMessage && timeOut < 300) {
			timeOut++;
			broadcastManager.broadcast("/test");
		}

		assert( receivedMulticastMessage );
	}

}
