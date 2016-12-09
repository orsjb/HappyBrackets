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

package net.happybrackets.device.misc_tests;

import net.happybrackets.core.Synchronizer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SynchronizerTest {

    static Synchronizer sync;
    static  float stability;
    static long timeNow, timeAfter, timeDiffMillis, timeDiffNanos;

    public static void main(String[] args) throws IOException {

        sync = Synchronizer.getInstance();
        MulticastSocket socket = new MulticastSocket(4446);
        InetAddress group = InetAddress.getByName("192.168.1.102");
        socket.joinGroup(group);


//      System.out.println("Multicast: " + LoadableConfig.getInstance().getMulticastAddr());

        while(true) {

            stability = sync.getStability();
//            This method is depreciated! sg
//            sync.broadcast("Stable time: " + sync.stableTimeNow());
//            sync.broadcast("Corrected time: " + sync.correctedTimeNow());

            timeNow = System.nanoTime();
            System.out.print("Time: " + sync.stableTimeNow());
            System.out.print(" Stab: " + sync.getStability());
            System.out.print(" Corr: " + sync.correctedTimeNow());
            timeAfter = System.nanoTime();

            System.out.println(timeAfter - timeNow);
            timeDiffMillis = (long) Math.floor( (timeAfter - timeNow) / 1000000);
            timeDiffNanos =  (long) (((timeAfter - timeNow) / 1000000) - (timeDiffMillis * 1000000));

            System.out.println(" Diff: " + timeDiffMillis + " Nnos: " + timeDiffNanos);

            try {
                Thread.sleep(1000 - timeDiffMillis - 1, (int) timeDiffNanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public SynchronizerTest(){

    }
}