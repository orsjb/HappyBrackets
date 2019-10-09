package net.happybrackets.device.network;

import java.net.InetAddress;

/**
 * Interface for sending Device Connected Events
 * This way we can be notified when a new device has entered the network
 */
public interface DeviceConnectedEventListener {
    void deviceConnected (String device_name, InetAddress device_address);
}
