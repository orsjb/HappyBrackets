package net.happybrackets.device;

import net.happybrackets.core.Device;
import org.junit.Test;

import java.net.InetAddress;

/**
 * Test adding of device names
 */
public class TestDeviceName {

    @Test
    public void testDevice(){
        try {
            HB.runDebug(null);

            assert (!HB.HBInstance.addDeviceAddress(Device.getDeviceName(), InetAddress.getLoopbackAddress()));

            InetAddress broadcast = InetAddress.getByName("255.255.255.255");

            System.out.println("Name map Loopback is working. Now test name change");
            assert (HB.HBInstance.addDeviceAddress(Device.getDeviceName(), broadcast));

            System.out.println("Check that name updated");
            assert (!HB.HBInstance.addDeviceAddress(Device.getDeviceName(), broadcast));

        } catch (Exception e) {
            assert (true); // we are going to get this if we cant get our broadcast. This will happen if wifi off
        }

    }

}
