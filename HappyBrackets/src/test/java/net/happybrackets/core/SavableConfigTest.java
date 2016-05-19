package net.happybrackets.core;

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