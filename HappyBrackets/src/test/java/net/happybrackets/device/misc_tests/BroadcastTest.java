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

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class BroadcastTest {

	//http://docs.oracle.com/javase/tutorial/networking/datagrams/broadcasting.html	
	
	public static void main(String[] args) throws Exception {
		new BroadcastTest();
	}
	
	public BroadcastTest() throws Exception {
		MulticastSocket socket = new MulticastSocket(4446);
		InetAddress group = InetAddress.getByName("203.0.113.0");
		socket.joinGroup(group);

		DatagramPacket packet;
		for (int i = 0; i < 5; i++) {
		    byte[] buf = new byte[256];
		    packet = new DatagramPacket(buf, buf.length);
		    socket.receive(packet);

		    String received = new String(packet.getData());
		    System.out.println("Quote of the Moment: " + received);
		}

		socket.leaveGroup(group);
		socket.close();
	}
}
