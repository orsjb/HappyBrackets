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

package net.happybrackets.compositions;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Created by ollie on 22/06/2016.
 */
public class LiveCoder implements HBAction {
    @Override
    public void action(HB hb) {
        hb.masterGainEnv.setValue(0.2f);
        hb.clock.getIntervalUGen().setValue(2000);
        hb.clock.clearMessageListeners();
        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(hb.clock.getCount() % 8 == 0) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 2000 + 100, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * 0.03f + 0.1f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 5000, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 6 == 5) {
                    float freq = Pitch.forceFrequencyToScale(hb.rng.nextFloat() * 1000 + 100, Pitch.dorian);
                    WavePlayer wp = new WavePlayer(hb.ac, freq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(hb.rng.nextFloat() * 0.02f + 0.04f, 1000);
                    e.addSegment(0, 100, new KillTrigger(g));
                    g.addInput(wp);
                    hb.sound(g);
                }
                if(hb.clock.getCount() % 4 == 0) {
                    Noise n = new Noise(hb.ac);
                    Envelope e = new Envelope(hb.ac, hb.rng.nextFloat() * 0.01f + 0.02f);
                    Gain g = new Gain(hb.ac, 1, e);
                    e.addSegment(0, 5, new KillTrigger(g));
                    g.addInput(n);
                    hb.sound(g);
                }
            }
        });
    }
}
