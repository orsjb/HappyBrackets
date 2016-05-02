package net.happybrackets.core.misc_tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;
import core.EnvironmentConf;
import core.LoadableConfig;

public class LoadablePrototype implements EnvironmentConf {
	
	class MockClass extends LoadableConfig {
		
	}
	
	MockClass cfg;
	
	public static void main(String[] args) {
		String fileName = "config/misc_tests-controller-config.json";
		System.out.println("Loading: " + fileName);
		File f = new File(fileName);
		if ( !f.isFile() ) {
			System.err.println("File: '" + f.getAbsolutePath() + "' does not exist!");
			System.exit(1);
		}
		
		MockClass cfg = null;
		Gson gson = new Gson();
		
		try {
			BufferedReader br = new BufferedReader( new FileReader(f.getAbsolutePath() ));
			cfg = gson.fromJson(br, MockClass.class);
		}
		catch (IOException e) {
			System.out.println("Unable to open file: " + fileName);
			e.printStackTrace();
		}
		
		if (cfg == null) {
			System.err.println("Failed loading config file: " + fileName);
			System.exit(1);
		}
		
		System.out.println("KeepAliveInterval: " + cfg.getAliveInterval());
		System.out.println("KeepAliveInterval: " + cfg.getBroadcastOSCPort());
	}

}
