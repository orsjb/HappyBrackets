package net.happybrackets.device.network;

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.config.DeviceController;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import org.slf4j.Logger;

import java.net.SocketAddress;

public interface ControllerDiscoverer {

	default void listenForController(DeviceController controller, BroadcastManager broadcastManager, Logger logger) {
		broadcastManager.addBroadcastListener(new OSCListener(){
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				if (msg.getName().equals("/hb/controller") && msg.getArgCount() > 0) {
                    String advertisedAddress = (String) msg.getArg(1);
                    String advertisedHostname = (String) msg.getArg(0);
                    if (!( controller.getAddress().equals(advertisedAddress) && controller.getHostname().equals(advertisedHostname) )) {
                        controller.setAddress(advertisedAddress);
                        controller.setHostname(advertisedHostname);
                        logger.debug("Updated controller to {} at {}", controller.getHostname(), controller.getAddress());
                    }
				}
			}
		});

	}

}
