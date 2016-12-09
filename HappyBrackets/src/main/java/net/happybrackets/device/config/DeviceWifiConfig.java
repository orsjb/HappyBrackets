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

package net.happybrackets.device.config;

import net.happybrackets.core.config.LoadableConfig;

/**
 * A basic class to hold our wifi config and work with gson
 *
 * Created by Samg on 19/05/2016.
 */
public class DeviceWifiConfig extends LoadableConfig {

    private String ssid;
    private String psk;

    public DeviceWifiConfig(String ssid, String psk) {
        this.ssid = ssid;
        this.psk = psk;
    }

    public String getSSID() {
        return ssid;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public String getPSK() {
        return psk;
    }

    public void setPSK(String psk) {
        this.psk = psk;
    }
}
