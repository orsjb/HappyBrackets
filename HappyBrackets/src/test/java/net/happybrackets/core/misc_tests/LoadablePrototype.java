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

package net.happybrackets.core.misc_tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.google.gson.Gson;
import net.happybrackets.core.config.EnvironmentConfig;
import net.happybrackets.core.config.LoadableConfig;

public class LoadablePrototype implements EnvironmentConfig {
	
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
		System.out.println("KeepAliveInterval: " + cfg.getBroadcastPort());
	}

}
