package net.happybrackets.device.config.template;

/**
 * This class provides a static function to generate a valid interfaces files with the provided ssid and psk
 *
 * This is a bit of a hack to give us some basic functionality we need without adopting a full blown templating library.
 *
 * In the future we might consider palming this off to a jvm based scripting language.
 *
 */
public class NetworkInterfaces {

    public static String newConfig(String ssid, String psk) {
        //Java, why can't I use multi-line strings? and what about heredocs!
        return "auto lo\n" +
                "\n" +
                "iface lo inet loopback\n" +
                "iface eth0 inet dhcp\n" +
                "\n" +
                "allow-hotplug wlan0\n" +
                "auto wlan0\n" +
                "\n" +
                "iface wlan0 inet dhcp\n" +
                "        wpa-ssid \"" + ssid + "\"\n" +
                "        wpa-psk \"" + psk + "\"";
    }
}
