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

public class ReceiverTest {

	public static void main(String[] args) throws IOException {
		MulticastSocket s = new MulticastSocket(2225);
		System.out.println("Created socket");
		s.joinGroup(InetAddress.getByName("225.2.2.5"));
		System.out.println("Joined group");
		byte[] buf = new byte[1024];
		DatagramPacket pack = new DatagramPacket(buf, buf.length);
		s.receive(pack);
		String response = new String(buf);
		System.out.println("got data: " + response);
		s.close();
	}
	
}
