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

package net.happybrackets.core;

import net.happybrackets.device.config.DeviceWifiConfig;
import net.happybrackets.core.config.LoadableConfig;
import net.happybrackets.core.config.SavableConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Samg on 19/05/2016.
 */
public class SavableConfigTest {
    DeviceWifiConfig testConfig; // a simple config to test with

    @Before
    public void setUp() throws Exception {
        testConfig = new DeviceWifiConfig("testnetwork", "TestPassword1");
    }

    @Test
    public void save() throws Exception {
        String configPath = "build/tmp/test/test-config.json";

        //test save
        assertTrue( SavableConfig.save(configPath, testConfig) );

        //test load
        DeviceWifiConfig testConfigIn = new DeviceWifiConfig("default", "default");
        testConfigIn = LoadableConfig.load(configPath, testConfigIn);

        //test equality
        assertTrue( testConfig.getPSK().equals(testConfigIn.getPSK()) );
        assertTrue( testConfig.getSSID().equals(testConfigIn.getSSID()) );
    }

}