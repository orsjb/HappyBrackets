package net.happybrackets.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Samg on 19/10/2016.
 */
public class DeviceTest {

    @Test
    public void removeLinkLocalSuffixTests() {
        String ipv6withSuffix       = "fe80:0:0:0:a00:27ff:fe8c:991d%eth0";
        String ipv6withoutSuffix    = "fe80:0:0:0:a00:27ff:fe8c:991d";
        String ipv4                 = "192.168.0.1";
        String hostName             = "happybrackets.net";

        System.out.println("------------------- Testing Device.removeLinkLocalSuffix() --------------------------------");
        System.out.println("Testing IPv4 -> IPv4");
        assertEquals(ipv4,              Device.removeLinkLocalSuffix(ipv4));

        System.out.println("Testing host name -> host name");
        assertEquals(hostName,          Device.removeLinkLocalSuffix(hostName));

        System.out.println("Testing IPv6 no suffix -> IPv6 no suffix");
        assertEquals(ipv6withoutSuffix, Device.removeLinkLocalSuffix(ipv6withoutSuffix));

        System.out.println("Testing IPv6 with suffix -> IPv6 no suffix");
        assertEquals(ipv6withoutSuffix, Device.removeLinkLocalSuffix(ipv6withSuffix));

        System.out.println("------------------- Device.removeLinkLocalSuffix() testing complete -----------------------");
    }
}
