package net.happybrackets.core.misc_tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.controller.config.ControllerConfig;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import static org.junit.Assert.*;

public class BroadcastTest {
	protected BroadcastManager broadcastManager;
  protected ControllerConfig config;
	boolean receivedMulticastMessage; // for testSendReceive()

	@Before
	public void setUp() throws Exception {
        Date date = new Date();
        System.out.println("BroadcastManager testing setup started at: "
                + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(date)
        );
		config            				= new ControllerConfig();
		config							= config.load("src/test/config/test-controller-config.json", config);
		broadcastManager  				= new BroadcastManager(config.getMulticastAddr(), config.getBroadcastPort());
		receivedMulticastMessage 		= false;

        //setup test listener
        broadcastManager.addBroadcastListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if(msg.getName().equals("/test")) {
                    receivedMulticastMessage = true;
                    System.out.println("Received test message");
                }
            }
        });
        date = new Date();
        System.out.println("BroadcastManager testing setup finished at: "
                + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(date)
        );
	}

	@After
	public void tearDown() throws Exception {
		broadcastManager.clearBroadcastListeners();
        broadcastManager.dispose();
	}

	@Test
    public void testsOrdered() {
        //setup a running order for our tests
        testSendReceive();
        testBroadcastRefresh();
    }

	public void testSendReceive() {
		int	timeOut	= 0;

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
				System.err.println("Sleep was interrupted during BroadcastTest.");
				e.printStackTrace();
			}
		}

		assertTrue( receivedMulticastMessage );
	}

    public void testBroadcastRefresh() {
        System.out.println("Refreshing broadcast manager");
        broadcastManager.refreshBroadcaster();
        // attempt the send receive test again after refresh
        System.out.println("Attempting send recive again after refersh");
        receivedMulticastMessage = false;
        testSendReceive();
    }

}
