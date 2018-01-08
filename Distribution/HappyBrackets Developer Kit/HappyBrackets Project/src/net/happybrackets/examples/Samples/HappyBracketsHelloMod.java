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

package net.happybrackets.examples.Samples;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 * Ensure you have a device set up and connected. It should be visible in the devices list in the HappyBrackets plugin.
 * From the Send Composition dropdown menu in the plugin select HappyBracketsHelloWorld and click All.
 */
public class HappyBracketsHelloMod implements HBAction {

    @Override
    public void action(HB hb) {
        Envelope freq_env = new Envelope(hb.ac, 1000);
        WavePlayer wp = new WavePlayer(hb.ac, freq_env, Buffer.TRIANGLE);
        freq_env.addSegment(1500, 2000);
        freq_env.addSegment(1000, 2000);
        Envelope e = new Envelope(hb.ac, 0.1f);
        Gain g = new Gain(hb.ac, 1, e);
        e.addSegment(0, 5000, new KillTrigger(g));

        g.addInput(wp);
        hb.sound(g);
    }

}
