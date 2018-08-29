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
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TimeZone;

import static org.junit.Assert.assertTrue;

/**
 * This module will generate the version information
 * The version file will contain major, minor and build
 * the COMPILE_DATE_FILE_TEXT is stored in the HB.jar resource.
 * It is incremented each time the module runs, except where the version has changed
 * in which case it will be set to zero
 *
 * So our version 3.0.0.1 has the first three numbers from VERSION_FILENAME,
 * the last digit is from COMPILE_DATE_FILE_TEXT
 *
 * we will also write the complete version and compile number so we can store it
 * into the plugin in gradle
 */
public class VersionTest {


	final String BUILD_PATH = "build/libs/";

	final String MINOR_BUILD_FILE = BUILD_PATH + "minorbuild.txt";

	final String VERSION_FILENAME = BUILD_PATH + BuildVersion.VERSION_FILE;

	// note that we are going to write the compile number straight to resource file due to gradle task ordering
	final String COMPILE_DATE_FILE_TEXT = "src/main/resources/" + BuildVersion.BUILD_COMPILE_NUM_FILE;

	// we will put the date the version is created
	final String VERSION_DATE_FILE_TEXT = "src/main/resources/" + BuildVersion.BUILD_VERSION_DATE;


	@Test
    public void writeVersion() {

		int days_since_version = 0;

		long daysSinceEpoch =  new Date().getTime() /1000 / 60 / 60 / 24;

		String version_date_text = "" + daysSinceEpoch;

		System.out.println(daysSinceEpoch);
		// First read our existing recorded version
		String version_text =  "";

		try {
			Scanner in = new Scanner(new FileReader(MINOR_BUILD_FILE));
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) {
				sb.append(in.next());
			}
			in.close();
			version_text = sb.toString();
		}catch (Exception ex) {

		}

		// let us read the date the version was updated
		try {
			Scanner in = new Scanner(new FileReader(VERSION_DATE_FILE_TEXT));
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) {
				sb.append(in.next());
			}
			in.close();
			version_date_text = sb.toString();
		}catch (Exception ex) {
		}

		// See if our recorded version is equal to current version
		if (version_text.equalsIgnoreCase(BuildVersion.getVersionText())){
			// They are the same so we will change the days_since_version based on date difference

			// see if we have recorded a compile date. If not, we will leave as zero
			try {
				Scanner in = new Scanner(new FileReader(VERSION_DATE_FILE_TEXT));
				StringBuilder sb = new StringBuilder();
				while(in.hasNext()) {
					sb.append(in.next());
				}
				in.close();
				// let us read the build date in
				long build_date = Long.parseLong(sb.toString());
				long date_diff =  daysSinceEpoch - build_date;
				days_since_version = (int)date_diff;

			}catch (Exception ex) {

			}
		}
		else
		{
			// Existing is not equal to recorded. Make it our current\version
			// note our days_since_version will be zero
			version_text = BuildVersion.getVersionText();
			version_date_text = "" + daysSinceEpoch;
		}

		// Write our version to file
		try {
			PrintWriter version_file = new PrintWriter(MINOR_BUILD_FILE);
			version_file.print(version_text);
			version_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		// Write our version Date to file
		try {
			PrintWriter version_file = new PrintWriter(VERSION_DATE_FILE_TEXT);
			version_file.print(version_date_text);
			version_file.close();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		// write our compile number which will be stored as a resource
		try {
			PrintWriter version_file = new PrintWriter(COMPILE_DATE_FILE_TEXT);
			version_file.print("" + days_since_version);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		// now write the combination of version and compile to file
		try {
			PrintWriter version_file = new PrintWriter(VERSION_FILENAME);
			version_file.print(version_text + "." + days_since_version);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		System.out.println(version_text + "." + days_since_version);


    }


}
