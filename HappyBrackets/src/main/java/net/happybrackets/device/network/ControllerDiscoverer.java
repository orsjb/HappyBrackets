package net.happybrackets.device.network;

import net.happybrackets.core.BroadcastManager;
import net.happybrackets.device.config.DeviceController;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import java.net.SocketAddress;

public interface ControllerDiscoverer {

	default void listenForController(DeviceController controller, BroadcastManager broadcastManager) {
		broadcastManager.addBroadcastListener(new OSCListener(){
			public void messageReceived(OSCMessage msg, SocketAddress sender, long time) {
				if (msg.getName().equals("/hb/controller") && msg.getArgCount() > 0) {
					controller.setAddress( (String) msg.getArg(1) );
					controller.setHostname( (String) msg.getArg(0) );
				}
			}
		});

	}

}
