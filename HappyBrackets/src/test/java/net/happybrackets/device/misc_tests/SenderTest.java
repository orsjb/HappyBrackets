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

package net.happybrackets.device.misc_tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SenderTest {

	public static void main(String[] args) throws IOException {
		MulticastSocket s = new MulticastSocket();
		byte buf[] = "This is my message!".getBytes();
		// Create a DatagramPacket 
		DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName("225.2.2.5"), 2225); 
		s.send(pack); 
		// And when we have finished sending data close the socket
		s.close();
	}

}
