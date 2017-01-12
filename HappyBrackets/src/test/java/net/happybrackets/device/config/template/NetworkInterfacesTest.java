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