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

package net.happybrackets.controller.misc_tests;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//import de.sciss.net.OSCChannel;
import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import de.sciss.net.OSCServer;

//NOTE: to run on master server

public class OldConnectionServer {

	//DEBUG: entry point
	public static void main(String[] args) {
		try {
			OldConnectionServer cs = new OldConnectionServer();
			cs.beginMonitorClients();
			//Keep this thread busy to avoid program termination
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class client {
		String MAC;
		String name;
		int id;
		SocketAddress addr;
		long lastAliveT; //The time when the client sent its most recent alive message
		boolean alive;
	}

	private static final String namesConfPath = "conf/names.conf";
	//The time between receiving an alive message from a client and marking
	//that client as having a bad connection to the server.
	private static final int integrityTimeout = 3300;
	//The time between receiving an alive message and removing that client
	//from the list of connected clients
	private static final int clientTimeout = 15300;

	public static final int sendPort = 2225;
	public static final int receivePort = 2224;

	private OSCServer oscs;
	private HashMap<String, String> namesMap;
	private ArrayList<client> clients; //List of connected clients
	private Object clientsLock;
	private Thread clientMonWorker;
	private boolean monitoringClients;

	public OldConnectionServer() throws IOException {
		clients = new ArrayList<client>();

		namesMap = new HashMap<String, String>();
		Scanner scanner = new Scanner(new File(namesConfPath));
		while (scanner.hasNext()) {
			namesMap.put(scanner.next(), scanner.next());
		}

		oscs = OSCServer.newUsing(OSCServer.UDP, receivePort);
		oscs.start();

		oscs.addOSCListener(new OSCListener() {
			public void messageReceived(OSCMessage m, SocketAddress sender, long time) {
				String mName = m.getName();
				Object firstArg = m.getArg(0);
				InetSocketAddress sendAddress = new InetSocketAddress(((InetSocketAddress)sender).getHostName(), sendPort);

				if (mName.equals("/PI/alive/announce") && m.getArgCount() == 1) {
					synchronized (clientsLock) {
						for (client c : clients) {
							if (firstArg.equals(c.name)) {
								//Respond so that the client knows if it has a good connection to the server
								try {
									oscs.send( new OSCMessage("/PI/alive/acknowledge", new Object[] {c.name}), sendAddress);
								} catch (IOException e) { }
								c.alive = true;
								c.lastAliveT = System.currentTimeMillis();
								break;
							}
						}
					}
				}
				else if (mName.equals("/PI/hshake/announce")) {
					synchronized (clientsLock) {
						if (namesMap.containsKey(firstArg)) {
							//Make sure the client hasn't already been added
							boolean already = false;
							client recipient = new client();
							for (client c : clients) {
								if (c.MAC.equals(firstArg)) {
									already = true;
									recipient = c;
									break;
								}
							}

							if (!already) {
								//Add new client
								recipient.MAC = (String)firstArg;
								recipient.name = namesMap.get(firstArg);
								recipient.id = 0; //TODO: assign id
								recipient.addr = sender;
								recipient.lastAliveT = System.currentTimeMillis();
								recipient.alive = true;
								clients.add(recipient);
							}

							try {
								//Even if the client has already been added, respond with name anyway.
								oscs.send( new OSCMessage("/PI/hshake/respond", new Object[] {recipient.MAC, recipient.name}), sendAddress);
							} catch (IOException e) { }
						}
					}
				}
			}
		});

		//DEBUG:
		//oscs.dumpOSC( OSCChannel.kDumpBoth, System.out );

		clientsLock = new Object();
		monitoringClients = false;

		clientMonWorker = new Thread() {
			public void run() {
				synchronized(clientsLock) {
					while (monitoringClients) {
						long t = System.currentTimeMillis();
						boolean removeClient = false;
						client forRemoval = null;
						for (client c : clients) {
							if (t - c.lastAliveT >= clientTimeout)
							{
								removeClient = true;
								forRemoval = c;
							}
							else if (t - c.lastAliveT >= integrityTimeout)
								c.alive = false;
						}
						if (removeClient)
							clients.remove(forRemoval);

						//List connected clients (run in bash)
						System.out.print("\033[0;0HDynamic PI Master Server - Connected Clients\033[K\n");
						System.out.print(displayClients());
						//Clear some lines
						for (int i = 0; i < 15; i++)
							System.out.print("\033[K\n");

						try {
							clientsLock.wait(900);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
	}

	public String displayClients() {
		StringBuilder builder = new StringBuilder();
		builder.append("MAC Address       | IP Address      | Name              | ID | Connection\n");
		builder.append("-------------------------------------------------------------------------\n");

		for (client c : clients) {
			String ip = ((InetSocketAddress)c.addr).getAddress().getHostAddress();
			builder.append(String.format("%s | %-15s | %-17s | %-2s | %s\n", c.MAC, ip, c.name, c.id, c.alive ? "good" : "bad "));
		}

		return builder.toString();
	}

	public void beginMonitorClients() {
		monitoringClients = true;
		clientMonWorker.start();
	}

	public void endMonitorClients() {
		synchronized (clientsLock) {
			monitoringClients = false;
		}
	}
}