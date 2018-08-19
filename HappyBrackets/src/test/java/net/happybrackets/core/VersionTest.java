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


import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * This module will generate the version information
 * The version file will contain major, minor and build
 * the DATE_FILE_TEXT is stored in the HB.jar resource.
 * It is incremented each time the module runs, except where the version has changed
 * in which case it will be set to zero
 *
 * So our version 3.0.0.1 has the first three numbers from VERSION_FILENAME,
 * the last digit is from DATE_FILE_TEXT
 *
 * we will also write the complete version and compile number so we can store it
 * into the plugin in gradle
 */
public class VersionTest {
	final String BUILD_PATH = "build/libs/";

	final String VERSION_FILENAME = BUILD_PATH + BuildVersion.VERSION_FILE;

	// note that we are going to write the compile number straight to resource file due to gradle task ordering
	final String DATE_FILE_TEXT = "src/main/resources/" + BuildVersion.BUILD_COMPILE_NUM_FILE;
	final String PLUGIN_VERSION_TEXT =  BUILD_PATH + BuildVersion.PLIUGIN_VERSION_FILE;

	@Test
    public void writeVersion() {

		int compile_number = 0;

		// First read our existing recorded version
		String version_text =  "";

		try {
			Scanner in = new Scanner(new FileReader(VERSION_FILENAME));
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) {
				sb.append(in.next());
			}
			in.close();
			version_text = sb.toString();
		}catch (Exception ex) {

		}

		// See if our recorded version is equal to current version
		if (version_text.equalsIgnoreCase(BuildVersion.getVersionText())){
			// They are the same so we will increment the compile_number

			// see if we have recorded a compile_number. If not, we will leave as zero
			try {
				Scanner in = new Scanner(new FileReader(DATE_FILE_TEXT));
				StringBuilder sb = new StringBuilder();
				while(in.hasNext()) {
					sb.append(in.next());
				}
				in.close();
				compile_number = Integer.parseInt(sb.toString()) + 1;
			}catch (Exception ex) {

			}
		}
		else
		{
			// Existsing is not equal to recorded. Maxe it our current\version
			// note our compile_number will be zero
			version_text = BuildVersion.getVersionText();
		}

		// Write our version to file
		try {
			PrintWriter version_file = new PrintWriter(VERSION_FILENAME);
			version_file.print(version_text);
			version_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		// write our compile number which will be stored as a resource
		try {
			PrintWriter version_file = new PrintWriter(DATE_FILE_TEXT);
			version_file.print("" + compile_number);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		// now write the combination of version and compile to file
		try {
			PrintWriter version_file = new PrintWriter(PLUGIN_VERSION_TEXT);
			version_file.print(version_text + "." + compile_number);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		System.out.println(version_text + "." + compile_number);


    }


}
