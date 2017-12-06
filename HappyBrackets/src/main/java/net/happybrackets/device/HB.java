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

package net.happybrackets.device;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.PolyLimit;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.DynamicControlScreen;
import net.happybrackets.core.*;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.device.dynamic.DynamicClassLoader;
import net.happybrackets.device.network.NetworkCommunication;
import net.happybrackets.device.sensors.*;
import net.happybrackets.device.config.DeviceConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HB is the main controller class for a HappyBrackets program. It is accessed from an {@link HBAction}'s {@link HBAction#action(HB)} method, where users can play sounds, create network listeners, send network messages, and perform other actions.
 */
public class HB {

	private static boolean enableSimulators = false;

	/**
	 * Return whether we will displaysimulator sensors if no hardware sensor is found
	 * @return true if we will display
	 */
	public static boolean isEnableSimulators() {
		return enableSimulators;
	}

	/**
	 * Set  whether we will displaysimulator sensors if no hardware sensor is found
	 * @param enableSimulators true if we want to display simulator controls
	 */
	public static void setEnableSimulators(boolean enableSimulators) {
		HB.enableSimulators = enableSimulators;
	}

		public interface StatusChangedListener{
		void statusChanged(String new_status);
	}

	private List<StatusChangedListener> statusChangedListenerList  = new ArrayList<>();

	// We will create a static one to know whether we are in Debug or run
	// If we are in a PI, this ill already be set when we try to debug
	static HB HBInstance = null;
	/**
	 * Whether we will use encryption in transferring class data
	 * @param enable true if we want to use encryption
	 */
	public void setUseEncryption(boolean enable) {
		useEncryption = enable;
	}

	private boolean useEncryption = false;

	/**
	 * Add Listeners for status change event
	 * @param listener The listener to add
	 */
	public void addStatusChangedListener (StatusChangedListener listener)
	{
		synchronized (statusChangedListenerList)
		{
			statusChangedListenerList.add(listener);
		}
	}

	final static Logger logger = LoggerFactory.getLogger(HB.class);




	/**
	 * The {@link AudioContext} used by HappyBrackets. This is autorun by default from the start script, but that can be controlled by a commandline flag.
	 */
	public final AudioContext ac;

	/**
	 * The {@link Clock} used by HappyBrackets. This is up and running, with a default interval of 500ms and default "ticks per beat" of 16.
	 */
	public final Clock clock;

	/**
	 * The {@link Envelope} that controls the clock interval. Default value is 500ms.
	 */
	public final Envelope clockInterval;

	/**
	 * The {@link PolyLimit} that controls polyphony. This is used for sounds added with the {@link HB#sound(UGen)} method. Default number of voices is 4.
	 */
	public final PolyLimit pl;

	/**
	 * The {@link Envelope} used to control the master gain. Default value is 1.
	 */

	public final Envelope masterGainEnv;


	/**
	 * Audio on status.
	 */
	boolean audioOn = false;

	/**
	 * Status string used to send to the controller periodically.
	 */
	private String status = "No ID set";

	// sensor stuffs
	/**
	 * A {@link Hashtable} to store sensors.
	 */
	public final Hashtable<Class<? extends Sensor>, Sensor> sensors;

	// shared data
	/**
	 * A {@link Hashtable} to store generic objects.
	 */
	public final Hashtable<String, Object> share = new Hashtable<String, Object>();
	private int nextElementID = 0;

	/**
	 * A random number generator for general use.
	 */
	public final Random rng = new Random();

	// network comms stuff

	/**
	 * The {@link NetworkCommunication} object used to communicate with the controller. The important methods are provided directly from {@link HB}, e.g., {@link HB#sendToController(String, Object...)} and {@link HB#addControllerListener(OSCListener)}.
	 */
	public final NetworkCommunication controller;

	/**
	 * The {@link BroadcastManager}object used to communicate with other devices using 1-many broadcasts. The important methods are provided directly from {@link HB}, e.g., {@link HB#broadcast(String, Object...)} and {@link HB#addBroadcastListener(OSCListener)}.
	 */
	public static BroadcastManager broadcast;

	/**
	 * The {@link Synchronizer} object used to manage time synch between devices. The important methods are provided directly from {@link HB}, e.g., {@link HB#doAtTime(Runnable, long)} and {@link HB#getSynchTime()}.
	 */
	public final Synchronizer synch;

	private AccessMode accessMode;
	private int myId = 0;

	/**
	 * Creates the HB.
	 *
	 * @param _ac the {@link AudioContext} for audio.
	 * @throws IOException if any of the core network set up fails. Could happen if port is already in use, or if setting up multicast fails.
	 */
	public HB(AudioContext _ac) throws IOException {
		this(_ac, AccessMode.OPEN, false);
	}

	/**
  	 * Run HB in a debug mode so we can debug sample code in INtelliJ
	 * run the command like this: HB.runDebug(MethodHandles.lookup().lookupClass());
	 * @param action_class The class that we are debugging. use MethodHandles.lookup().lookupClass()
	 * @return true if we are debugging
	 * @throws IOException if there is a problem with the debug
	 */
	public static boolean runDebug(Class<?> action_class) throws Exception {

		boolean ret = false;

		// we will enable simulators of Sensors
		enableSimulators = true;
		if (HBInstance == null) {

			HB.AccessMode mode = HB.AccessMode.LOCAL;
			String[] start_args = new String[]{
					"buf=1024",
					"sr=44100",
					"bits=16",
					"ins=0",
					"outs=1",
					"start=true",
					"access=local"
			};

			HB hb = new HB(AudioSetup.getAudioContext(start_args), mode, false);
			hb.startAudio();

			if (action_class != null) {

				Class<? extends HBAction> incomingClass = null;
				DynamicClassLoader loader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());

				Class<?>[] interfaces = action_class.getInterfaces();
				boolean isHBActionClass = false;
				for (Class<?> cc : interfaces) {
					if (cc.equals(HBAction.class)) {
						isHBActionClass = true;
						break;
					}
				}
				if (isHBActionClass) {
					incomingClass = (Class<? extends HBAction>) action_class;

				} else {
					throw new Exception("Class does not have HBAction");
				}

				if (incomingClass != null) {
					HBAction action = null;
					try {
						action = incomingClass.newInstance();
						action.action(hb);
						ret = true;

						try
						{
							if (incomingClass.isAssignableFrom(Application.class)) {
								// now we try and JavaFX calls
								DynamicControlScreen debugControlsScreen = new DynamicControlScreen("Debug");


								debugControlsScreen.addDynamicControlScreenLoadedListener(new DynamicControlScreen.DynamicControlScreenLoaded() {
									@Override
									public void loadComplete(DynamicControlScreen screen, boolean loaded) {
										// screen load is complete.
										//Now Add all controls
										ControlMap control_map = ControlMap.getInstance();

										List<DynamicControl> controls = control_map.GetSortedControls();

										for (DynamicControl control : controls) {
											if (control != null) {
												debugControlsScreen.addDynamicControl(control);
												debugControlsScreen.show();
											}
										}

										// Now make a listener for Dynamic Controls that are made during the HB Action event
										control_map.addDynamicControlCreatedListener(new ControlMap.dynamicControlCreatedListener() {
											@Override
											public void controlCreated(DynamicControl control) {
												debugControlsScreen.addDynamicControl(control);
												debugControlsScreen.show();
											}
										});

									}
								});

								// now we have a listener to see when stage is made, let us load the stage
								debugControlsScreen.createDynamicControlStage();
							}
							else
							{
								Application.launch(DebugApplication.class);
							}
						}catch (Exception ex)
						{
							System.out.println("Unable to display Dynamic Control Screen in NON JavaFX Application");
						}


					} catch (Exception e) {
						throw new Exception("Error instantiating received HBAction!", e);
					}
				}

			}
		}
		return ret;
	}

	/**
	 * Creates the HB.
	 *
	 * @param _ac the {@link AudioContext} for audio.
	 * @param _am The access mode.
	 * @throws IOException if any of the core network set up fails. Could happen if port is already in use, or if setting up multicast fails.
	 */
	public HB(AudioContext _ac, AccessMode _am)  throws IOException {
		this(_ac, _am, true);
	}

	/**
	 * Creates the HB.
	 *
	 * @param _ac the {@link AudioContext} for audio.
	 * @param _am The access mode.
	 * @param start_network true if we are to start networking
	 * @throws IOException if any of the core network set up fails. Could happen if port is already in use, or if setting up multicast fails.
     */
	public HB(AudioContext _ac, AccessMode _am, boolean start_network) throws IOException {
		ac = _ac;
		// default audio setup (note we don't start the audio context yet)
		masterGainEnv = new Envelope(ac, 0);
		masterGainEnv.addSegment(1, 5000);
		ac.out.setGain(masterGainEnv);
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);

		int poly_limit = 0;
		String multi_cast_address = "::FFFF:225.2.2.5";
		int broadcast_port = 2222;
		int status_port = 2223;

		if (DeviceConfig.getInstance() != null)
		{
			poly_limit = DeviceConfig.getInstance().getPolyLimit();
			multi_cast_address = DeviceConfig.getInstance().getMulticastAddr();
			broadcast_port = DeviceConfig.getInstance().getBroadcastPort();
		}

		pl = new PolyLimit(ac, ac.out.getOuts(), poly_limit);
		pl.setSteal(true);
		ac.out.addInput(pl);
		ac.out.addDependent(clock);
		logger.info("HB audio setup complete.");
		// sensor setup
		sensors = new Hashtable<>();
		System.out.print(".");

		if (start_network) {
			// start network connection
			broadcast = new BroadcastManager(multi_cast_address, broadcast_port);

			controller = new NetworkCommunication(this);
			System.out.print(".");
			synch = Synchronizer.getInstance();
			System.out.print(".");
			DeviceConfig.getInstance().listenForController(broadcast);
			System.out.print(".");

			this.accessMode = _am;
			if (accessMode != AccessMode.CLOSED) {
				// start listening for code
				startListeningForCode();
			}
			System.out.print(".");


			broadcast.startRefreshThread();
			testBleep3();
		}
		else
		{
			synch = null;
			controller = null;
		}
		//notify started (happens immeidately or when audio starts)

		logger.info("HB initialised");

		HBInstance = this;
	}


	/**
	 * Broadcast an {@link OSCMessage} msg over the multicast group.
	 *
	 * @param string the message string to send.
	 * @param args the args to the message.
	 */
	public void broadcast(String string, Object... args) {
		broadcast.broadcast(string, args);
	}

	/**
	 * Send an {@link OSCMessage} to the controller. The controller already responds to certain OSCMessages that are already generated automatically. Best not to interefere with these. This function is only really useful if you are going to modify your controller program.
	 *
	 * @param string the message string to send.
	 * @param args the args of the message.
     */
	public void sendToController(String string, Object... args) {
		controller.send(string, args);
	}
	/**
	 * Add a new {@link OSCListener}, listening to broadcasts.
	 *
	 * @param listener the new {@link OSCListener}.
	 */
	public void addBroadcastListener(OSCListener listener) {
		broadcast.addBroadcastListener(listener);
	}

	/**
	 * Add a new {@link OSCListener}, listening to incoming messages from the controller.
	 * @param listener the new listener.
     */
	public void addControllerListener(OSCListener listener) {
		controller.addListener(listener);
	}

	/**
	 * Causes the audio to start at a given synchronised time on all devices.
	 *
	 * @param time time at which to sync, according to the agreed clock time
     */
	public void syncAudioStart(long time) {
		doAtTime(new Runnable() {
			public void run() {
				startAudio();
				clock.reset();		//if audio is already running, we just reset the clock
			}
		}, time);
	}

	/**
	 * Returns the system time adjusted according to the result of any device synch attempts.
	 *
	 * @return corrected time as long, in millseconds since 1st Jan 1970.
     */
	public long getSynchTime() {
		return synch.correctedTimeNow();
	}

	/**
	 * Causes an action to be implemented at the given, synchronized time.
	 * @param runnable the action to perform.
	 * @param time the time at which to perform the action, in millseconds since 1st Jan 1970.
     */
	public void doAtTime(Runnable runnable, long time) {
		synch.doAtTime(runnable, time);
	}

	/**
	 * Causes audio processing to start. By default, audio runs on startup. This is a commandline flag to {@link DeviceMain}.
	 */
	public void startAudio() {
		ac.start();
		audioOn = true;
	}

	/**
	 * Notifies the device that it should try to pull the device config file from the controller.
	 *
	 * @throws IOException if the HTTP connection fails.
     */
	public void pullConfigFileFromController() throws IOException {
        //getInstance fresh config file
		String configFile = "config/device-config.json";
        String configUrl = "http://" + DeviceConfig.getInstance().getControllerHostname() + ":" + DeviceConfig.getInstance().getControllerHTTPPort() + "/config/device-config.json";
        logger.debug("GET config file: {}", configUrl);
        OkHttpClient client = new OkHttpClient();
        Request request = new okhttp3.Request.Builder()
                .url(configUrl)
                .build();
        Response response = client.newCall(request).execute();
        logger.debug("Saving new config file: {}", configFile);
        Files.write(Paths.get(configFile), response.body().string().getBytes());
        //reload config from file again after pulling in updates
        logger.debug("Reloading config file: {}", configFile);
        DeviceConfig.load(configFile);
	}

	/**
	 * Checks for an exact match to a string.
	 * @param m the message to check.
	 * @param match the string to check for.
     * @return true if match.
     */
	public boolean messageIs(OSCMessage m, String match) {
		return m.getName().equals(match);
	}

	/**
	 * Gets a float arg from an {@link OSCMessage}. Accesses the arg as a float even if the arg is type int.
	 * @param m the message.
	 * @param index the index of the argument.
     * @return a float argument.
     */
	public float getFloatArg(OSCMessage m, int index) {
		float result = 0;
		try {
			result = (float)m.getArg(index);
		} catch(ClassCastException e) {
			result = (int)m.getArg(index);
		}
		return result;
	}

	/**
	 * Produces a single short test bleep on the device. Assumes audio is running.
	 */
	public void testBleep() {
		Envelope e = new Envelope(ac, 0);
		Gain g = new Gain(ac, 1, e);
		WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
		g.addInput(wp);
		pl.addInput(g);
		e.addSegment(0, 10);
		e.addSegment(0.2f, 0);
		e.addSegment(0.2f, 50);
		e.addSegment(0, 10, new KillTrigger(g));
	}

	/**
	 * Produces a short series of 3 bleeps on the device. Assumes audio is running.
	 */
	public void testBleep3() {
		Envelope e = new Envelope(ac, 0);
		Gain g = new Gain(ac, 1, e);
		WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
		g.addInput(wp);
		pl.addInput(g);
		e.addSegment(0, 1000);
		e.addSegment(0.4f, 0);
		e.addSegment(0.4f, 300);
		e.addSegment(0f, 10);
		e.addSegment(0f, 400);
		e.addSegment(0.4f, 0);
		e.addSegment(0.4f, 300);
		e.addSegment(0f, 10);
		e.addSegment(0f, 400);
		e.addSegment(0.4f, 0);
		e.addSegment(0.4f, 600);
		e.addSegment(0, 10, new KillTrigger(g));
	}

	/**
	 * Launches a separate thread that listens for incoming data. When anything looks like an instance of {@link HBAction} it gets loaded and run.
	 */
	private void startListeningForCode() {
		new Thread() {
			public void run() {
				try {
					// socket server (listens to incoming classes)
					ServerSocket server = new ServerSocket(DeviceConfig.getInstance().getCodeToDevicePort());
					//dynamically loads a class from byte[] data. TODO error if we receive two of the same NON-HBAction classes.
					DynamicClassLoader loader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
					int display_count = 0;
					// start socket server listening loop
					while (true) {
						// must reopen socket each time
						Socket s = server.accept();
						InetAddress incomingAddress = s.getInetAddress();
						String incomingIP = incomingAddress.getHostAddress();

						// Check if code is allowed from this address.
						boolean allow = accessMode == AccessMode.OPEN || accessMode == AccessMode.LOCAL && Device.isThisMyIpAddress(incomingAddress);
						if (!allow) {
							logger.error("Code from host IP " + incomingIP + " DISALLOWED because access mode is set to local.");
							continue;
						}
						logger.debug("Code from host IP " + incomingIP + " ALLOWED.");

						setStatus("Received class load request");
						Class<? extends HBAction> incomingClass = null;
						try {
							InputStream input = s.getInputStream();
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();
							int data = input.read();
							while (data != -1) {
								buffer.write(data);
								data = input.read();
							}

							byte[] dataRaw = buffer.toByteArray();
							byte[] classData;

							display_count ++;

							if (useEncryption) {
								setStatus("Decrypting " + TextOutput.getProgressChar(display_count));
								try {
									classData = Encryption.decrypt(DeviceConfig.getInstance().getEncryptionKey(), dataRaw, 32, dataRaw.length - 32 - Encryption.getIVLength());
								} catch (Exception e) {
									setStatus("Error decrypt class");
									logger.error("Error decrypting received class. Check that the encryptionKey in this device's configuration and the controller's configuration match.");
									throw e;
								}

								// Check given hash matches hash of (decrypted) data.
								MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
								byte[] hash = sha256.digest(classData);
								for (int i = 0; i < hash.length; i++) {
									if (hash[i] != dataRaw[i]) {
										setStatus("Error hash mismatch");
										throw new Exception("Hash mismatch for received class data.");
									}
								}
								logger.debug("Received class data hash matches given hash.");

							}
							else
							{
								setStatus("Loading " + TextOutput.getProgressChar(display_count));
								classData = dataRaw;
							}


							try {
								//at this stage we have the class data in a byte array
								Class<?> c = loader.createNewClass(classData);
								Class<?>[] interfaces = c.getInterfaces();
								boolean isHBActionClass = false;
								for (Class<?> cc : interfaces) {
									if (cc.equals(HBAction.class)) {
										isHBActionClass = true;
										break;
									}
								}
								if (isHBActionClass) {
									incomingClass = (Class<? extends HBAction>) c;
									String class_name = incomingClass.getSimpleName();
									logger.debug("new HBAction >> " + class_name);
									setStatus("Loading " + class_name);
									// this means we're done with the sequence, time to recreate
									// the classloader to avoid duplicate errors
									loader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
									setStatus("Successful Load  " + class_name);
								} else {
									logger.debug("new object (not HBAction) >> " + c.getName());
								}
							}
							catch (ClassFormatError e)
							{
								logger.debug("Class Load error >> " + e.getMessage());
								setStatus("Error Loading Class");
							}
						} catch (Exception e) {
							logger.error("An error occurred while trying to read object from socket.", e);
						}
						if (incomingClass != null) {
							HBAction action = null;
							try {
								action = incomingClass.newInstance();
								action.action(HB.this);
							} catch (Exception e) {
								logger.error("Error instantiating received HBAction!", e);
													 // means that we avert an exception
													 // heading up to audio processes.
								//TODO look into reported cases where this still falls over.
							}
						}
						s.close();
					}
				} catch (IOException e) {
					logger.error("Error receiving new HBAction!", e);
				}
			}
		}.start();

	}

	/**
	 * Attempts to load the given class as an {@link HBAction}. If an {@link HBAction} can be found matching the fully qualified Java classname then this is loaded and its {@link HBAction#action(HB)} method is run.
	 * @param class_name The class name
	 */
	public void attemptHBActionFromClassName(String class_name) {
		try {
			Class<HBAction> hbActionClass = (Class<HBAction>)Class.forName(class_name);
			HBAction action = hbActionClass.newInstance();
			action.action(this);
		} catch (Exception e) {
			logger.error("Unable to cast Object into HBAction!", e);
		}
	}

	/**
	 * Gets the sensor with the given sensor ID. This will attempt to make a connection with the given sensor.
	 *
	 * @param sensorClass the class of the {@link Sensor} you want returned
	 * @return the returned {@link Sensor}, if one can be found
     */
	public Sensor getSensor(Class sensorClass) {
		Sensor result = sensors.get(sensorClass);
		if(!sensors.containsKey(sensorClass)) {
			try {
				result = (Sensor) sensorClass.getConstructor().newInstance();
				if(result != null) sensors.put(sensorClass, result);
			} catch (Exception e) {
				logger.info("Cannot create sensor: {}", sensorClass);
				setStatus("No sensor " + sensorClass + " available.");
			}
		}
		return result;
	}

	/**
	 * Puts an {@link Object} into the global memory store with a given name. This overwrites any object that was previously stored with the given name.
	 *
	 * @param s {@link String} name to store the object with.
	 * @param o {@link Object} to store.
     */
	public void put(String s, Object o) {
		share.put(s, o);
	}

	/**
	 * Gets an @{@link Object} with the given name from the global memory store. Returns null if there is no object with this name.
	 * @param s the name of the object.
	 * @return an @{@link Object} or null if there is no object.
     */
	public Object get(String s) {
		return share.get(s);
	}

	/**
	 * Gets an object of type int from the global memory store.
	 * @param s the name of the int.
	 * @return the int, or null if there is no object.
	 * @throws ClassCastException if the stored object is not an int.
     */
	public int getInt(String s) {
		return (Integer) share.get(s);
	}

	/**
	 * Gets an object of type float from the global memory store.
	 * @param s the name of the float.
	 * @return the float, or null if there is no object.
	 * @throws ClassCastException if the stored object is not a float.
	 */
	public float getFloat(String s) {
		return (Float) share.get(s);
	}

	/**
	 * Gets an object of type @{@link String} from the global memory store.
	 * @param s the name of the @{@link String}.
	 * @return the @{@link String}, or null if there is no object.
	 * @throws ClassCastException if the stored object is not a @{@link String}.
	 */
	public String getString(String s) {
		return (String) share.get(s);
	}

	/**
	 * Gets an object of type @{@link UGen} from the global memory store.
	 * @param s the name of the @{@link UGen}.
	 * @return the @{@link UGen}, or null if there is no object.
	 * @throws ClassCastException if the stored object is not a @{@link UGen}.
	 */
	public UGen getUGen(String s) {
		return (UGen) share.get(s);
	}

	/**
	 * Gets an object of type @{@link Bead} from the global memory store.
	 * @param s the name of the @{@link Bead}.
	 * @return the @{@link Bead}, or null if there is no object.
	 * @throws ClassCastException if the stored object is not a @{@link Bead}.
	 */
	public Bead getBead(String s) {
		return (Bead) share.get(s);
	}

	/**
	 * Stores an object in the global memory store. The object is stored only if you haven't already created something with that name. If you have, then the new object is not stored and the existing object is returned instead.
	 * Returns either the existing stored object or the new object.
	 *
	 * @param id ID {@link String} of the object to store.
	 * @param o the object.
	 * @return either the new stored object or the existing object if something previously stored there.
	 */
	public Object perm(String id, Object o) {
		if(share.containsKey(id)) {
			return share.get(id);
		} else {
			share.put(id, o);
			return o;
		}
	}

	/**
	 * Adds a new pattern {@link Bead} object to the {@link Clock}. This will be removed using {@link #reset()} or {@link #resetLeaveSounding()}, or can be specifically removed by killing the {@link Bead}.
	 *
	 * @param pattern to play.
	 * @return returns a string of the form "patternX" that can be used to store the pattern in global memory.
     */
	public String pattern(Bead pattern) {
		clock.addMessageListener(pattern);
		String name = "pattern" + nextElementID++;
//		put(name, pattern);
//		System.out.println(name);
		return name;
	}

	/**
	 * Adds a sound to the audio output. The sound, in the form of any @{@link UGen}, is played immediately. It can be killed by calling {@link #reset()}, or by manually destroying the sound with a {@link Bead#kill()} message. Note that the system automatically limits the number of sounds added using a @{@link PolyLimit} object.
	 *
	 * @param snd the sound to play.
	 * @return returns a string of the form "sndX" that can be used to store the pattern in global memory.
     */
	public String sound(UGen snd) {
		pl.addInput(snd);
		String name = "snd" + nextElementID++;
//		put(name, snd);
//		System.out.println(name);
		return name;
	}

	/**
	 * Clears all of the audio that is currently playing (connected to output). Warning, this leaves dependents and patterns. Just cleans the audio signal chain. If you want to completely clear all objects, use {@link #reset()} and if you want to clear everything except the sound, use {@link #resetLeaveSounding()}.
	 */
	public void clearSound() {
		//rebuilt top elements of signal chain
		ac.out.clearInputConnections();
		ac.out.addInput(pl);
		pl.clearInputConnections();
	}

	public void reset() {
		resetLeaveSounding();
		clearSound();
		setStatus("Reset");

	}

	/**
	 * Like {@link #reset()} except that any sounds currently playing are kept. This includes everything that is in the global memory store, all patterns, all dependents, all sensor behaviours and all controller listener behaviours.
 	 */
	public void resetLeaveSounding() {
		//clear dependencies and inputs
		ac.out.clearDependents();
		ac.out.addDependent(clock);
		clock.clearMessageListeners();
		clock.clearInputConnections();
		clock.clearDependents();
		pl.clearDependents();
		//clear data store
		share.clear();
		//clear mu listeners
		for(Sensor sensor : sensors.values()) {
			sensor.clearListeners();
			sensor.resetToDefault();
		}
		//clear osc listeners
		controller.clearListeners();
		//clear broadcast listeners
		broadcast.clearBroadcastListeners();

		// clear dynamic control listeners
		ControlMap.getInstance().clearAllListeners();
	}

	/**
	 * Like {@link #clearSound()} but with a fade out of the specified duration before clearing.
	 *
	 * @param fadeTime the fade out time in milliseconds.
     */
	public void fadeOutClearSound(float fadeTime) {
		masterGainEnv.clear();
		masterGainEnv.addSegment(0, fadeTime, new Bead() {
			public void messageReceived(Bead message) {
				clearSound();
				masterGainEnv.addSegment(1, 10);
			}
		});
	}

	/**
	 * Like {@link #reset()} but with a fade out of the specified duration before clearing.
	 *
	 * @param fadeTime the fade out time in milliseconds.
	 */
	public void fadeOutReset(float fadeTime) {
		masterGainEnv.clear();
		masterGainEnv.addSegment(0, fadeTime, new Bead() {
			public void messageReceived(Bead message) {
				reset();
				masterGainEnv.addSegment(1, 10);
			}
		});
	}

	/**
	 * Returns the assigned ID of the current device. The ID is a non-negative integer. The controller assigns IDs to devices based on either a look-up table in a file of known devices (mapping hostnames to IDs), or by assigning new integers on the fly. The ID of a device that has not yet been assigned an ID from the controller is -1.
	 * @return the ID of this device, as assigned by the current controller.
     */
	public int myIndex() {
		return myId;
	}

	/**
	 * We will set the index of the device here. It may be set by a controller
	 * @param val new value
	 */
	public void setMyIndex(int val){
		myId = val;
	}
	/**
	 * Reboots the device immediately.
	 */
	public static void rebootDevice() {
		try {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo reboot"}).waitFor();
		} catch (Exception e) {
			logger.error("Unable to reboot device!", e);
		}
	}

	/*
	 * Shuts down the device immediately.
	 */
	public static void shutdownDevice() {
		try {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo shutdown now"}).waitFor();
		} catch (Exception e) {
			logger.error("Unable to shutdown device!", e);
		}
	}

	/**
	 * Sets the status @{@link String} of this device. The status string is sent to the controller and is reported in the device's list item. Note that certain application behaviours automatically set the device status, such as when the device has been assigned and ID, or when a new @{@link HBAction} has been loaded onto the device.
	 * @param s the status of the device.
     */
	public void setStatus(String s) {
		status = s;
		synchronized (statusChangedListenerList)
		{
			for (StatusChangedListener listener : statusChangedListenerList) {
				listener.statusChanged(s);
			}
		}
	}

	/**
	 * Returns the current status @{@link String} of the device.
	 * @return the status of the device.
     */
	public String getStatus() {
		return status;
	}


	public enum AccessMode {
		/**
		 * Allow receiving incoming code from any controller.
		 */
		OPEN,
		/**
		 * Allow receiving incoming code only from a controller running on the same host machine.
		 */
		LOCAL,
		/**
		 * Disallow receiving code.
		 */
		CLOSED
	}

	/**
	 * A dynamic control that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 *
	 * @param parent_sketch the object calling - typically this
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @param initial_value The initial value of the control
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public DynamicControl createDynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value)
	{
		return new DynamicControl(parent_sketch, control_type, name, initial_value);
	}

	/**
	 * A dynamic control that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 *
	 * @param parent_sketch the object calling - typically this
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @param initial_value The initial value of the control
	 * @param min_value     The minimum value of the control
	 * @param max_value     The maximum value of the control
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public DynamicControl createDynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
		return new DynamicControl(parent_sketch, control_type, name, initial_value, min_value, max_value);
	}

	/**
	 * A dynamic control that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 *
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @param initial_value The initial value of the control
	 * @param min_value     The minimum value of the control
	 * @param max_value     The maximum value of the control
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public DynamicControl createDynamicControl(ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
		return new DynamicControl(control_type, name, initial_value, min_value, max_value);
	}

	/**
	 * A dynamic control that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 *
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @param initial_value The initial value of the control
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public DynamicControl createDynamicControl(ControlType control_type, String name, Object initial_value) {
		return new DynamicControl(control_type, name, initial_value);
	}

	/**
	 * A dynamic control that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 *
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public DynamicControl createDynamicControl(ControlType control_type, String name) {
		return new DynamicControl(control_type, name);
	}

}
