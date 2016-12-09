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

package net.happybrackets.tutorial.session5;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * This example can be played twice on the same device or on two devices to show that multiple patterns can play at once.
 */
public class Example5_4 implements HBAction {

    @Override
    public void action(HB hb) {

//        hb.resetLeaveSounding();

        hb.clock.getIntervalUGen().setValue(500);

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 16 == 0) {
                    float freq = 500 + hb.rng.nextFloat() * 500;
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 400, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                    hb.setStatus("Playing freq " + freq);
                }
            }
        });


    }

}
