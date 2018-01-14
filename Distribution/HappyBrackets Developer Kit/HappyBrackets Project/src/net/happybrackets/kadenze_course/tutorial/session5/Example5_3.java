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

package net.happybrackets.kadenze_course.tutorial.session5;

import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * This example plays random sounds from the Nylon_Guitar group on the Pi. As before, you can reset and resend but the
 * sounds will remain loaded.
 */
public class Example5_3 implements HBAction {

    @Override
    public void action(HB hb) {

        hb.reset();

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        hb.pattern(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (hb.clock.getCount() % 32 == 0) {
                    //play a new random sound
                    Sample s = SampleManager.randomFromGroup("Guitar");
                    hb.ac.out.addInput(new SamplePlayer(hb.ac, s));
                }
            }
        });

    }

}
