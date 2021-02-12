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

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.*;
import net.happybrackets.core.config.KnownDeviceID;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.scheduling.ClockAdjustment;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.core.scheduling.DeviceSchedules;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.config.DeviceConfig;
import net.happybrackets.device.dynamic.DynamicClassLoader;
import net.happybrackets.device.network.DeviceConnectedEventListener;
import net.happybrackets.device.network.NetworkCommunication;
import net.happybrackets.device.sensors.*;
import net.happybrackets.device.sensors.gpio.GPIO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;

//import javafx.application.Application;
//import net.happybrackets.intellij_plugin.gui.DynamicControlScreen;

//import javafx.application.Application;

/**
 * HB is the main controller class for a HappyBrackets program. It is accessed from an {@link HBAction}'s {@link HBAction#action(HB)} method, where users can play sounds, create network listeners, send network messages, and perform other actions.
 */
public class HB {

	// The lead device on our scheduler will send this number of Milliseconds apart as a sync message
	// Other devices will send this number multiplied by the number devices so we don't get clogged with messages
	final int DEVICE_SCHEDULE_TIME = 1000;
	final int DEVICE_SYNCHRONISER_REMOVE_TIME =  10000; // If we don't see a lead device for this time we will remove it

    static final int BLINK_INTERVAL = 250;
    static final int NUM_BLINKS = 6;

	private static boolean enableSimulators = false;

	// Define a map that associates a device name with an InetAddress
	private Map<String, InetAddress> deviceAddressByHostname = new Hashtable<String, InetAddress>();


	private List<DeviceConnectedEventListener> deviceConnectedEventsListeners = new ArrayList<>();


	// create a lock to synchronise getting default sensor
	private final static Object sensorLock = new Object();

	private static OSCUDPListener OSCListener = null;

	/**
	 * Return the Mute Control
	 * @return the Mute Control
	 */
	public MuteControl getMuteControl() {
		return muteControl;
	}

	/**
	 * Set the Mute Control for HB
	 * @param muteControl the Mute Control we are using
	 */
	void setMuteControl(MuteControl muteControl) {
		this.muteControl = muteControl;
	}

	private MuteControl muteControl = null;

	private static boolean simulatorOnly = false;
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

	/// create an execute shell to set onboard LED
	private  ShellExecute piLedSetter = new ShellExecute();

	/**
	 * The Type of Hardware that this instance of HappyBrackets is running on
	 */
	public enum DeviceType {
		/**
		 * The Harware Type is unknown or undefined
		 */
		UNKONWN,

		/**
		 * The Device is actually being run inside the Debugger
		 */
		DEBUGGER,

		/**
		 * This is a simulator
		 */
		SIMULATOR,

		/**
		 * Raspberry Pi zero
		 */
		PI_ZERO,

		/**
		 * Raspberry Pi V1
		 */
		PI_1,

		/**
		 * Raspberry Pi V2
		 */
		PI_2,

		/**
		 * Raspberry Pi V3
		 */
		PI_3,

		/**
		 * Raspberry Pi V4
		 */
		PI_4
	}


	static DeviceType deviceType =  DeviceType.UNKONWN;



	/**
	 * Set the TCP Osc Port given to us by operating system
	 * We will pass it to the controller so it knows how to connect to us
	 * @param tcpServerPort the Port we need to connect to TCP server
	 */
    public void setTCPServerPort(int tcpServerPort) {
		myDeviceId.setConnectToServerPort(tcpServerPort);
    }


	public interface StatusChangedListener{
		void statusChanged(String new_status);
		void classLoadedMessage(Class<? extends HBAction> incomingClass);
		void gainChanged(float new_gain);
	}

	/**
	 * We will store all our loaded classes so we can see if they need to be reset
	 */
	private static List <Object> loadedHBClasses = Collections.synchronizedList(new ArrayList<Object>());

	/**
	 * We will store the INetAddress of the controllers that create the classes
	 */
	private  Map<Object, InetAddress> classSenders = Collections.synchronizedMap( new Hashtable<Object, InetAddress>());

	private List<StatusChangedListener> statusChangedListenerList  = new ArrayList<>();

	// We will create a static one to know whether we are in Debug or run
	// If we are in a PI, this will already be set when we try to debug
	public static HB HBInstance = null;
	/**
	 * Whether we will use encryption in transferring class data
	 * @param enable true if we want to use encryption
	 */
	public void setUseEncryption(boolean enable) {
		useEncryption = enable;
	}

	private boolean useEncryption = false;

	/**
	 * Get the {@link InetAddress} of the controller that sent the class
	 * If the class was loaded on startup it will be null
	 * @param classObject the class that was sent by the controller
	 * @return Address of controller that sent the class. If was a startup class, then returns null
	 */
	public InetAddress getSendingController(Object classObject){
		InetAddress ret = null;

		if (classSenders.containsKey(classObject)){
			ret = classSenders.get(classObject);
		}
		return ret;
	}

	/**
	 * Determine if we have any classes Loaded
	 * @return true if we have any classes loaded
	 */
	public boolean hasClassesLoaded(){
		return loadedHBClasses.size() > 0;
	}
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

	// registered data
	/**
	 * A {@link Hashtable} to store generic objects that do not get cleared when reset, however, are still volatile.
	 */
	private Hashtable<String, Object> registered = new Hashtable<String, Object>();


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
	private KnownDeviceID myDeviceId = new KnownDeviceID();

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
	 * Set the onboard LED value on the Pi based on Integer value.
	 * @param i_val the value to turn LED on or off is dependeant upon the {@link DeviceType}, so thei function should be called from {@link HB#setDeviceLedValue(boolean)}
	 */
	synchronized  void setPiLed(int i_val){
		try {
			piLedSetter.runProcess("/bin/sh", "-c", "echo " + i_val + "  > /sys/class/leds/led0/brightness");
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	/**
	 * Return the Integer value required to turn the onboard LED on or off based on stored {@link DeviceType}
	 * @param on true if turning LED on, false if Turning Off
	 * @return true if able to send value
	 */
	public static boolean setDeviceLedValue(boolean on) {
		boolean ret = false;

		switch (deviceType) {
			case PI_3:
			case PI_2:
			case PI_4:
			case PI_1:
				HB.HBInstance.setPiLed(on ? 1 : 0);
				ret = true;
				break;

			case PI_ZERO:
				HB.HBInstance.setPiLed(on ? 0 : 1);
				ret = true;
				break;

			case DEBUGGER:
			case SIMULATOR:
				System.out.println("Led " + on);
				ret = true;

			default:
		}
		return ret;
	}
	/**
	 * The Type of Device this instance of HappyBrackets is running on
	 * @return the Type of Device
	 */
	public static DeviceType getDeviceType(){
		return deviceType;
	}


	/**
	 * Sets how often in milliseconds this device will send an alive message to all
	 * controllers. Does not take effect until next send.
	 * If this interval is less than previous one then will cause a send immediatly
	 * You can force by sending immediately with {@link #sendAliveMessage}
	 * @param milliseconds number of milliseconds between
	 */
	public void setAliveTimeInterval(int milliseconds){
		controller.setAlivetimeInterval(milliseconds);
	}

	/**
	 * Force device to send alive message to all controllers
	 */
	public void sendAliveMessage(){
		controller.sendAlive();
	}

	/**
	 * Detect the type of device we are running by reading specific files through filesystem
	 * If detected, the new {@link DeviceType} is stored
	 * @return new deviceType detected. If none dected, whatever has been set previously
	 */
	public static DeviceType detectDeviceType(){

		try
		{
			String detected_type =  "";

			byte[] readAllBytes = java.nio.file.Files.readAllBytes(Paths.get("/proc/device-tree/model"));
			detected_type = new String( readAllBytes);

			if (!detected_type.isEmpty()){
				if (detected_type.contains("Pi Zero")){
					deviceType = DeviceType.PI_ZERO;
				}
				else if (detected_type.contains("Pi 1")){
					deviceType = DeviceType.PI_1;
				}
				else if (detected_type.contains("Pi 2")){
					deviceType = DeviceType.PI_2;
				}
				else if (detected_type.contains("Pi 3")){
					deviceType = DeviceType.PI_3;
				}
			}
		}
		catch (Exception ex){}


		return deviceType;
	}


	/**
	 * Perform a mute or Unmute of Output
	 * @param mute true if we want to mute
	 * @return the value of previous mute state.
	 */
	public boolean muteAudio(boolean mute){
		boolean ret = false;
		if (muteControl != null){
			ret = muteControl.muteOutput(mute);
		}
		return ret;
	}

	/**
	 * Process Annotations @HBParam and @HBCommand and create an OSCListener if any of those exists.
	 * @param newinstance Instance of HBAction
	 */
	private static void processAnnotations(HBAction newinstance) {
		Map<String, Class> exposedVariables = new HashMap<>();
		Map<String, Class[]> exposedMethods = new HashMap<>();

		int oscPort =  9001;

		for(Field field : newinstance.getClass().getDeclaredFields()){

			Class type = field.getType();
			String name = field.getName();
			Annotation[] annotations = field.getAnnotations();

			for(Annotation ann: annotations) {
				if(ann.annotationType() == HBAction.HBParam.class) {
					exposedVariables.put(name,type);
				}
			}

			try {
				Object value = field.get(newinstance);
				if(name.equals("oscPort")) {
					oscPort = (int)value;
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}

		for(Method method : newinstance.getClass().getDeclaredMethods()){
			String name = method.getName();
			Annotation[] annotations = method.getAnnotations();

			for(Annotation ann: annotations) {
				if(ann.annotationType() == HBAction.HBCommand.class) {
					if(exposedMethods.containsKey(name)) {
						System.out.println("ERROR - Multiple methods with the same name: " + name);
						continue;
					}
					exposedMethods.put(name,method.getParameterTypes());
				}
			}

		}

		if(exposedMethods.isEmpty() && exposedVariables.isEmpty()) return;

		HBAction finalNewinstance = newinstance;

		OSCListener = new OSCUDPListener(oscPort) {
			@Override
			public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
				try {
					String messageName = oscMessage.getName().substring(1);
					if(exposedVariables.containsKey(messageName)) {
						Class type = exposedVariables.get(messageName);
						Field field = newinstance.getClass().getField(messageName);
						switch (type.toString()) {
							case "java.lang.String" :
								field.set(finalNewinstance, String.valueOf(oscMessage.getArg(0)));
								break;
							case "class de.sciss.net.OSCMessage":
								field.set(finalNewinstance, oscMessage);
								break;
							case "float":
								try {
									field.setFloat(finalNewinstance, (float) oscMessage.getArg(0));
								} catch( ClassCastException e) {
									field.setFloat(finalNewinstance, (int)oscMessage.getArg(0));
								}
								break;
							case "int":
								field.setInt(finalNewinstance, (int) oscMessage.getArg(0));
								break;
							case "boolean":
								field.setBoolean(finalNewinstance, (boolean)oscMessage.getArg(0));
								break;
							default:
								System.out.println("ERROR Data Type not expected: " + type + ". Expected values: f i b s");
						}
					}
					if(exposedMethods.containsKey(messageName)) {
						Class types[] = exposedMethods.get(messageName);
						Method m = newinstance.getClass().getMethod(messageName, types);
						Object[] ObjectArray = new Object[types.length];
                        /*
                        For each arguments in the OSC message, cast this argument to the expected type according to the method signature
                        Matching the osc parameter position with the method parameter position
                         */
						int arg = 0;
						for(Class argType: types) {
							switch (argType.toString()) {
								case "java.lang.String" :
									ObjectArray[arg] = String.valueOf(oscMessage.getArg(arg));
									break;
								case "class de.sciss.net.OSCMessage":
									ObjectArray[arg] =  oscMessage;
									break;
								case "float":
									try {
										ObjectArray[arg] = (float)oscMessage.getArg(arg);
									} catch( ClassCastException e) {
										ObjectArray[arg] = (int)oscMessage.getArg(arg);
									}
									break;
								case "int":
									ObjectArray[arg] =  (int)oscMessage.getArg(arg);
									break;
								case "boolean":
									ObjectArray[arg] = (boolean)oscMessage.getArg(arg);
									break;
								default:
									System.out.println("ERROR Data Type not expected: " + argType.toString() + ". Expected values: f i b s");
							}
							arg++;
						}
						m.invoke(finalNewinstance, ObjectArray);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};
	}

	/**
  	 * Run HB in a debug mode so we can debug sample code in INtelliJ
	 * run the command like this: HB.runDebug(MethodHandles.lookup().lookupClass());
	 * @param action_class The class that we are debugging. use MethodHandles.lookup().lookupClass()
	 * @return true if we are debugging
	 * @throws IOException if there is a problem with the debug
	 */
	public static boolean runDebug(Class<?> action_class) throws Exception {

		// we need to display Visualiser here and then assign AudioContext at the end of this function
		// Otherwise, running for IDE crashes on some systems
		DebuggerWaveformVisualizer visualizer = DebuggerWaveformVisualizer.createVisualiser();
		// Disable Our non simulated sensors to prevent extra System Messages
		Sensor.setSimulatedOnly(true);

		boolean ret = false;
		String current = System.getProperty("user.dir");
		String simulator_file = current + "/scripts/simulator.config";


		deviceType =  DeviceType.DEBUGGER;

		String[] start_args = new String[]{
				"buf=1024",
				"sr=44100",
				"bits=16",
				"ins=0",
				"outs=1",
				"device=0",
				"start=true",
				"access=local"
		};


		try (BufferedReader br = new BufferedReader(new FileReader(simulator_file))) {
			ArrayList<String> ar = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null) {
				// process the line.
				System.out.println(line);
				ar.add(line.trim().toLowerCase());
			}
			start_args = new String[ar.size()];

			for (int i = 0; i < ar.size(); i++){
				start_args[i] = ar.get(i);
			}
		}
		catch (Exception ex){}

		System.out.println(current);
		// we will enable simulators of Sensors
		enableSimulators = true;
		simulatorOnly = true;
		if (HBInstance == null) {

			HB.AccessMode mode = HB.AccessMode.LOCAL;


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
						// If we have a mute control, disable muting
						hb.muteAudio(false);
						action = incomingClass.newInstance();

						// add our controller address before we call action
						hb.classSenders.put(action, InetAddress.getLocalHost());
						action.action(hb);

						HB.processAnnotations(action);

						// we will add to our list here.
						// It is important we do this after the action in case this is the class that called reset
						synchronized (loadedHBClasses) {
							loadedHBClasses.add(action);
						}

						ret = true;


						try
						{

							/* This code has been removed because we cannot spawn App from every platform
							if (incomingClass.isAssignableFrom(Application.class)) {
								// now we try and JavaFX calls
								System.out.println("Incoming class assignable");
								DynamicControlScreen debugControlsScreen = new DynamicControlScreen("Debug");


								System.out.println("Add Listener");
								debugControlsScreen.addDynamicControlScreenLoadedListener(new DynamicControlScreen.DynamicControlScreenLoaded() {
									@Override
									public void loadComplete(DynamicControlScreen screen, boolean loaded) {
										// screen load is complete.
										//Now Add all controls
										System.out.println("Listener Load Complete");
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

								System.out.println("Create Dynamic Control Stage");
								// now we have a listener to see when stage is made, let us load the stage
								debugControlsScreen.createDynamicControlStage();
							}
							else
							{

								System.out.println("Swing Invoke Later");
								SwingUtilities.invokeLater(() -> {
									try {
										Application.launch(DebugApplication.class);
									} catch (Exception ex) {
									}
								});


							}

							 */
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
		visualizer.setAudioContext(HB.getAudioOutput().getContext());
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
		UGen.setDefaultContext(_ac);
		// default audio setup (note we don't start the audio context yet)
		masterGainEnv = new Envelope(ac, 0);
		masterGainEnv.addSegment(1, 5000);
		ac.out.setGain(masterGainEnv);
		clockInterval = new Envelope(ac, 500);
		clock = new Clock(ac, clockInterval);

		primeJIT();


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
			controller = new NetworkCommunication(this);
			// start network connection
			broadcast = new BroadcastManager(multi_cast_address, broadcast_port);


			System.out.print(".");
			synch = Synchronizer.getInstance();
			System.out.print(".");

			DeviceConfig config = DeviceConfig.getInstance();

			if (config != null) {
				DeviceConfig.getInstance().listenForController(broadcast);
				System.out.print(".");

				this.accessMode = _am;
				if (accessMode != AccessMode.CLOSED) {
					// start listening for code
					startListeningForCode();
					startSynchronisation();
				}
				System.out.print(".");


				broadcast.startRefreshThread();
				testBleep3();

				DynamicControl.postRequestNamesMessage();
			}
			else
			{
				System.out.println("No Config file detected");
				testBleep6();
			}
		}
		else
		{
			controller = null;
			synch = null;
		}
		//notify started (happens immeidately or when audio starts)

		logger.info("HB initialised");

		HBInstance = this;

		addDeviceAddress(Device.getDeviceName(), InetAddress.getLoopbackAddress());
		DynamicControl.postRequestNamesMessage();
	}


	/**
	 * Broadcast an {@link OSCMessage} msg over the multicast group.
	 *
	 * @param string the message string to send.
	 * @param args the args to the message.
	 */
	public void broadcast(String string, Object... args) {
		if(simulatorOnly){
			System.out.println("You cannot broadcast while using simulator ");
		}else {
			broadcast.broadcast(string, args);
		}
	}

    /**
     * Creates a valid OSC message. It converts invalid types to OSC compatible
     * EG, doubles to floats, boolean to int, long to int
     * @param name OSC Message name
     * @param args arguments
     * @return OSCMessage
     */
	public static OSCMessage createOSCMessage(String name, Object ...args){
        return OSCMessageBuilder.createOscMessage(name, args);
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
		if(simulatorOnly){
			System.out.println("You cannot addBroadcastListener while using simulator ");
		}else {
			broadcast.addBroadcastListener(listener);
		}
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
	 * @param intervalForSyncAction time at which to sync, according to the agreed clock time
	 * @deprecated Syn functions removed
     */
	public void syncAudioStart(int intervalForSyncAction) {
		long timeToAct = (synch.correctedTimeNow() / intervalForSyncAction + 1)  * intervalForSyncAction;
		doAtTime(new Runnable() {
			public void run() {
				startAudio();
				clock.reset();		//if audio is already running, we just reset the clock
			}
		}, timeToAct);
	}

	/**
	 * Returns the system time adjusted according to the result of any device synch attempts.
	 *
	 * @return Scheduled time
     */
	public double getSynchTime() {
		return HB.getSchedulerTime();
	}

	/**
	 * Causes an action to be implemented at the given, synchronized time.
	 * @param runnable the action to perform.
	 * @param time the time at which to perform the action, in millseconds since 1st Jan 1970.
	 * @deprecated
     */
	public void doAtTime(Runnable runnable, long time) {
		synch.doAtTime(runnable, time);
	}

	/**
	 *
	 * @param time the Scheduler
	 * @param listener The listener to receive call
	 *
	 * The param returned in the listener will be null
	 */
	public void doAtTime(double time, Delay.DelayListener listener){

		new Delay(time - getSynchTime(), null, listener);
	}

	/**
	 *
	 * @param time the Scheduler
	 * @param param the parameter to pass to listener
	 * @param listener The listener to receive call
	 */
	public void doAtTime(double time, Object param, Delay.DelayListener listener){

		new Delay(time - getSynchTime(), param, listener);
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
	 * Perform a bleep at the network scheduled time. After sending message, will send a status message
	 *
	 * @param schedule_time the time on the global {@link HBScheduler} to action message
	 */
	public void sychronisedBleep(double schedule_time){

		double interval =  schedule_time -  getSchedulerTime();

		// type delayline to create this code
		Delay delay = new Delay(interval, null, (delay_offset, param) -> {
			// delay_offset is how far out we were from our exact delay time in ms and is a double
			// param is the parameter we passed in type your code below this line

			testBleep();
			setStatus("Bleep " + (long)getSchedulerTime());
			// type your code above this line
		});
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

		boolean was_muted = muteAudio(false);
		// Then this is Muted already. Unmute and wait for power to load
		if (was_muted){
			e.addSegment(0, 100);
		}

		e.addSegment(0, 10);
		e.addSegment(0.2f, 0);
		e.addSegment(0.2f, 50);

		e.addSegment(0, 10, new KillTrigger(g));

		flashLed(NUM_BLINKS, BLINK_INTERVAL);
	}

    /**
     * FLash the onboard LED
     * @param num_blinks the number of times the LED wil blink on and Off
     * @param interval The interval between blinks
     */
	public static void flashLed(int num_blinks, int interval){

        setDeviceLedValue(true);
		// To create this, just type clockTimer. We use interval / 2 becauuse each tick will be toggleing state
		net.happybrackets.core.scheduling.Clock addClockTickListener = createClock(interval / 2).addClockTickListener((offset, this_clock) -> {// Write your code below this line

			boolean led_state = this_clock.getNumberTicks() % 2 == 0;
			if (this_clock.getNumberTicks() >= num_blinks * 2){
				this_clock.stop();
				this_clock.reset();
			}
			setDeviceLedValue(led_state);
			// Write your code above this line
		});

		addClockTickListener.start();// End Clock Timer
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

		// we will start Muted
		e.addSegment(0, 100,
				new Bead() {
					@Override
					protected void messageReceived(Bead bead) {/* Write your code below this line */

						/* Write your code above this line */
						if (loadedHBClasses.size() == 0) {
							muteAudio(true);
						} else {
							muteAudio(false);
						}
					}
				});

		e.addSegment(0, 10, new KillTrigger(g));
	}

	/**
	 * Produces a short series of 6 bleeps on the device to indicate an error. Assumes audio is running.
	 */
	public void testBleep6() {
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
		e.addSegment(0.4f, 300);

		e.addSegment(0f, 10);
		e.addSegment(0f, 400);
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
	 * Launch a separate thread to start sending Scheduler Time
	 */
	private void startSynchronisation(){
		// Type threadFunction to generate this code
		Thread thread = new Thread(() -> {

			DeviceSchedules deviceSchedules = DeviceSchedules.getInstance();

			while (true) {// write your code below this line

				// write your code above this line
				try {
					deviceSchedules.sendCurrentTime();

					if (deviceSchedules.isLeadDevice()) {
						Thread.sleep(DEVICE_SCHEDULE_TIME);
					}
					else
					{
						Thread.sleep(DEVICE_SCHEDULE_TIME * deviceSchedules.numberDevices());
					}

					if (DeviceSchedules.getInstance().removeExpiredLead(DEVICE_SYNCHRONISER_REMOVE_TIME)){
						setStatus("Lead Device Removed");
					}


				} catch (InterruptedException e) {// remove the break below to just resume thread or add your own action
					break;

				}
			}
		});

		//  write your code you want to execute before you start the thread below this line

		// write your code you want to execute before you start the thread above this line

		thread.start();// End threadFunction
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

								muteAudio(false);

								// Make sure we call before action so we can use it in our action
								classSenders.put(action, incomingAddress);

								action.action(HB.this);

								HB.processAnnotations(action);

								sendClassLoaded(incomingClass);
								// we will add to our list here.
								// It is important we do this after the action in case this is the class that called reset
								synchronized (loadedHBClasses) {
									loadedHBClasses.add(action);
								}



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
			muteAudio(false);
			action.action(this);
			sendClassLoaded(hbActionClass);
			// we will add to our list here.
			// It is important we do this after the action in case this is the class that called reset
			synchronized (loadedHBClasses) {
				loadedHBClasses.add(action);
			}

		} catch (Exception e) {
			logger.error("Unable to cast Object into HBAction!", e);
		}
	}

	/**
	 * Gets the sensor with the given sensor ID. This will attempt to make a connection with the given sensor.
	 * Note that this function will be deprecated in 3.0
	 * deprecated use {@link #findSensor} instead.
	 * @param sensorClass the class of the {@link Sensor} you want returned
	 * @return the returned {@link Sensor}, if one can be found
     */
	@SuppressWarnings("rawtypes")
	public Sensor getSensor(Class sensorClass) {
		// see if we have a sensor stored in our list
		Sensor result = sensors.get(sensorClass);

		// check the versions we have store here
		if(!sensors.containsKey(sensorClass)) {
			// we do not have a copy here. See if one was called by a generic caller
			try {
				// check the ones we have stored inside sensor class
				result = Sensor.getSensor(sensorClass);
				if (result == null) {
					try {
						sensorClass.getConstructor().newInstance();
					}
					catch (Exception ex){}
				}

				// we will load from getSensor
				result = Sensor.getSensor(sensorClass);

				if(result != null) {
					// let is see if it did a valid load
					if (result.isValidLoadedSensor()){
						sensors.put(sensorClass, result);
					}
					else
					{
						// it was not loaded correctly. Set our result to null
						result = null;
					}

				}
			} catch (Exception e) {
				logger.info("Cannot create sensor: {}", sensorClass);
				setStatus("No sensor " + sensorClass + " available.");
			}
		}
		else
		{
			// Do not make Simulation sensors reload. Instead, we will
			//if (Sensor.isSimulatedOnly()){
				//result.reloadSimulation();
			//}

		}
		return result;
	}

    /**
     *
     * @param sensorClass the class of the {@link Sensor} you want returned
     * @return the returned {@link Sensor}
     * @throws SensorNotFoundException if sensor type not found
     */
    @SuppressWarnings("deprecation")
	public Sensor findSensor(Class sensorClass) throws SensorNotFoundException{
	    Sensor sensor = getSensor(sensorClass);
	    if (sensor == null){
	        throw new SensorNotFoundException("Unable to find " + sensorClass.getSimpleName());
        }
        return sensor;
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
	 * Puts an {@link Object} into the global memory store with a given name. This overwrites any object that was previously stored with the given name.
	 * Values does not get reset, however, is still volatile
	 *
	 * @param name {@link String} name to store the object with.
	 * @param value {@link Object} to store.
     */
	public void registerVariable(String name, Object value) {
		registered.put(name, value);
	}


	/**
	 * Gets an @{@link Object} with the given name from the registered memory store. Returns null if there is no object with this name.
	 * @param name {@link String} name object is mapped as
	 * @return an @{@link Object} or null if there is no object.
	 */
	public Object getRegisteredVariable(String name) {
		Object ret =  null;

		if (registered.containsKey(name)){
			ret = registered.get(name);
		}
		return  ret;
	}

	/**
	 * Erases @{@link Object} with the given name from the registered memory store.
	 * @param name {@link String} name object is mapped as
	 */
	public void clearRegisteredVariable(String name) {
		if (registered.containsKey(name)){
			registered.remove(name);
		}

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
	 * Adds a UGen to {@link PolyLimit} connected to the audio output. The sound, in the form of any @{@link UGen}, is played immediately. It can be killed by calling {@link #reset()}, or by manually destroying the sound with a {@link Bead#kill()} message.
	 * Note that the system automatically limits the number of sounds added using a @{@link PolyLimit} object.
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
		OSCUDPReceiver.resetListeners();
		GPIO.resetGpioListeners();
		deviceConnectedEventsListeners.clear();
		// clear all scheduled events
		HBScheduler.getGlobalScheduler().reset();
		if(OSCListener != null) {
			OSCUDPReceiver.resetListeners();
			OSCListener.close();
			OSCListener = null;
		}

		synchronized (loadedHBClasses) {
			for (Object loaded_class : loadedHBClasses) {

				try {
					Class<?>[] interfaces = loaded_class.getClass().getInterfaces();

					for (Class<?> cc : interfaces) {
						if (cc.equals(HBReset.class)) {

							((HBReset)loaded_class).doReset();
						}
					}

				} catch (Exception ex){}
			}

			loadedHBClasses.clear();

		}
		setStatus("Reset");

	}

	/**
	 * Like {@link #reset()} except that any sounds currently playing are kept. This includes everything that is in the global memory store, all patterns, all dependents, all sensor behaviours and all controller listener behaviours.
 	 */
	public void resetLeaveSounding() {
		try {
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
			for (Sensor sensor : sensors.values()) {
				sensor.clearListeners();
				sensor.resetToDefault();
			}
			if (controller != null) {
				//clear osc listeners
				controller.clearListeners();
			}

			if (broadcast != null) {
				//clear broadcast listeners
				broadcast.clearBroadcastListeners();
			}

			// clear dynamic control listeners
			ControlMap.getInstance().clearAllListeners();
		}
		catch (Exception ex){}
	}

	/**
	 * Get the default  HappyBrackets audio output as a UGen
	 * @return the default HappyBrackets audio Output Ugen
	 */
	public static UGen getAudioOutput(){
		UGen ret = null;
		if (HB.HBInstance != null){
			if (HB.HBInstance.ac != null){
				ret = HB.HBInstance.ac.out;
			}
		}
		return ret;
	}


	/**
	 * Get the {@link PolyLimit} that is connected to the  HappyBrackets  audio output as a UGen
	 * @return the  HappyBrackets PolyLimit connected to the default output
	 */
	public static PolyLimit getPolyLimitedOutput(){
		PolyLimit ret = null;
		if (HB.HBInstance != null){
			if (HB.HBInstance.ac != null){
				ret = HB.HBInstance.pl;
			}
		}
		return ret;
	}


	/**
	 * Get the number of audio channels on the default HappyBrackets audio output <br>
	 * This parameter is dependant upon the configuration of the device
	 * @return the number of channels
	 */
	public static int getNumOutChannels(){
		int ret = 0;

		if (getAudioOutput() != null){
			ret =  getAudioOutput().getOuts();
		}
		return ret;
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
		return myDeviceId.getDeviceId();
	}

	public int myConnectPort(){
		return myDeviceId.getConnectToServerPort();
	}
	/**
	 * We will set the index of the device here. It may be set by a controller
	 * @param val new value
	 */
	public void setMyIndex(int val){
		myDeviceId.setDeviceId(val);
	}

	/**
	 * Returns a freindly name for the device. This is assigned by controller if it is in the known config
	 * It makes it easier to determine which device we want than the hostname - eg Dancer1 is easier to read than hb-001d43201188
	 * @return the friendly name if assigned.
	 */
	public String friendlyName(){
		return  myDeviceId.getFriendlyName();
	}

	/**
	 * We will set the friendly of the device here. It may be set by a controller
	 * @param name the friendly name
	 */
	public void setFriendlyName(String name){
		myDeviceId.setFriendlyName(name);
	}


	/**
	 * Reboots the device immediately.
	 */
	public static void rebootDevice() {
		if (enableSimulators){
			System.exit(0);
		}
		else {
			try {
				Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "sudo reboot"}).waitFor();
			} catch (Exception e) {
				logger.error("Unable to reboot device!", e);
			}
		}
	}

	/*
	 * Shuts down the device immediately.
	 */
	public static void shutdownDevice() {
		if (enableSimulators){
			System.out.println("Shutdown Simulator");
			System.exit(0);
		}
		else {
			try {
				Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "sudo shutdown now"}).waitFor();
			} catch (Exception e) {
				logger.error("Unable to shutdown device!", e);
			}
		}
	}

	/**
	 * Force JIT Compiler to compile certain code on startup
	 */
	private void primeJIT(){
		// Do following call twice because of delays from JIT compiler. Synchronises clocks to system time
		synchroniseClocks();
		synchroniseClocks();
		synchroniseClocks();

		List<String> self = new ArrayList<>();
		self.add(Device.getDeviceName());

		// Prime Our receive Schedule Clock
		HB.sendScheduleSetTime(HB.getSchedulerTime(), self);
		HB.sendScheduleSetTime(HB.getSchedulerTime(), self);
		HB.sendScheduleSetTime(HB.getSchedulerTime(), self);


	}

	/**
	 * Static version of {@link HB#sendStatus(String)}.
	 * @param status the status message we are sending
	 */
	public static void sendStatus(String status){
		HBInstance.setStatus(status);
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

		if (isEnableSimulators()){
			System.out.println("HB Status: " + s);
		}
	}

	/**
	 * Set the output gain of the device in an envelope.
	 * @param new_gain the new target gain to set device to
	 * @param millseconds the duration of the segment to reach the new gain in milliseconds
	 */
	public void setGain (float new_gain, float millseconds){
		masterGainEnv.addSegment(new_gain, millseconds);
		synchronized (statusChangedListenerList)
		{
			for (StatusChangedListener listener : statusChangedListenerList) {
				listener.gainChanged(new_gain);
			}
		}

		if (isEnableSimulators()){
			System.out.println("Gain changed to : " + new_gain);
		}

	}

	/**
	 * Get the current gain of the device
	 * @return the current masterGain envelope value
	 */
	public float getGain(){
		return masterGainEnv.getCurrentValue();
	}
	/**
	 * Send a message that a class has been loaded
	 * @param incomingClass the class that has been loaded
	 */
	void sendClassLoaded(Class<? extends HBAction> incomingClass){
		String class_name = incomingClass.getSimpleName();
		//setStatus("Successful Load " + class_name);
		synchronized (statusChangedListenerList)
		{
			for (StatusChangedListener listener : statusChangedListenerList) {
				listener.classLoadedMessage(incomingClass);
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
	public static DynamicControl createDynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value)
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
	 * @return Creates a DynamicControl for sending values to other sketches
	 */
	public static DynamicControl createDynamicControl(Object parent_sketch, ControlType control_type, String name)
	{
		return new DynamicControl(parent_sketch, control_type, name);
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
	public static DynamicControl createDynamicControl(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
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
	public static DynamicControl createDynamicControl(ControlType control_type, String name, Object initial_value, Object min_value, Object max_value) {
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
	public static DynamicControl createDynamicControl(ControlType control_type, String name, Object initial_value) {
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
	public static DynamicControl createDynamicControl(ControlType control_type, String name) {
		return new DynamicControl(control_type, name);
	}

	/**
	 * A dynamic control pair that can be accessed from outside
	 * it is created with the sketch object that contains it along with the type
	 * only a single dynamic control is returned becasue the buddy is a mirror
	 *
	 * @param parent_sketch the object calling - typically this
	 * @param control_type  The type of control you want to create
	 * @param name          The name we will give to differentiate between different controls in this class
	 * @param initial_value The initial value of the control
	 * @param min_value     The minimum value of the control
	 * @param max_value     The maximum value of the control
	 * @return Returns the text control object, so max and min are hidden.
	 */
	public static DynamicControl createControlBuddyPair(Object parent_sketch, ControlType control_type, String name, Object initial_value, Object min_value, Object max_value)
	{
		DynamicControl text_control = createDynamicControl(parent_sketch, control_type, name, initial_value).setControlScope(ControlScope.SKETCH);

		DynamicControl slider_control = createDynamicControl(parent_sketch, control_type, name, initial_value, min_value, max_value).setControlScope(ControlScope.SKETCH);

		text_control.addControlScopeListener(new_scope -> {
			slider_control.setControlScope(new_scope);
		});

		/*************************************************************
		 * We return the text control because returning the slider
		 * sometimes displays wrong value in GUI when using a pair
		 * Text control gives best behaviour when setting via setValue
		 ************************************************************/
		 return text_control;
	}

	/**
	 * Create a new Clock using HappyBrackets scheduler
	 * @param interval int6erval in milliseconds. Can use fractions of a millisecond
	 * @return Clock object
	 */
	public static net.happybrackets.core.scheduling.Clock createClock(double interval){
		return new net.happybrackets.core.scheduling.Clock(interval);
	}

	/*********************************************************************
	 * Default accelerometer
	 ********************************************************************/
	static Accelerometer defaultAccelerometer = null;


	private static Accelerometer getDefaultAccelerometer(){
		synchronized (sensorLock) {
			if (defaultAccelerometer == null) {
				defaultAccelerometer = (Accelerometer) HBInstance.getSensor(Accelerometer.class);
			}
			if (isEnableSimulators()) {
				defaultAccelerometer.reloadSimulation();
			}
		}
		return defaultAccelerometer;
	}

	/*********************************************************************
	 * Default accelerometer
	 ********************************************************************/
	static Gyroscope defaultGyroscope = null;


	private static Gyroscope getDefaultGyroscope(){
		synchronized (sensorLock) {
			if (defaultGyroscope == null) {
				defaultGyroscope = (Gyroscope) HBInstance.getSensor(Gyroscope.class);
			}
			if (isEnableSimulators()) {
				{
					defaultGyroscope.reloadSimulation();
				}
			}
		}
		return defaultGyroscope;
	}

	/**
	 * Return a UGen that is mapped to Accelerometer X value
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_X(){
		return getAccelerometer_X(-1, 1);
	}
	/**
	 * Return a UGen that is mapped to Accelerometer X value
	 * @param scale_min the value for axis pointing to ground
	 * @param scale_max the value for axis pointing to sky
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_X(double scale_min, double scale_max){
		UGen ret;
		Accelerometer accel = getDefaultAccelerometer();
		if (accel != null){
			ret = new Glide(0);

			accel.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					float val = accel.getAccelerometerX();
					ret.setValue(Sensor.scaleValue(-1, 1, scale_min, scale_max, val));
				}
			});
		}
		else
		{
			sendStatus("Unable to find accelerometer");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return a UGen that is mapped to Accelerometer Y value
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_Y(){
		return getAccelerometer_Y(-1, 1);
	}

	/**
	 * Return a UGen that is mapped to Accelerometer Y value
	 * @param scale_min the value for axis pointing to ground
	 * @param scale_max the value for axis pointing to sky
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_Y(double scale_min, double scale_max){
		UGen ret;
		Accelerometer accel = getDefaultAccelerometer();
		if (accel != null){
			ret = new Glide(0);

			accel.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					float val = accel.getAccelerometerY();
					ret.setValue(Sensor.scaleValue(-1, 1, scale_min, scale_max, val));
				}
			});
		}
		else
		{
			sendStatus("Unable to find accelerometer");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return a UGen that is mapped to Accelerometer Z value
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_Z(){
		return getAccelerometer_Z(-1, 1);
	}
	/**
	 * Return a UGen that is mapped to Accelerometer Z value
	 * @param scale_min the value for axis pointing to ground
	 * @param scale_max the value for axis pointing to sky
	 * @return UGen object mapped to the axis. If no accelerometer is found, will return a static with value 0
	 */
	public static UGen getAccelerometer_Z(double scale_min, double scale_max){
		UGen ret;
		Accelerometer accel = getDefaultAccelerometer();
		if (accel != null){
			ret = new Glide(0);

			accel.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					float val = accel.getAccelerometerZ();
					ret.setValue(Sensor.scaleValue(-1, 1, scale_min, scale_max, val));
				}
			});
		}
		else
		{
			sendStatus("Unable to find accelerometer");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return a UGen that is mapped to Gyroscope Yaw value
	 * @return UGen object mapped to the axis. If no gyroscope is found, will return a static with value 0
	 */
	public static UGen getGyroscopeYaw(){
		UGen ret;
		Gyroscope gyro = getDefaultGyroscope();
		if (gyro != null){
			ret = new Glide(0);

			gyro.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					ret.setValue(gyro.getYaw());
				}
			});
		}
		else
		{
			sendStatus("Unable to find gyroscope");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return a UGen that is mapped to Gyroscope Pitch value
	 * @return UGen object mapped to the axis. If no gyroscope is found, will return a static with value 0
	 */
	public static UGen getGyroscopePitch(){
		UGen ret;
		Gyroscope gyro = getDefaultGyroscope();
		if (gyro != null){
			ret = new Glide(0);

			gyro.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					ret.setValue(gyro.getPitch());
				}
			});
		}
		else
		{
			sendStatus("Unable to find gyroscope");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return a UGen that is mapped to Gyroscope Roll value
	 * @return UGen object mapped to the axis. If no gyroscope is found, will return a static with value 0
	 */
	public static UGen getGyroscopeRoll(){
		UGen ret;
		Gyroscope gyro = getDefaultGyroscope();
		if (gyro != null){
			ret = new Glide(0);

			gyro.addValueChangedListener(new SensorValueChangedListener() {
				@Override
				public void sensorUpdated(Sensor sensor) {
					ret.setValue(gyro.getRoll());
				}
			});
		}
		else
		{
			sendStatus("Unable to find gyroscope");
			ret = new Static(HBInstance.ac, 0);
		}
		return ret;
	}

	/**
	 * Return the {@link InetAddress} associated with this device name that we can address that device with
	 * If the device is this, then it will return the loopback address
	 * @param name The name of the device we require the address of
	 * @return The address associated with the name. If not stored will return null
	 */
	public synchronized InetAddress getDeviceAddress(String name){
		return deviceAddressByHostname.get(name);
	}

	/**
	 * Get the names of the devices that we know are on the network.
	 * The devices can be targeted with Dynamic controls with {@link ControlScope#TARGET} using the device name.
	 * If you require the {@link InetAddress}of the device, for example, if you want to use OpenSoundControl,
	 * then use the device name in the function {@link #getDeviceAddress(String)}
	 * @return a collection of device names that we know about
	 */
	public synchronized Collection<String> getKnownDeviceNames(){

		return deviceAddressByHostname.keySet();
	}

	/**
	 * Add a {@link DeviceConnectedEventListener} to be informed when a new device is detected on the network
	 * @param listener the listener that will be notified when a device is connected
	 */
	public void addDeviceConnectedEventListener(DeviceConnectedEventListener listener){
		deviceConnectedEventsListeners.add(listener);
	}

	/**
	 * Associate a device name with an {@link InetAddress}
	 * @param name The device name to use as a key
	 * @param address the {@link InetAddress} to associate with the name
	 * @return true if the device was added or update
	 */
	public synchronized boolean addDeviceAddress(String name, InetAddress address) {
		boolean ret = true;
		InetAddress stored = deviceAddressByHostname.get(name);
		if (stored != null) {
			if (stored.getHostAddress().equalsIgnoreCase(address.getHostAddress())) {
				ret = false;
			}
		}

		deviceAddressByHostname.put(name, address);

		if (ret) {
			for (DeviceConnectedEventListener listener:
					deviceConnectedEventsListeners) {
				listener.deviceConnected(name, address);
			}
		}

		return ret;
	}

	/**
	 * Detect whether this {@link InetAddress} is actually this device
	 * @param address the address we are testing for
	 * @return true if this is one of our addresses
	 */
	static public boolean isOurAddress(InetAddress address){
		boolean ret = false;
		if (address != null){
			String target_address = address.getHostAddress();
			String loopback = InetAddress.getLoopbackAddress().getHostAddress();

			if (target_address.equalsIgnoreCase(loopback)){
				ret = true;
			}
			else
			{
				try {
					if (InetAddress.getLocalHost().getHostAddress().equalsIgnoreCase(target_address)){
						ret = true;
					}
				} catch (UnknownHostException e) {
					//e.printStackTrace();
				}
			}
		}

		return ret;
	}

	/**
	 * Get the global {@link HBScheduler}
	 * @return The global scheduler
	 */
	static public HBScheduler getScheduler(){
		return HBScheduler.getGlobalScheduler();
	}

	/**
	 * Get the amount of time elapsed since we set reference time in {@link HBScheduler}
	 * @return the elapsed time in milliseconds
	 */
	static public double getSchedulerTime(){
		return getScheduler().getSchedulerTime();
	}

	/**
	 * Get a scheduler time in the future from {@link #getSchedulerTime()}.
	 * @param milliseconds the number of milliseconds into the future we want the time for
	 * @return {@link #getSchedulerTime()} + milliseconds
	 */
	static public double getFutureSchedulerTime(double milliseconds){
		return getScheduler().getSchedulerTime() + milliseconds;
	}


	/**
	 * Set the global {@link HBScheduler} scheduled time to this time
	 * @param new_time the new time
	 */
	static public void setScheduleTime(double new_time){
		getScheduler().setScheduleTime(new_time);
	}

	/**
	 * Adjust the {@link HBScheduler} scheduler time
	 * @param amount the amount of milliseconds we need to adjust our time by. A positive amount will advance the scheduler
	 * @param duration the number of milliseconds over which we want this change to occur so we don't just get a jump
	 */
	static public void adjustScheduleTime(double amount, long duration){
		getScheduler().adjustScheduleTime(amount, duration);
	}

	/**
	 * Synchronise {@link HBScheduler} timer with the system time on next tick
	 * @return the number of milliseconds we will be moving scheduler
	 */
	static public double synchroniseClocks(){
		return getScheduler().synchroniseClocks(0);
	}

	/**
	 * Synchronise global {@link HBScheduler} timer with the system time
	 * @param slew_time the amount of milliseconds that we want to take to complete it
	 * @return the number of milliseconds we will be moving scheduler
	 */
	static public double synchroniseClocks(long slew_time){
		return synchroniseClocks(slew_time);
	}


	/**
	 * Send a schedule adjustment message to one or more devices.
	 * Will set adjust {@link HBScheduler} on that device
	 * @param amount the amount of milliseconds we need to adjust our time by. A positive amount will advance the scheduler
	 * @param duration the number of milliseconds over which we want this change to occur so we don't just get a jump
	 * @param targets The names of HB devices. If this is null, will send a broadcast
	 *
	 * @return true if able to send to at least one address
	 */
	static public boolean sendScheduleChange(double amount, long duration, Collection<String> targets){
		ClockAdjustment adjustmentMessage = new ClockAdjustment(amount, duration);

		// encode our message
		OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.ADJUST, adjustmentMessage);

		return NetworkCommunication.sendNetworkOSCMessages(message, targets, false);
	}

	/**
	 * Send a Schedule Set time to one or more targets
	 * @param new_time the new time to set our device scheduler to
	 * @param targets The names of HB devices. If this is null, will send a broadcast
	 *
	 * @return true if able to send to at least one address
	 */
	static public boolean sendScheduleSetTime(double new_time, Collection<String> targets){
		ClockAdjustment adjustmentMessage = new ClockAdjustment(new_time, 0);

		// encode our message
		OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.SET, adjustmentMessage);

		return NetworkCommunication.sendNetworkOSCMessages(message, targets, false);
	}
}
