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

import javax.sound.sampled.*;

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
public class TestAudio {


	@Test
	public void writeVersion() {

		Mixer mixer = AudioSystem.getMixer(null); // default mixer
		try {
			mixer.open();

			System.out.printf("Supported SourceDataLines of default mixer (%s):\n\n", mixer.getMixerInfo().getName());
			for (Line.Info info : mixer.getSourceLineInfo()) {
				if (SourceDataLine.class.isAssignableFrom(info.getLineClass())) {
					SourceDataLine.Info info2 = (SourceDataLine.Info) info;
					System.out.println(info2);
					System.out.printf("  max buffer size: \t%d\n", info2.getMaxBufferSize());
					System.out.printf("  min buffer size: \t%d\n", info2.getMinBufferSize());
					AudioFormat[] formats = info2.getFormats();
					System.out.println("  Supported Audio formats: ");
					for (AudioFormat format : formats) {
						System.out.println("    " + format);
//          System.out.printf("      encoding:           %s\n", format.getEncoding());
//          System.out.printf("      channels:           %d\n", format.getChannels());
//          System.out.printf(format.getFrameRate()==-1?"":"      frame rate [1/s]:   %s\n", format.getFrameRate());
//          System.out.printf("      frame size [bytes]: %d\n", format.getFrameSize());
//          System.out.printf(format.getSampleRate()==-1?"":"      sample rate [1/s]:  %s\n", format.getSampleRate());
//          System.out.printf("      sample size [bit]:  %d\n", format.getSampleSizeInBits());
//          System.out.printf("      big endian:         %b\n", format.isBigEndian());
//
//          Map<String,Object> prop = format.properties();
//          if(!prop.isEmpty()) {
//              System.out.println("      Properties: ");
//              for(Map.Entry<String, Object> entry : prop.entrySet()) {
//                  System.out.printf("      %s: \t%s\n", entry.getKey(), entry.getValue());
//              }
//          }
					}
					System.out.println();
				} else {
					System.out.println(info.toString());
				}
				System.out.println();
			}


		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}

	}

}
