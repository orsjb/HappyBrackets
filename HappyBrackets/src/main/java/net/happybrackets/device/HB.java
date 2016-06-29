package net.happybrackets.device;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Random;

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
import net.happybrackets.core.BroadcastManager;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.dynamic.DynamicClassLoader;
import net.happybrackets.device.network.NetworkCommunication;
import net.happybrackets.device.sensors.MiniMU;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.core.Synchronizer;
import net.happybrackets.device.sensors.Sensor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HB {

	// audio stuff
	public final AudioContext ac;
	public final Clock clock;
	public final Envelope clockInterval;
	public final PolyLimit pl;
	public final Envelope masterGainEnv;
	boolean audioOn = false;
	
	String status = "No ID set";

	// sensor stuffs
	public final Hashtable<String, Sensor> sensors;

	// shared data
	public final Hashtable<String, Object> share = new Hashtable<String, Object>();
	private int nextElementID = 0;

	// random number generator for general use
	public final Random rng = new Random();

	// network comms stuff
	public final NetworkCommunication controller;
	public final BroadcastManager broadcast;
	public final Synchronizer synch;

	/**
	 * Creates the HB.
	 *
	 * @param _ac the {@link AudioContext} for audio.
	 * @throws IOException if any of the core network set up fails. Could happen if port is already in use, or if setting up multicast fails.
     */
	public HB(AudioContext _ac) throws IOException {
		ac = _ac;
		// default audio setup (note we don't start the audio context yet)
		masterGainEnv = new Envelope(ac, 0);
		masterGainEnv.addSegment(1, 5000);
		ac.out.setGain(masterGainEnv);
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);
		pl = new PolyLimit(ac, ac.out.getOuts(), DeviceConfig.getInstance().getPolyLimit());
		pl.setSteal(true);
		ac.out.addInput(pl);
		ac.out.addDependent(clock);
		System.out.println("HB audio setup complete.");
		// sensor setup
		sensors = new Hashtable<>();
		//TODO this is temporary code. Eventually available sensors will be read from the DeviceConfig.
		sensors.put("mu", new MiniMU());
		// start network connection
		controller = new NetworkCommunication(this);
		broadcast = new BroadcastManager(DeviceConfig.getInstance());	//TODO is this singleton approach good?
		synch = Synchronizer.getInstance();
		// start listening for code
		startListeningForCode();
		//notify started (happens immeidately or when audio starts)
		testBleep3();
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
			}
		}, time);
	}

	/**
	 * Causes an action to be implemented at the given, synchronized time.
	 * @param runnable the action to perform.
	 * @param time the time at which to perform the action.
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
        System.out.println("GET config file: " + configUrl);
        OkHttpClient client = new OkHttpClient();
        Request request = new okhttp3.Request.Builder()
                .url(configUrl)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println("Saving new config file: " + configFile);
        Files.write(Paths.get(configFile), response.body().string().getBytes());
        //reload config from file again after pulling in updates
        System.out.println("Reloading config file: " + configFile);
        DeviceConfig.load(configFile);
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
					// start socket server listening loop
					while (true) {
						// must reopen socket each time
						Socket s = server.accept();
						Class<? extends HBAction> incomingClass = null;
						try {
							InputStream input = s.getInputStream();
							ByteArrayOutputStream buffer = new ByteArrayOutputStream();
							int data = input.read();
							while (data != -1) {
								buffer.write(data);
								data = input.read();
							}
							byte[] classData = buffer.toByteArray();
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
								System.out.println("new HBAction >> " + incomingClass.getName());
								// this means we're done with the sequence, time to recreate
								// the classloader to avoid duplicate errors
								loader = new DynamicClassLoader(ClassLoader.getSystemClassLoader());
								status = "Last HBAction: " + incomingClass.getCanonicalName();
							} else {
								System.out.println("new object (not HBAction) >> " + c.getName());
							}
						} catch (Exception e) {/* snub it? */
							System.out.println("Exception Caught trying to read Object from Socket");
							e.printStackTrace();
						}
						if (incomingClass != null) {
							HBAction pipo = null;
							try {
								pipo = incomingClass.newInstance();
								pipo.action(HB.this);
							} catch (Exception e) {
								e.printStackTrace(); // catching all exceptions
													 // means that we avert an exception
													 // heading up to audio processes.
								//TODO look into reported cases where this still falls over.
							}
						}
						s.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

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
	 * @return returns a string of the form "patternX" that can be used to retrieve the pattern from global memory.
     */
	public String pattern(Bead pattern) {
		clock.addMessageListener(pattern);
		String name = "pattern" + nextElementID++;
		put(name, pattern);
		System.out.println(name);
		return name;
	}

	/**
	 * Adds a sound to the audio output. The sound, in the form of any @{@link UGen}, is played immediately. It can be killed by calling {@link #reset()}, or by manually destroying the sound with a {@link Bead#kill()} message. Note that the system automatically limits the number of sounds added using a @{@link PolyLimit} object.
	 *
	 * @param snd the sound to play.
	 * @return returns a string of the form "sndX" that can be used to retrieve the pattern from global memory.
     */
	public String sound(UGen snd) {
		pl.addInput(snd);
		String name = "snd" + nextElementID++;
		put(name, snd);
		System.out.println(name);
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
		}
		//clear osc listeners
		controller.clearListeners();
		//clear broadcast listeners
		broadcast.clearBroadcastListeners();
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
		return controller.getID();
	}

	/**
	 * Reboots the device immediately.
	 */
	public static void rebootDevice() {
		try {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo reboot"}).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Shuts down the device immediately.
	 */
	public static void shutdownDevice() {
		try {
			Runtime.getRuntime().exec(new String[]{"/bin/bash","-c","sudo shutdown now"}).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the status @{@link String} of this device. The status string is sent to the controller and is reported in the device's list item. Note that certain application behaviours automatically set the device status, such as when the device has been assigned and ID, or when a new @{@link HBAction} has been loaded onto the device.
	 * @param s the status of the device.
     */
	public void setStatus(String s) {
		status = s;
	}

	/**
	 * Returns the current status @{@link String} of the device.
	 * @return the status of the device.
     */
	public String getStatus() {
		return status;
	}

}


