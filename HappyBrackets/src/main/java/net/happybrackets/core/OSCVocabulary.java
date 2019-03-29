package net.happybrackets.core;

import de.sciss.net.OSCMessage;

/**
 * This module will list the messages sent and received via OSC
 * Using this class will remove possibility of typo mistakes
 * furthermore, it will allow us to find usages througoht code
 * Also, it allows us to change the message in one place
 * It allows us to use intellij to suggest value
 */
public final class OSCVocabulary {

    public final class Device
    {
        public static final String LOG = "/device/log";
        public static final String SET_ID = "/device/set_id";
        public static final String SET_NAME = "/device/set_name";
        public static final String GET_LOGS = "/device/get_logs";
        public static final String SYNC = "/device/sync";
        public static final String REBOOT = "/device/reboot";
        public static final String SHUTDOWN = "/device/shutdown";
        public static final String GAIN ="/device/gain";
        public static final String RESET = "/device/reset";
        public static final String RESET_SOUNDING = "/device/reset_sounding";
        public static final String CLEAR_SOUND = "/device/clearsound";
        public static final String FADEOUT_RESET = "/device/fadeout_reset";
        public static final String FADEOUT_CLEAR_SOUND = "/device/fadeout_clearsound";
        public static final String BLEEP = "/device/bleep";
        public static final String CONFIG_WIFI = "/device/config/wifi";
        public static final String ALIVE = "/device/alive";
        public static final String STATUS = "/device/status";
        public static final String VERSION = "/device/version";
        public static final String FRIENDLY_NAME = "/device/friendly";
        public static final String SET_ENCRYPTION = "/device/encrypt";
    }

    public final class DynamicControlMessage
    {
        public static final String CONTROL = "/control";
        public static final String GET = CONTROL + "/get";
        public static final String UPDATE = CONTROL + "/update";
        public static final String GLOBAL = CONTROL + "/global";
        public static final String CREATE = CONTROL + "/create";
        public static final String DESTROY = CONTROL + "/destroy";
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
