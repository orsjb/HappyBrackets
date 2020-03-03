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

package tests.controller.misc_tests;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Created by ollie on 22/06/2016.
 */
public class GetHostNameTest {

    public static void main(String[] args) throws Exception {
        System.out.println("Localhost name: " + InetAddress.getLocalHost().getHostName());
        System.out.println("Ethernet name: " + NetworkInterface.getByName("en0").getInetAddresses().nextElement().getHostName());
        System.out.println(new MulticastSocket().getNetworkInterface() + " " + new MulticastSocket().getInterface());


    }
}
