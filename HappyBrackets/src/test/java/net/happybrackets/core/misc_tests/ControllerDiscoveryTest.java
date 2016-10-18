package net.happybrackets.core.misc_tests;

import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.Device;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.BroadcastManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.NetworkInterface;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ControllerDiscoveryTest {
	protected DeviceConfig 					deviceEnv;
	protected ControllerConfig 			controllerEnv;
	protected ControllerAdvertiser 	advertiser;
	protected BroadcastManager			broadcastManager;

	@Before
	public void setUp() throws Exception {
		deviceEnv = new DeviceConfig();
		deviceEnv = deviceEnv.load("src/test/config/test-device-config.json", deviceEnv);

		controllerEnv = new ControllerConfig();
		controllerEnv = controllerEnv.load("src/test/config/test-controller-config.json", controllerEnv);

		//we need a broadcast manager for testing but we can only run one at a time
		broadcastManager = new BroadcastManager(controllerEnv.getMulticastAddr(), controllerEnv.getBroadcastPort());

		advertiser = new ControllerAdvertiser(broadcastManager);
		advertiser.start();

		deviceEnv.listenForController(broadcastManager);
	}

	@After
	public void tearDown() throws Exception {
		advertiser.stop();
	}

	@Test
	public void testGetControllerHostname() {
        ArrayList<String> controllerHostName = new ArrayList<>();
        try {
            Device.viableInterfaces().forEach( ni -> controllerHostName.add(Device.selectHostname(ni)) );
        } catch (Exception e) {
            System.err.println("Error retrieving host name!");
            e.printStackTrace();
        }

		//give some time to catch the name of the controller
		try {
			Thread.sleep(500);
		}
		catch (InterruptedException e) {
			System.err.println("Sleep was interupted during ControllerDiscoveryTest.");
			e.printStackTrace();
		}

		assertTrue( advertiser.isAlive() );
		assertNotNull( deviceEnv.getControllerHostname() );
        //stop advertising so we do not change mid test
        advertiser.stop();
        //check that one of our host names matches
        assertTrue(
                controllerHostName.stream().anyMatch(name -> deviceEnv.getControllerHostname().equals(name))
        );
		System.out.println("Found controller: " + deviceEnv.getControllerHostname());
	}

}
