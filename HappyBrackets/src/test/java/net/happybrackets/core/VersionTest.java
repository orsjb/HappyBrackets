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

package net.happybrackets.core;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.happybrackets.controller.config.ControllerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class VersionTest {
	String VERSION_FILENAME = "build/libs/HBVersion.txt";
	
	@Test
    public void writeVersion() {

		// Get the version details
		String version_text = BuildVersion.getVersionText();
		
		try {
			PrintWriter version_file = new PrintWriter(VERSION_FILENAME);
			version_file.print(version_text);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		System.out.println(version_text);


    }


}
