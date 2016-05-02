package pi;

import core.LoadableConfig;
import core.PIConfig;

public class ManagerAgent {

	public static void main(String[] args) {
		PIConfig env = new PIConfig();
		env = LoadableConfig.load("config/pi-config.json", env);
		System.out.println("PI Manager Agent Started as: " + env.getMyHostName());
		System.out.println("Listening for Controller...");
		System.out.println("Found controller on host: " + env.getControllerHostname());
		
		System.out.println("Requesting configuration settings from controller...");

		System.out.println("PI Manager Agent Exiting");
	}

}
