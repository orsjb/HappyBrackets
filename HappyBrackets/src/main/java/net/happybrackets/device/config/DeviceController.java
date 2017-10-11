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

import java.net.InetSocketAddress;

/**
 * A small class to keep our host controller's hostname, address and port together
 */
public class DeviceController {
    private String hostname;

    private InetSocketAddress socketAddress;
    int hash;

    private long lastTimeSeen;

    /**
     * Set the last time controller seen as now
     */
    public void controllerSeen(){
        lastTimeSeen = System.currentTimeMillis();
    }

    /**
     * Create a controller based on ip address and port it wants to receive messages on
     * @param hostname hostname provided by controller
     * @param address ip address that we send messages to this controller
     * @param port the port we send messages on
     */
    public DeviceController(String hostname, String address, int port) {
        this.hostname = hostname;

        socketAddress = new InetSocketAddress(address, port);
        String hash_build = address + port;
        hash = hash_build.hashCode();
    }

    /**
     * Has code based on i[ address and port
     * @return the hashCode
     */
    public int hashCode() {
        return hash;
    }

    /**
     * The socket address we use to send messages t this controller
     * @return
     */
    public InetSocketAddress getAddress() {
        return socketAddress;
    }


    /**
     * The hostname of the controller
     * @return hostname
     */
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}

