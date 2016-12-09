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

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

/**
 * In this example, we load some of the audio that is preloaded on the Pi, the same set of samples found in previous tutorials.
 *
 * Note that the first time you send this to the Pi it will load the samples, resulting in considerable overhead, with possible glitch and delay. However, when you send the code again, the SampleManager knows not to attempt to reload the audio, which is already stored in memory.
 *
 */
public class Example5_2 implements HBAction {

    @Override
    public void action(HB hb) {

        //load a set of sounds
        SampleManager.group("Guitar", "data/audio/Nylon_Guitar");

        //play a new random sound from the group
        Sample s = SampleManager.randomFromGroup("Guitar");
        hb.sound(new SamplePlayer(hb.ac, s));

    }

}
