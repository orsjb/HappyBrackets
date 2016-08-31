package net.happybrackets.controller.network;

import net.happybrackets.controller.config.ControllerConfig;
import net.happybrackets.core.BroadcastManager;

public class ControllerAdvertiser {
	private Thread advertismentService;
	private boolean keepAlive = true;

	public ControllerAdvertiser(BroadcastManager broadcastManager, String controllerName) {
		//set up an indefinite thread to advertise the controller
		advertismentService = new Thread() {
			public void run() {
				while (keepAlive) {
					broadcastManager.broadcast("/hb/controller", controllerName);

					try {
						Thread.sleep(500);
					}
					catch (InterruptedException e) {
						System.err.println("Sleep was interupted in ControllerAdvertiser thread");
						e.printStackTrace();
					}
				}
			}
		};
	}

	public void start() {
		keepAlive = true;
		advertismentService.start();
	}

	public void stop() {
		keepAlive = false;
	}

	public void interrupt() {
		advertismentService.interrupt();
	}

	public boolean isAlive() {
		return advertismentService.isAlive();
	}
}
