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

package tests;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

//import net.happybrackets.controller.config.ControllerConfig;

public class BroadcastTest {
	protected TestBroadcastManager broadcastManager;
    protected ControllerConfig config;
	boolean receivedMulticastMessage; // for testSendReceive()

	@Before
	public void setUp() throws Exception {
        System.out.println("BroadcastManager testing setup started at: "
                + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date())
        );
		config            				= new ControllerConfig();
		config							= config.load("src/test/config/test-controller-config.json", config);
		broadcastManager  				= new TestBroadcastManager(config.getMulticastAddr(), config.getBroadcastPort());
		broadcastManager.startRefreshThread();
		receivedMulticastMessage 		= false;

        //setup test listener
        broadcastManager.addPersistentBroadcastListener(new OSCListener() {
            @Override
            public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
                if(msg.getName().equals("/test")) {
                    receivedMulticastMessage = true;
                    System.out.println("Received test message");
                }
            }
        });
        System.out.println("BroadcastManager testing setup finished at: "
                + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date())
        );
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("BroadcastManager testing tearDown started at: "
						+ new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date())
		);

        // leave these to be GCed
//		broadcastManager.clearBroadcastListeners();
//        broadcastManager.dispose();

		System.out.println("BroadcastManager testing tearDown finished at: "
						+ new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date())
		);
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
		while (!receivedMulticastMessage && timeOut < 300) {
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

		System.out.println("Send recieve test ended after: " + (timeOut * 100 * 0.001) + " seconds.");
		assertTrue( receivedMulticastMessage );
	}

    public void testBroadcastRefresh() {
        System.out.println("Simulating interface disconnect in broadcast manager: " + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date()));
        broadcastManager.simulateInterfaceDisconnection();
		System.out.println("Completed refresh: " + new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss:ms").format(new Date()));
        // attempt the send receive test again after refresh
        System.out.println("Attempting send receive again after refersh");
        receivedMulticastMessage = false;
        testSendReceive();
    }

    private class TestBroadcastManager extends BroadcastManager {

        public TestBroadcastManager(String address, int port) {
            super(address, port);
        }

        void simulateInterfaceDisconnection() {
            transmitters.clear();
            receivers.clear();
            netInterfaces.clear();
        }
    }

}
