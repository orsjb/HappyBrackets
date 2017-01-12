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

package net.happybrackets.tutorial.session6;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * Example of playing a single sound on multiple devices at the same time.
 * An alternative to this is to synch the clocks on the devices (using /device/sync) and then create events on the
 * clock.
 */
public class SyncExample implements HBAction {

    @Override
    public void action(HB hb) {

        hb.clock.getIntervalUGen().setValue(1000);

        hb.doAtTime(new Runnable() {
            @Override
            public void run() {
                SamplePlayer sp = new SamplePlayer(hb.ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav"));
                Gain g = new Gain(hb.ac, 1, 0.3f);
                sp.setKillListener(new KillTrigger(g));
                g.addInput(sp);
                hb.sound(g);
            }
        }, hb.getSynchTime() + 5000);

    }

}
