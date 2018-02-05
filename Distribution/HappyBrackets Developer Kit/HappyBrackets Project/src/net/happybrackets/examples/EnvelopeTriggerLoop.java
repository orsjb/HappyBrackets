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

package net.happybrackets.examples;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 * Ensure you have a device set up and connected. It should be visible in the devices list in the HappyBrackets plugin.
 * From the Send Composition dropdown menu in the plugin select HappyBracketsHelloWorld and click All.
 */
public class EnvelopeTriggerLoop implements HBAction {


    @Override
    public void action(HB hb) {
        float current_freq = 100;

        WavePlayer wp = new WavePlayer(hb.ac, current_freq, Buffer.SINE);
        Envelope e = new Envelope(hb.ac, 0);

        Gain g = new Gain(hb.ac, 1, e);

        while (current_freq < 15000) {
            current_freq *= 2;

            float finalCurrent_freq = current_freq;
            e.addSegment(0.1f, 2000, new Bead() {
                @Override
                protected void messageReceived(Bead bead) {
                    // Note that the scope of all of these are new
                    Envelope e = new Envelope(hb.ac, 0);

                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0.1f, 2000);


                    WavePlayer wp = new WavePlayer(hb.ac, finalCurrent_freq, Buffer.SINE);
                    g.addInput(wp);
                    hb.ac.out.addInput(g);
                }
            });
        }

        g.addInput(wp);
        hb.ac.out.addInput(g);
    }


    /**
     * This function is used when running sketch in IntelliJ for debugging or testing
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
