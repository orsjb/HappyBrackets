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

}
