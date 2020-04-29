package net.happybrackets.core;

import de.sciss.net.OSCMessage;

import java.util.Collection;

/**
 * This module will list the messages sent and received via OSC
 * Using this class will remove possibility of typo mistakes
 * furthermore, it will allow us to find usages througoht code
 * Also, it allows us to change the message in one place
 * It allows us to use intellij to suggest value
 */
public final class OSCVocabulary {

    /**
     * Create an interface used for sending OSC Messages to various listeners
     */
    public interface OSCAdvertiseListener {
        void OSCAdvertiseEvent(OSCMessage msg, Collection<String> targets);
    }

    /**
     * OSC Name parameters for Device Messages
     */
    public final class Device
    {
        public static final String DEVICE = "/device";

        public static final String LOG = DEVICE  + "/log";
        public static final String SET_ID = DEVICE  + "/set_id";
        public static final String SET_NAME = DEVICE  + "/set_name";
        public static final String GET_LOGS = DEVICE  + "/get_logs";
        public static final String SYNC = DEVICE  + "/sync";
        public static final String REBOOT = DEVICE  + "/reboot";
        public static final String SHUTDOWN = DEVICE  + "/shutdown";
        public static final String GAIN = DEVICE  + "/gain";
        public static final String RESET = DEVICE  + "/reset";
        public static final String RESET_SOUNDING = DEVICE  + "/reset_sounding";
        public static final String CLEAR_SOUND = DEVICE  + "/clearsound";
        public static final String FADEOUT_RESET = DEVICE  + "/fadeout_reset";
        public static final String FADEOUT_CLEAR_SOUND = DEVICE  + "/fadeout_clearsound";
        public static final String BLEEP = DEVICE  + "/bleep";
        public static final String ALIVE = DEVICE  + "/alive";
        public static final String STATUS = DEVICE  + "/status";
        public static final String VERSION = DEVICE  + "/version";
        public static final String FRIENDLY_NAME = DEVICE  + "/friendly";
        public static final String SET_ENCRYPTION = DEVICE  + "/encrypt";
        public static final String CLASS_LOADED = DEVICE  + "/classloaded";
        public static final String SIMULATOR_HOME_PATH = DEVICE  + "/simulatorpath";
    }

    /**
     * OSC names for config messages to device
     */
    public final class DeviceConfig{
        public static final String CONFIG = Device.DEVICE + "/config";

        /**
         * Print the contents of config files and print to Status
         */
        public static final String PRINT_CONFIG =  CONFIG + "/print";

        /**
         * Delete the config files for this device
         */
        public static final String DELETE_CONFIG =  CONFIG + "/delete";

    }

    /**
     * OSC Names for Dynamic control messages
     */
    public final class DynamicControlMessage
    {
        public static final String CONTROL = "/cont";
        public static final String GET = CONTROL + "/get";
        public static final String UPDATE = CONTROL + "/up";
        public static final String GLOBAL = CONTROL + "/global";
        public static final String TARGET = CONTROL + "/target";
        public static final String CREATE = CONTROL + "/create";
        public static final String DESTROY = CONTROL + "/destroy";
        public static final String DEVICE_NAME = CONTROL + "/device";
        public static final String REQUEST_NAME = CONTROL + "/request";
    }

    public final class SchedulerMessage
    {
        public static final String TIME = "/time";
        public static final String SET = TIME + "/set";
        public static final String ADJUST = TIME + "/adj";
        public static final String CURRENT = TIME + "/cur";
        public static final String STRATUM = TIME + "/strat";


    }
    public final class FileSendMessage
    {
        public static final String CONTROL = "/file";
        public static final String START = CONTROL + "/start";
        public static final String WRITE = CONTROL + "/write";
        public static final String COMPLETE = CONTROL + "/complete";
        public static final String CANCEL = CONTROL + "/cancel";
        public static final String ERROR = CONTROL + "/error";
    }

    public final class CONTROLLER
    {
        public static final String CONTROLLER = "/hb/controller";
    }
    /**
     * Test whether an OSC message matches the supplied name
     * Only simple test and does not do wildcards
     * @param msg OSC Message to test
     * @param name the name to test for
     * @return true on a match
     */
    static public boolean match(OSCMessage msg, String name)
    {
        return msg.getName().equals(name);
    }

    /**
     * Test to see whether a message starts with a particular name
     * @param msg the OSC Message
     * @param name the name to test for
     * @return true if message name starts with the name
     */
    static public boolean startsWith(OSCMessage msg, String name)
    {
        return msg.getName().startsWith(name);
    }



}
