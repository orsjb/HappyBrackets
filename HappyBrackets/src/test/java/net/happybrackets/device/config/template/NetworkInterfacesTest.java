package net.happybrackets.device.config.template;

import org.junit.Test;

import static org.junit.Assert.*;

import net.happybrackets.device.config.template.NetworkInterfaces;

/**
 * Created by Samg on 22/06/2016.
 */
public class NetworkInterfacesTest {
    @Test
    public void newConfig() throws Exception {
        assertTrue("Generated output matches", NetworkInterfaces.newConfig("PINet", "happybrackets").equals("auto lo\n" +
                    "\n" +
                    "iface lo inet loopback\n" +
                    "iface eth0 inet dhcp\n" +
                    "\n" +
                    "allow-hotplug wlan0\n" +
                    "auto wlan0\n" +
                    "\n" +
                    "iface wlan0 inet dhcp\n" +
                    "        wpa-ssid \"PINet\"\n" +
                    "        wpa-psk \"happybrackets\""
            )
        );
    }

}