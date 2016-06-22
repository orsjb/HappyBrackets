package net.happybrackets.controller.misc_tests;

import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Created by ollie on 22/06/2016.
 */
public class GetHostNameTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Localhost name: " + InetAddress.getLocalHost().getHostName());
        System.out.println("Ethernet name" + NetworkInterface.getByName("wlan0").getInetAddresses().nextElement().getHostName());
    }
}
