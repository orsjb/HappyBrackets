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

package net.happybrackets.intellij_plugin.controller.network;

import de.sciss.net.*;
import net.happybrackets.core.*;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.scheduling.ClockAdjustment;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.device.network.UDPCachedMessage;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import net.happybrackets.intellij_plugin.controller.gui.DynamicControlScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;

public class LocalDeviceRepresentation implements FileSender.FileSendStatusListener {

    public static final int MAX_LOG_DISPLAY_CHARS = 5000;
    final static Logger logger = LoggerFactory.getLogger(LocalDeviceRepresentation.class);
    private static final int MAX_UDP_SENDS = 3; // use this number of messages to account for
    // create a set of listeners to see if any change in connection happens
    private static List<DeviceConnectedUpdateListener> globalConnectedUpdateListenerList = new ArrayList<>();
    public final String deviceName;
    public final String hostName;
    public final boolean isFakeDevice;
    public final boolean[] groups;
    final Object dynamicControlLock = new Object();
    private final Object clientLock = new Object(); // define a lock for tcpClient
    private final OSCUDPSender server = new OSCUDPSender();
    private final int replyPort; // this is the port where we will tell devices to send our messages
    private final Object[] replyPortObject; //we will use this as a cached Object to send in OSC Message
    private final Object dynamicControlsLock = new Object();
    // define object to prevent assyncronous list access
    private final Object deviceRemovedListenerListLock = new Object();
    private final Object favouriteChangedListenersLock = new Object();
    private final Object deviceIdUpdateListenerListLock = new Object();
    private final Object statusUpdateListenerListLock = new Object();
    private final Object printConfigListenerListLock = new Object();
    private final Object gainChangedListenerListLock = new Object();
    private final Object connectedUpdateListenerListLock = new Object();
    private final Object friendlyNameListenerListLock = new Object();
    private final Object loggingStateListenerLock = new Object();
    private final Object socketAddressChangedListenerListLock = new Object();
    private final Object addDynamicControlListenerListLock = new Object();
    private final Object removeDynamicControlListenerListLock = new Object();
    private final Object errorListenerListLock = new Object();
    private final Object logListenerListLock = new Object();
    public long lastTimeSeen;
    public List<String> preferredAddressStrings;    //This list contains, in order of preference: address, hostName, deviceName, hostname.local or deviceName.local.
    DatagramSocket advertiseTxSocket = null;
    String simulatorHomePath = ""; // this variable will only be set on localhost
    int serverPort = 0; // This is TCP Server Port for standard control comms. We need to create a controlCommsClient to connect to this
    boolean controlRequestSent = false;
    boolean invalidVersion = false;
    private String friendlyName = "";
    private String address;
    private int deviceId; //
    private String status = "Status unknown"; // This is the displayed ID
    private DynamicControlScreen dynamicControlScreen = null;
    private InetSocketAddress socketAddress;
    private OSCClient controlCommsClient = null;
    private ControllerConfig controllerConfig;
    private boolean loggingEnabled = false;
    private Map<String, DynamicControl> dynamicControls = Collections.synchronizedMap(new Hashtable<String, DynamicControl>());
    private Queue<DynamicControl> pendingControls = new LinkedList<DynamicControl>();
    private Object pendingControlsLock = new Object();
    private boolean isConnected = true;
    private boolean ignoreDevice = false;
    private boolean isFavouriteDevice = false;
    private boolean encryptionEnabled = false;
    private FileSender fileSender = null;
    private int majorVersion = 0;
    private int minorVersion = 0;
    private int buildVersion = 0;
    private int dateVersion = 0;
    private List<StatusUpdateListener> statusUpdateListenerList = new ArrayList<>();
    private List<StatusUpdateListener> friendlyNameListenerList = new ArrayList<>();
    private List<StatusUpdateListener> printConfigListenerList = new ArrayList<>();
    private List<GainChangedListener> gainChangedListenerList = new ArrayList<>();
    private List<ConnectedUpdateListener> connectedUpdateListenerList = new ArrayList<>();
    private List<ConnectedUpdateListener> loggingStateListener = new ArrayList<>();
    private List<SocketAddressChangedListener> socketAddressChangedListenerList = new ArrayList<>();
    private List<DeviceIdUpdateListener> deviceIdUpdateListenerList = new ArrayList<>();
    private List<DeviceRemovedListener> deviceRemovedListenerList = new ArrayList<>();
    private List<FavouriteChangedListener> favouriteChangedListeners = new ArrayList<>();
    private List<DynamicControl.DynamicControlListener> addDynamicControlListenerList = new ArrayList<>();
    private List<DynamicControl.DynamicControlListener> removeDynamicControlListenerList = new ArrayList<>();
    private List<ErrorListener> errorListenerList = new ArrayList<>();
    private String currentLogPage = "";
    private ArrayList<String> completeLog = new ArrayList<String>();
    private List<LogListener> logListenerList = new ArrayList<>();

    // Overload constructors. Construct with a SocketAddress
    public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, ControllerConfig config, InetSocketAddress socketAddress, int reply_port) {
        this(deviceName, hostname, addr, id, config, reply_port, false);
        this.socketAddress = socketAddress;

        try {
            advertiseTxSocket = new DatagramSocket();
            advertiseTxSocket.setBroadcast(true);
        } catch (Exception ex) {
        }
    }

    public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, ControllerConfig config, int reply_port, boolean isFakeDevice) {
        // We will set timeDisplayed so it will not make a request for a control until it has been set by the display Cell
        replyPort = reply_port;
        replyPortObject = new Object[]{replyPort};
        this.deviceName = deviceName;
        this.hostName = hostname;
        this.address = addr;
        this.socketAddress = null;
        this.deviceId = id;
        this.controllerConfig = config;
        this.isFakeDevice = isFakeDevice;
        groups = new boolean[4];

        this.isConnected = true;

        dynamicControlScreen = new DynamicControlScreen(this);

        dynamicControlScreen.createDynamicControlStage();

        // Set-up log monitor.
        currentLogPage = "";

        try {
            advertiseTxSocket = new DatagramSocket();
            advertiseTxSocket.setBroadcast(true);
        } catch (Exception ex) {
        }
    }

    public static void addDeviceConnectedUpdateListener(DeviceConnectedUpdateListener listener) {
        globalConnectedUpdateListenerList.add(listener);
    }

    /**
     * Returns whether we are the simulator on the local host
     *
     * @return true if we are a simulator
     */
    public boolean isLocalSimulator() {
        boolean ret = !simulatorHomePath.isEmpty();

        // if there is no path, check if it is on loopback address
        if (!ret) {
            try {
                String this_device = Device.getDeviceName();

                ret = this_device.equalsIgnoreCase(deviceName);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Get if we are sending a file
     *
     * @return true if we are sendinmg a file
     */
    public boolean getFileIsSending() {
        boolean ret = false;
        if (fileSender != null) {
            ret = fileSender.isSending();
        }
        return ret;
    }

    /**
     * Cancel semnding of file if we are seninf a file
     */
    public void cancelSendFile() {
        if (fileSender != null) {
            fileSender.cancelSend();
        }
    }

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(boolean enabled) {
        encryptionEnabled = enabled;
        if (encryptionEnabled) {
            setStatus("Encryption Enabled");
        } else {
            setStatus("Encryption Disabled");
        }
        send(OSCVocabulary.Device.SET_ENCRYPTION, new Object[]{enabled ? 1 : 0});
    }

    /**
     * Cause device to print config to log file
     */
    public boolean sendPrintConfig() {

        boolean ret = false;
        if (openControlPort()) {
            send(OSCVocabulary.DeviceConfig.PRINT_CONFIG);
            ret = true;
        }

        return ret;

    }

    /**
     * Send the selected file to the the device
     *
     * @param source_file the file we are sending
     * @param target_path the target path on device
     * @return true if able to send
     */
    public boolean sendFileToDevice(String source_file, String target_path) {


        boolean ret = false;

        if (openControlPort()) {
            if (fileSender != null) {
                ret = fileSender.addFile(source_file, target_path);
            }
        }

        return ret;
    }

    /**
     * Get the Address we use to access this device over the network
     *
     * @return the network Address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Close the Client TCP Port
     */
    void closeClientPort() {
        synchronized (clientLock) {
            if (controlCommsClient != null) {
                try {
                    if (controlCommsClient.isConnected()) {
                        controlCommsClient.stop();
                        controlCommsClient.dispose();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                controlCommsClient = null;
            }
        }
    }

    /**
     * Open TCP Client and assign listeners
     *
     * @param port Port to connect to
     * @return true on success
     */
    boolean openClientPort(int port) {
        boolean ret = false;
        synchronized (clientLock) {
            if (controlCommsClient == null) {

                try {
                    controlCommsClient = OSCClient.newUsing(OSCChannel.TCP);
                    controlCommsClient.setTarget(new InetSocketAddress(getAddress(), port));
                    controlCommsClient.start();

                    fileSender = new FileSender(controlCommsClient);
                    fileSender.addWriteStatusListener(this);
                    ret = true;

                    controlCommsClient.addOSCListener(new OSCListener() {
                        public void messageReceived(OSCMessage m, SocketAddress addr, long time) {
                            incomingMessage(m, addr);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    controlCommsClient.dispose();
                    controlCommsClient = null;
                }

            }
        }

        return ret;
    }

    /**
     * Check if our TCP controlCommsClient is assigned and connected
     *
     * @return true if connected
     */
    boolean testClientOpen() {
        boolean ret = false;
        synchronized (clientLock) {
            if (controlCommsClient != null) {
                ret = controlCommsClient.isConnected();
            }

        }
        return ret;

    }

    /**
     * Set the port we need to connect our controlCommsClient to to communicate via TCP
     *
     * @param port remote port number
     */
    public synchronized void setServerPort(int port) {

        if (serverPort != port) {
            closeClientPort();
            serverPort = port;

        }

    }

    /**
     * get The friendly name we want to display this device as
     *
     * @return the friendly name. If not set, will return device name
     */
    public String getFriendlyName() {
        String ret = friendlyName;
        if (friendlyName.isEmpty()) {
            ret = deviceName;
        }
        return ret;
    }

    /**
     * Set the friendly name for this device
     *
     * @param friendlyName THe name we want to have this device displayed as
     */
    public void setFriendlyName(String friendlyName) {
        boolean changed = !friendlyName.equals(this.friendlyName);

        if (changed) {
            this.friendlyName = friendlyName;
            synchronized (friendlyNameListenerListLock) {
                for (StatusUpdateListener listener : friendlyNameListenerList) {
                    listener.update(getFriendlyName());
                }
            }

            if (dynamicControlScreen != null) {
                dynamicControlScreen.setTitle(getFriendlyName());
            }

        }
    }

    /**
     * Get DynamicControl Screen
     *
     * @return The dynamicControlScreen associated with this device
     */
    public DynamicControlScreen getDynamicControlScreen() {
        return dynamicControlScreen;
    }

    /**
     * If true, we will ignore this device and not respond to any of its messages
     *
     * @return true if we are ignoring
     */
    public boolean isIgnoringDevice() {
        return ignoreDevice;
    }

    /**
     * Set whether we will ignore this device. We will set status of device
     *
     * @param ignoreDevice true if we want to ignore
     */
    public void setIgnoreDevice(boolean ignoreDevice) {
        this.ignoreDevice = ignoreDevice;
        if (ignoreDevice) {
            setStatus("Ignore Device");
        } else {
            setStatus("Stoped Ignoring");
            sendStatusRequest();
        }
    }

    public boolean isFavouriteDevice() {
        return isFavouriteDevice;
    }

    public void setFavouriteDevice(boolean enabled) {
        if (isFavouriteDevice != enabled) {
            this.isFavouriteDevice = enabled;
            synchronized (favouriteChangedListenersLock) {
                for (FavouriteChangedListener deviceModifiedListener : favouriteChangedListeners) {
                    deviceModifiedListener.favouriteChanged(this);
                }
            }
        }
    }

    /**
     * Return if the device has it's status as actively connected
     *
     * @return true if connected
     */
    public boolean getIsConnected() {
        return this.isConnected;
    }

    public void setIsConnected(boolean connected) {

        if (isConnected != connected) {
            this.isConnected = connected;
            synchronized (connectedUpdateListenerListLock) {
                for (ConnectedUpdateListener listener : connectedUpdateListenerList) {
                    listener.update(connected);
                }
            }
            sendConnectionListeners();
            setStatus(isConnected ? "Connected" : "Disconnected");
        }


        if (!connected) {
            controlRequestSent = false;
        }

    }

    /**
     * If our major and minor version do not match plugin, we will be an invalid version
     *
     * @return true if plugin and device do not match major and minor
     */
    public boolean isInvalidVersion() {
        return invalidVersion;
    }

    /**
     * Get the HB Version of this device as a string
     *
     * @return Device HB Version
     */
    public String getVersionText() {
        return majorVersion + "." + minorVersion + "." + buildVersion + "." + dateVersion;
    }

    /**
     * Return a message to display to the user that their device veriosn is incompatible
     * with plugin
     *
     * @return warning message
     */
    public String getInvalidVersionWarning() {
        return "Invalid device version. Device has " + getVersionText() + ". Must be " + BuildVersion.getMinimumCompatibilityVersion();
    }

    public void setVersion(int major, int minor, int build, int date) {
        majorVersion = major;
        minorVersion = minor;
        buildVersion = build;
        dateVersion = date;

        if (BuildVersion.getMajor() != majorVersion ||
                BuildVersion.getMinor() != minorVersion) {
            invalidVersion = true;
        }


        String status_text = "V: " + getVersionText();
        setStatus(status_text);
    }

    @Override
    public void writingFile(String filename) {
        setStatus("Writing " + filename);
    }

    @Override
    public void writeSuccess(String filename) {
        setStatus("Wrote " + filename);
    }

    @Override
    public void writeError(String filename) {
        setStatus("Error " + filename);
    }

    /**
     * Set the device to our Time
     *
     * @return true if able to send the message
     */
    public boolean synchroniseDevice() {
        boolean ret = false;

        if (advertiseTxSocket != null) {
            double current_time = HBScheduler.getCalcTime();
            ClockAdjustment adjustmentMessage = new ClockAdjustment(current_time, 0);

            // encode our message
            OSCMessage message = HBScheduler.buildNetworkSendMessage(OSCVocabulary.SchedulerMessage.SET, adjustmentMessage);


            int device_port = controllerConfig.getControlToDevicePort();
            UDPCachedMessage cached_message = null;
            try {
                cached_message = new UDPCachedMessage(message);
                DatagramPacket packet = cached_message.getCachedPacket();
                packet.setAddress(getSocketAddress().getAddress());
                packet.setPort(device_port);

                for (int i = 0; i < MAX_UDP_SENDS; i++) {
                    try {
                        advertiseTxSocket.send(packet);
                        ret = true;
                    } catch (IOException e) {
                        System.out.println("Unable to broadcast");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return ret;
    }

    public void sendResetConfiguration() {
        send(OSCVocabulary.DeviceConfig.DELETE_CONFIG, replyPortObject);
    }

    public void addDynamicControlListenerCreatedListener(DynamicControl.DynamicControlListener listener) {
        synchronized (addDynamicControlListenerListLock) {
            addDynamicControlListenerList.add(listener);
        }
    }

    public void removeDynamicControlListenerCreatedListener(DynamicControl.DynamicControlListener listener) {
        synchronized (addDynamicControlListenerListLock) {
            addDynamicControlListenerList.remove(listener);
        }
    }

    public void addDynamicControlListenerRemovedListener(DynamicControl.DynamicControlListener listener) {
        synchronized (removeDynamicControlListenerListLock) {
            removeDynamicControlListenerList.add(listener);
        }
    }

    public void addFavouriteListener(FavouriteChangedListener listener) {
        synchronized (favouriteChangedListenersLock) {
            favouriteChangedListeners.add(listener);
        }
    }

    public void removeFavouriteListener(FavouriteChangedListener listener) {
        synchronized (favouriteChangedListenersLock) {
            favouriteChangedListeners.remove(listener);
        }
    }

    public void removeDynamicControlListenerRemovedListener(DynamicControl.DynamicControlListener listener) {
        synchronized (removeDynamicControlListenerListLock) {
            removeDynamicControlListenerList.add(listener);
        }
    }

    void sendInitialControlRequest() {

        if (!controlRequestSent) {
            controlRequestSent = true;
            sendControlsRequest();
        }
    }

    /**
     * Open the control Port for TCP Communication
     *
     * @return true if port is opened. If not opened, it will open itm and on success, return true
     */
    public boolean openControlPort() {
        boolean ret = true;
        if (!testClientOpen()) {
            ret = openClientPort(serverPort);
        }

        return ret;
    }

    /**
     * Open TCP Port and show the control screen
     *
     * @return TRue if a TCP connection was made and controls could be shown
     */
    public boolean showControlScreen() {
        boolean ret = false;


        openControlPort();

        // we will only show control screen if we have a valid TCP port
        if (testClientOpen()) {
            if (!controlRequestSent) {
                sendInitialControlRequest();
            }

            dynamicControlScreen.setTitle(getFriendlyName());
            dynamicControlScreen.show();
            ret = true;
        }
        return ret;
    }

    /**
     * Add A dynamic Control
     *
     * @param control The DynamicControl we are making
     */
    public void addDynamicControl(DynamicControl control) {
        synchronized (dynamicControlLock) {

            synchronized (dynamicControlsLock) {
                dynamicControls.put(control.getControlMapKey(), control);
            }

            synchronized (pendingControlsLock) {
                pendingControls.add(control);
            }

            dynamicControlScreen.loadDynamicControls(this);

            synchronized (addDynamicControlListenerListLock) {
                for (DynamicControl.DynamicControlListener listener : addDynamicControlListenerList) {
                    listener.update(control);
                }
            }
            control.addControlListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {

                    System.out.println("Dynamic Control value changed");
                }
            });

            // add listener so we will send changes that occu to control back to device
            control.addValueSetListener(new DynamicControl.DynamicControlListener() {
                @Override
                public void update(DynamicControl control) {
                    sendDynamicControl(control);
                }
            });
        }
    }

    /**
     * Get next control in pending list. Will automatically remove from queue
     *
     * @return next item in queue. If queue is empty will return null
     */
    public DynamicControl popNextPendingControl() {
        DynamicControl ret = null;

        synchronized (pendingControlsLock) {
            ret = pendingControls.poll();
        }
        return ret;
    }

    /**
     * Reset the device and clear dynamic controls
     */
    public void resetDevice() {
        this.send(OSCVocabulary.Device.RESET);
        clearDynamicControls();
    }

    /**
     * We need to remove all dynamic controls From This device
     */
    public void clearDynamicControls() {
        // we need to get the collection synchronised with map
        // or we will get an access vioaltion
        synchronized (pendingControlsLock) {
            pendingControls.clear();
        }

        try {
            dynamicControlScreen.eraseDynamicControls();
        } catch (Exception ex) {
        }

        Collection<DynamicControl> removal_list;
        synchronized (dynamicControlsLock) {
            removal_list = dynamicControls.values();
        }

        for (DynamicControl control : removal_list) {
            control.eraseListeners();
            removeDynamicControl(control);
        }
    }

    /**
     * Remove A dynamic Control
     *
     * @param control The DynamicControl we are removing
     */
    public void removeDynamicControl(DynamicControl control) {
        synchronized (dynamicControlLock) {
            synchronized (dynamicControlsLock) {
                dynamicControls.remove(control.getControlMapKey());
            }

            dynamicControlScreen.removeDynamicControl(control);

            synchronized (removeDynamicControlListenerListLock) {
                for (DynamicControl.DynamicControlListener listener : removeDynamicControlListenerList) {
                    listener.update(control);
                }
            }
        }
    }

    public void sendConnectionListeners() {
        for (DeviceConnectedUpdateListener listener :
                globalConnectedUpdateListenerList) {
            listener.update(this, isConnected);

        }
    }

    /**
     * Process and incoming OSC Message for this device
     *
     * @param msg    the OSC Message
     * @param sender Socket address of sender
     */
    public synchronized void incomingMessage(OSCMessage msg, SocketAddress sender) {
        if (OSCVocabulary.match(msg, OSCVocabulary.Device.STATUS)) {
            processStatusMessage(msg, sender);
        } else if (OSCVocabulary.startsWith(msg, OSCVocabulary.SchedulerMessage.TIME)) {
            HBScheduler.ProcessSchedulerMessage(msg);
        } else if (OSCVocabulary.match(msg, OSCVocabulary.Device.VERSION)) {
            processVersionMessage(msg, sender);

        } else if (OSCVocabulary.match(msg, OSCVocabulary.Device.FRIENDLY_NAME)) {
            //processFriendlyNameMessage(msg, sender);
        } else if (OSCVocabulary.startsWith(msg, OSCVocabulary.DynamicControlMessage.CONTROL)) {
            processDynamicControlMessage(msg, sender);
        } else if (OSCVocabulary.match(msg, OSCVocabulary.Device.LOG)) {
            processLogMessage(msg, sender);
        } else if (OSCVocabulary.match(msg, OSCVocabulary.Device.SIMULATOR_HOME_PATH)) {
            processSimulatorHomeMessage(msg, sender);
        } else if (OSCVocabulary.match(msg, OSCVocabulary.Device.GAIN)) {
            processGainMessage(msg, sender);
        } else if (OSCVocabulary.startsWith(msg, OSCVocabulary.DeviceConfig.CONFIG)) {
            processDeviceConfigMessage(msg);
        }

    }

    /**
     * Process a gain changed message from device
     *
     * @param msg    the OSC Message
     * @param sender The sender
     */
    private void processGainMessage(OSCMessage msg, SocketAddress sender) {
        try {
            float gain = (float) msg.getArg(0);

            synchronized (gainChangedListenerListLock) {
                for (GainChangedListener gainChangedListener : gainChangedListenerList) {
                    gainChangedListener.gainChanged(gain);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Store the ho me path of the simulator. We can use this to see what project the simulator was launched from
     *
     * @param msg    The OSC Message
     * @param sender the sending address
     */
    private void processSimulatorHomeMessage(OSCMessage msg, SocketAddress sender) {
        simulatorHomePath = (String) msg.getArg(0);
    }

    private synchronized void processLogMessage(OSCMessage msg, SocketAddress sender) {
        String new_log_output = (String) msg.getArg(1);

        // see if our new logpage will exceed our max log page size
        int current_log_length = currentLogPage.length();

        if (current_log_length + new_log_output.length() > MAX_LOG_DISPLAY_CHARS && currentLogPage.length() != 0) {
            completeLog.add(currentLogPage);
            currentLogPage = new String(new_log_output);
        } else {
            currentLogPage = currentLogPage + "\n" + new_log_output;
        }


        //logger.debug("Received new log output from device {} ({}): {}", deviceName, socketAddress, new_log_output);
        synchronized (logListenerListLock) {
            for (LogListener listener : logListenerList) {
                listener.newLogMessage(new_log_output, numberLogPages() - 1);
            }
        }
    }

    /**
     * process alive message from device
     *
     * @param msg    OSC Message
     * @param sender the Socket Address of where the message originated
     */
    private void processStatusMessage(OSCMessage msg, SocketAddress sender) {
        // Lets put some constants here so we can read them
        DeviceStatus status = new DeviceStatus(msg);

        setStatus(status.getStatusText());
        if (loggingEnabled != status.isLoggingEnabled()) {
            loggingEnabled = status.isLoggingEnabled();
            try {
                synchronized (loggingStateListenerLock) {
                    for (ConnectedUpdateListener listener : loggingStateListener) {
                        listener.update(loggingEnabled);
                    }
                }
            } catch (Exception ex) {
            }
        }

        encryptionEnabled = status.isClassEncryption();
    }

    /**
     * Process the Build Version message of this device
     *
     * @param msg    OSC Message
     * @param sender Socket address of sender
     */
    private void processVersionMessage(OSCMessage msg, SocketAddress sender) {
        final int DEVICE_NAME = 0;
        final int DEVICE_MAJOR = 1;
        final int DEVICE_MINOR = 2;
        final int DEVICE_BUILD = 3;
        final int DEVICE_DATE = 4;

        int major, minor, build, date;

        major = (int) msg.getArg(DEVICE_MAJOR);
        minor = (int) msg.getArg(DEVICE_MINOR);
        build = (int) msg.getArg(DEVICE_BUILD);
        date = (int) msg.getArg(DEVICE_DATE);

        setVersion(major, minor, build, date);
    }

    /**
     * Process the Build Version message of this device
     *
     * @param msg    OSC Message
     * @param sender Socket address of sender
     */
    private void processFriendlyNameMessage(OSCMessage msg, SocketAddress sender) {
        final int DEVICE_NAME = 0;
        final int NAME = 1;


        String name = (String) msg.getArg(NAME);

        setFriendlyName(name);
    }

    /**
     * Process Messages with Dynamic Control
     *
     * @param msg    OSC Message
     * @param sender socket addrss of sender
     */
    private void processDynamicControlMessage(OSCMessage msg, SocketAddress sender) {
        final int CONTROL_MAP_KEY = 1;
        String map_key = (String) msg.getArg(CONTROL_MAP_KEY);

        if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.CREATE)) {
            DynamicControl new_control = new DynamicControl(msg);
            addDynamicControl(new_control);
        } else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.DESTROY)) {
            DynamicControl control = ControlMap.getInstance().getControl(map_key);
            if (control != null) {
                control.eraseListeners();
                removeDynamicControl(control);
                ControlMap.getInstance().removeControl(control);
            }
        } else if (OSCVocabulary.match(msg, OSCVocabulary.DynamicControlMessage.UPDATE)) {
            // This call will send an update to all listeners
            DynamicControl.processUpdateMessage(msg);
        }
    }

    /**
     * Set the device to send alive signals back for the period defined by milliseconds
     *
     * @param milliseconds the number of milliseconds to send alive messages back
     */
    public void setAliveInterval(int milliseconds) {
        sendOscMsg(OSCMessageBuilder.createOscMessage(OSCVocabulary.Device.ALIVE, milliseconds));
    }

    /**
     * Process Messages for device config
     *
     * @param msg OSC Message
     */
    private void processDeviceConfigMessage(OSCMessage msg) {
        if (OSCVocabulary.match(msg, OSCVocabulary.DeviceConfig.PRINT_CONFIG)) {
            for (int i = 0; i < msg.getArgCount(); i++) {
                String config = (String) msg.getArg(i);
                displayConfig(config);
            }
        } else if (OSCVocabulary.match(msg, OSCVocabulary.DeviceConfig.DELETE_CONFIG)) {
            for (int i = 0; i < msg.getArgCount(); i++) {
                String config = "Deleted " + (String) msg.getArg(i);
                displayConfig(config);
            }
        }
    }

    /**
     * We will recieve this message and send the dynamic control message back to the device
     *
     * @param dynamic_control The Dynamic Control
     */
    void sendDynamicControl(DynamicControl dynamic_control) {
        OSCMessage msg = dynamic_control.buildUpdateMessage();
        sendOscMsg(msg);
    }

    public final InetSocketAddress getSocketAddress() {
        return this.socketAddress;
    }

    // First test if our stored socket address is the same as the argument
    // If it is different, store new value and raise event to notify that change occurred
    public void setSocketAddress(InetAddress new_socket_address) {
        InetAddress old = null;

        String old_host_address = "";

        if (this.socketAddress != null) {
            old = this.socketAddress.getAddress();
            old_host_address = old.getHostAddress();
        }

        String new_host_address = new_socket_address.getHostAddress();
        boolean same_address = old_host_address.equals(new_host_address);
        if (!same_address) {
            this.address = new_host_address;
            this.socketAddress = new InetSocketAddress(new_socket_address, controllerConfig.getControlToDevicePort());


            synchronized (socketAddressChangedListenerListLock) {
                // now raise event
                for (SocketAddressChangedListener listener : socketAddressChangedListenerList) {
                    listener.socketChanged(old, new_socket_address);
                }
            }
        }
    }

    /**
     * Lets device that know about this to remove their listeners
     * It then removes all listeners from list
     * Also closes the TCP Port
     */
    public void removeDevice() {
        synchronized (deviceRemovedListenerListLock) {
            for (DeviceRemovedListener listener : deviceRemovedListenerList) {
                listener.deviceRemoved(this);
            }
            deviceRemovedListenerList.clear();

        }
        // close our TCP Port

        closeClientPort();

        if (fileSender != null) {
            //cancel any file sends
            fileSender.cancelSend();
            fileSender.clearWriteStatusListeners();
        }

        // Just because a device is removed does not mean it is no longer a favourite
        synchronized (favouriteChangedListenersLock) {
            favouriteChangedListeners.clear();
        }

        synchronized (deviceIdUpdateListenerListLock) {
            deviceIdUpdateListenerList.clear();
        }

        synchronized (statusUpdateListenerListLock) {
            statusUpdateListenerList.clear();
        }

        synchronized (printConfigListenerListLock) {
            printConfigListenerList.clear();
        }

        synchronized (gainChangedListenerListLock) {
            gainChangedListenerList.clear();
        }

        dynamicControlScreen.removeDynamicControlScene();
        dynamicControlScreen = null;


    }

    public int getID() {
        return deviceId;
    }

    /**
     * Set the device Id of the this. If it has changed, it will notify any listeners
     *
     * @param id The ID set for this device
     */
    public void setID(int id) {
        boolean changed = this.deviceId != id;
        this.deviceId = id;

        if (changed) {
            synchronized (deviceIdUpdateListenerListLock) {
                for (DeviceIdUpdateListener listener : deviceIdUpdateListenerList) {
                    listener.update(id);
                }
            }
        }

    }

    /**
     * Send a request to get version number of HappyBrackets from device
     */
    public void sendVersionRequest() {
        send(OSCVocabulary.Device.VERSION, replyPortObject);
        send(OSCVocabulary.Device.FRIENDLY_NAME, replyPortObject);
        send(OSCVocabulary.Device.GAIN, replyPortObject);
    }

    /**
     * Send a request to get the dynamic controls on this device
     */
    public void sendControlsRequest() {
        // clearDynamicControls();
        send(OSCVocabulary.DynamicControlMessage.GET, replyPortObject);
    }

    /**
     * Send a status request to the device
     */
    public void sendStatusRequest() {
        send(OSCVocabulary.Device.STATUS, replyPortObject);
        send(OSCVocabulary.Device.GAIN, replyPortObject);
    }

    private void lazySetupAddressStrings() {
        if (preferredAddressStrings == null) {
            preferredAddressStrings = new LinkedList<>();
            preferredAddressStrings.add(deviceName + ".local");
            preferredAddressStrings.add(address);
            preferredAddressStrings.add(hostName + ".local");
            preferredAddressStrings.add(hostName);
            preferredAddressStrings.add(deviceName);
        }
    }

    public synchronized void sendOscMsg(OSCMessage msg) {
        lazySetupAddressStrings();
        boolean success = false;
        int count = 0;
        if (testClientOpen()) { // if we can send TCP = then lets do it that way
            synchronized (clientLock) {
                try {
                    controlCommsClient.send(msg);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // If we were not able to send TCP
        while (!success) {
            try {
                if (this.socketAddress == null) {
                    this.socketAddress = new InetSocketAddress(preferredAddressStrings.get(0), controllerConfig.getControlToDevicePort());
                }
                if (server.send(msg, socketAddress)) {
                    success = true;
                }
            } catch (UnresolvedAddressException ex) {
                logger.error("Error sending to device {} using address {}! (Setting socketAddress back to null).",
                        deviceName, preferredAddressStrings.get(0));

                //set the socketAddress back to null as it will need to be rebuilt
                socketAddress = null;
                //rotate the preferredAddressStrings list to try the next one in the list
                String failed_string = preferredAddressStrings.remove(0);
                preferredAddressStrings.add(failed_string);
                if (count > 4) break;
                count++;
            }

        }

    }

    public synchronized void send(String msg_name, Object... args) {
        if (isFakeDevice) {
            return;
        }
        OSCMessage msg = new OSCMessage(msg_name, args);
        sendOscMsg(msg);
    }

    public synchronized void send(byte[]... data) {
        if (isConnected) {
            lazySetupAddressStrings();
            boolean success = false;
            int count = 0;
            boolean possible_IP_vIssue = false;
            List<Exception> exceptions = new ArrayList<>(5);
            while (!success) {
                try {
                    String client_address = null;
                    if (this.socketAddress != null) {
                        //client_address = this.socketAddress.getHostName();
                        client_address = this.socketAddress.getAddress().getHostAddress();
                    } else {
                        client_address = preferredAddressStrings.get(0);
                    }

                    Socket s = new Socket(client_address, ControllerConfig.getInstance().getCodeToDevicePort());
                    for (byte[] d : data) {
                        s.getOutputStream().write(d);
                    }
                    s.close();
                    success = true;
                    logger.debug("Success sending to device {} using address {}!",
                            deviceName, preferredAddressStrings.get(0));
                } catch (IOException | IllegalArgumentException e1) {
                    logger.error("Error sending to device {} using address {}! (Setting socketAddress back to null).",
                            deviceName, preferredAddressStrings.get(0), e1);
                    //set the socketAddress back to null as it will need to be rebuilt
                    this.setSocketAddress(null);//socketAddress = null;
                    //rotate the preferredAddressStrings list to try the next one in the list
                    String failedString = preferredAddressStrings.remove(0);    //remove from front
                    preferredAddressStrings.add(failedString);        //add to end

                    exceptions.add(e1);
                    possible_IP_vIssue |= e1 instanceof java.net.SocketException && e1.getMessage().contains("rotocol");
                    if (count > 4) break;
                    count++;
                }
            }

            if (possible_IP_vIssue) {
                logger.error("It looks like there might be an IPv4/IPv6 incompatibility, try setting the JVM option -Djava.net.preferIPv6Addresses=true or -Djava.net.preferIPv4Addresses=true");
            }
            // Communicate the errors to the plugin gui if it's running (and anything else that's listening).
            exceptions.forEach((e) -> sendError("Error sending to device!", e));
        }
    }

    public void addStatusUpdateListener(StatusUpdateListener listener) {
        synchronized (statusUpdateListenerListLock) {
            statusUpdateListenerList.add(listener);
        }
    }

    public void removeStatusUpdateListener(StatusUpdateListener listener) {

        synchronized (statusUpdateListenerListLock) {
            statusUpdateListenerList.remove(listener);
        }
    }

    public void addConfigUpdateListener(StatusUpdateListener listener) {
        synchronized (printConfigListenerListLock) {
            printConfigListenerList.add(listener);
        }
    }

    public void removeConfigUpdateListener(StatusUpdateListener listener) {

        synchronized (printConfigListenerListLock) {
            printConfigListenerList.remove(listener);
        }
    }

    public void addGainChangedListener(GainChangedListener listener) {
        synchronized (gainChangedListenerListLock) {
            gainChangedListenerList.add(listener);
        }
    }

    public void removeGainCHangedListener(GainChangedListener listener) {

        synchronized (gainChangedListenerListLock) {
            gainChangedListenerList.remove(listener);
        }
    }

    public void addFriendlyNameUpdateListener(StatusUpdateListener listener) {
        synchronized (friendlyNameListenerListLock) {
            friendlyNameListenerList.add(listener);
        }
    }

    public void removeFriendlyNameUpdateListener(StatusUpdateListener listener) {
        StatusUpdateListener removal_object = null;

        synchronized (friendlyNameListenerListLock) {
            friendlyNameListenerList.remove(listener);
        }
    }

    public void addConnectedUpdateListener(ConnectedUpdateListener listener) {
        synchronized (connectedUpdateListenerListLock) {
            connectedUpdateListenerList.add(listener);
        }
    }

    public void addLoggingStateListener(ConnectedUpdateListener listener) {
        synchronized (loggingStateListenerLock) {
            loggingStateListener.add(listener);
        }
    }

    public void removeConnectedUpdateListener(ConnectedUpdateListener listener) {
        synchronized (connectedUpdateListenerListLock) {
            connectedUpdateListenerList.remove(listener);
        }
    }

    public void removeLoggingStateListener(ConnectedUpdateListener listener) {
        synchronized (loggingStateListenerLock) {
            loggingStateListener.remove(listener);
        }
    }

    public void addSocketAddressChangedListener(SocketAddressChangedListener listener) {
        synchronized (socketAddressChangedListenerListLock) {
            socketAddressChangedListenerList.add(listener);
        }
    }

    public void addDeviceRemovedListener(DeviceRemovedListener listener) {
        synchronized (deviceRemovedListenerListLock) {
            deviceRemovedListenerList.add(listener);
        }
    }

    public void addDeviceIdUpdateListener(DeviceIdUpdateListener listener) {
        synchronized (deviceIdUpdateListenerListLock) {
            deviceIdUpdateListenerList.add(listener);
        }
    }

    public void removeDeviceIdUpdateListener(DeviceIdUpdateListener listener) {
        synchronized (deviceIdUpdateListenerListLock) {
            deviceIdUpdateListenerList.remove(listener);
        }
    }

    public void addErrorListener(ErrorListener listener) {
        synchronized (errorListenerListLock) {
            errorListenerList.add(listener);
        }
    }

    public void removeErrorListener(ErrorListener listener) {
        synchronized (errorListenerListLock) {
            errorListenerList.remove(listener);
        }
    }

    public String getStatus() {
        return status;
    }

    /**
     * Store new status. If status has changed, will generate an event
     *
     * @param new_status the new status to write
     */
    public void setStatus(String new_status) {


        status = new_status;
        synchronized (statusUpdateListenerListLock) {
            for (StatusUpdateListener statusUpdateListener : statusUpdateListenerList) {
                statusUpdateListener.update(status);
            }
        }
    }

    /**
     * Display a returned config list from device
     *
     * @param config_display the new status to write
     */
    public void displayConfig(String config_display) {
        synchronized (printConfigListenerListLock) {
            for (StatusUpdateListener statusUpdateListener : printConfigListenerList) {
                statusUpdateListener.update(config_display);
            }
        }
    }

    private void sendError(String description, Exception ex) {
        synchronized (errorListenerListLock) {
            for (ErrorListener l : errorListenerList) {
                l.errorOccurred(this.getClass(), description, ex);
            }
        }
    }

    public void addLogListener(LogListener listener) {
        synchronized (logListenerListLock) {
            logListenerList.add(listener);
        }
    }

    public void removeLogListener(LogListener listener) {
        synchronized (logListenerListLock) {
            logListenerList.remove(listener);
        }
    }

    /**
     * Get the log by page
     *
     * @param page the page number
     * @return the log for that page
     */
    public String getDeviceLog(int page) {
        String ret = currentLogPage;
        if (page < completeLog.size()) {
            ret = completeLog.get(page);
        }
        return ret;
    }

    /**
     * Gets the number of pages in our log + our current last page
     *
     * @return number of pages
     */
    public int numberLogPages() {
        return completeLog.size() + 1;
    }

    /**
     * Is Device Logging available for this device
     *
     * @return Whether logging is enabled for device
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * Tell the device to start or stop sending log messages
     *
     * @param enable true if we want logs
     */
    public void setLogging(boolean enable) {
        // we need to send a start logging message to the device
        loggingEnabled = enable;

        send(OSCVocabulary.Device.GET_LOGS, new Object[]{enable ? 1 : 0});

    }

    /**
     * Send a reboot message to the device
     */
    public void rebootDevice() {
        send(OSCVocabulary.Device.REBOOT);
    }

    /**
     * Send a shutdown message to the device
     */
    public void shutdownDevice() {
        send((OSCVocabulary.Device.SHUTDOWN));
    }

    public interface StatusUpdateListener {
        void update(String state);
    }

    public interface ConnectedUpdateListener {
        void update(boolean connected);
    }

    public interface GainChangedListener {
        void gainChanged(float new_gain);
    }

    public interface DeviceConnectedUpdateListener {
        void update(LocalDeviceRepresentation device, boolean connected);
    }

    public interface DeviceIdUpdateListener {
        void update(int new_id);
    }

    public interface SocketAddressChangedListener {
        void socketChanged(InetAddress old_address, InetAddress inet_address);
    }

    public interface DeviceRemovedListener {
        void deviceRemoved(LocalDeviceRepresentation device);
    }

    public interface FavouriteChangedListener {
        void favouriteChanged(LocalDeviceRepresentation device);
    }

    public interface LogListener {
        void newLogMessage(String message, int page);
    }
}
