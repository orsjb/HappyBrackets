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
import java.util.Date;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class VersionTest {
	String VERSION_FILENAME = "build/libs/HBVersion.txt";
	String DATE_FILE_TEXT = "build/libs" + BuildVersion.BUILD_COMPILE_NUM_FILE;
	@Test
    public void writeVersion() {

		int build_number = 0;

		String existing_version_text =  "";
		try {
			Scanner in = new Scanner(new FileReader(VERSION_FILENAME));
			StringBuilder sb = new StringBuilder();
			while(in.hasNext()) {
				sb.append(in.next());
			}
			in.close();
			existing_version_text = sb.toString();
		}catch (Exception ex) {

		}

		// Get the version details
		String version_text = BuildVersion.getVersionText();

		if (version_text.equalsIgnoreCase(existing_version_text)){
			// we will increment the build number

			try {
				Scanner in = new Scanner(new FileReader(DATE_FILE_TEXT));
				StringBuilder sb = new StringBuilder();
				while(in.hasNext()) {
					sb.append(in.next());
				}
				in.close();
				build_number = Integer.parseInt(sb.toString()) + 1;
			}catch (Exception ex) {

			}
		}
		try {
			PrintWriter version_file = new PrintWriter(VERSION_FILENAME);
			version_file.print(version_text);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}


		try {
			PrintWriter version_file = new PrintWriter(DATE_FILE_TEXT);
			version_file.print("" + build_number);
			version_file.close();


		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}

		System.out.println(version_text + "." + build_number);


    }


}
