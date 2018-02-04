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
public class EnvelopeTrigger implements HBAction {

    @Override
    public void action(HB hb) {
        WavePlayer wp = new WavePlayer(hb.ac, 1000, Buffer.SINE);
        Envelope e = new Envelope(hb.ac, 0.1f);

        Gain g = new Gain(hb.ac, 1, e);

        final float segment_time = 4000;

        e.addSegment(0.1f, segment_time, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                // Note that the scope of wp is new
                WavePlayer wp = new WavePlayer(hb.ac, 2000, Buffer.SINE);
                g.addInput(wp);
            }
        });

        e.addSegment(0.1f, 2000, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                WavePlayer wp = new WavePlayer(hb.ac, 4000, Buffer.SINE);
                g.addInput(wp);            }
        });

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
