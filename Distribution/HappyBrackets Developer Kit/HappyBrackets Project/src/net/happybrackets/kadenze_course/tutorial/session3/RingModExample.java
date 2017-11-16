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

package net.happybrackets.kadenze_course.tutorial.session3;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * This is a slightly more complex example including playing back random samples from a set, panning each sound,
 * possibly reversing it at random, and ring modulating each sound with a common ringmod oscillator.
 * Note the output to the commandline, which reports the number of UGens connected to ac.out. This number changes
 * according to sounds being added and later auto-removed.
 *
 * Note the sp.setKillListener() function which flags the signal to be destroyed once the sample has played.
 *
 * Note also that the panner is a hand-coded UGen. This is not necessary, but illustrates how you can code UGens on the
 * fly.
 *
 * Lastly, this example shows how to record data to a file. Note the DelayTrigger which triggers the end of the
 * recording after 20 seconds.
 */
public class RingModExample {

    public static void main(String[] args) throws IOException {
        //random number generator
        Random rng = new Random();
        //set up audio
        AudioContext ac = new AudioContext();
        ac.start();
        //load samples
        SampleManager.group("guitar", "data/audio/Nylon_Guitar");
        //set up ring modulator
        Glide modFreq = new Glide(ac, 500, 100);
        WavePlayer modulator = new WavePlayer(ac, modFreq, Buffer.SINE);
        Clock clock = new Clock(ac, 500);
        ac.out.addDependent(clock);
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (clock.getCount() % 4 == 0) {
                    //load sample into sample player
                    SamplePlayer sp = new SamplePlayer(ac, SampleManager.randomFromGroup("guitar"));
                    //optional reverse
                    if (rng.nextFloat() < 0.3f) {
                        sp.getRateUGen().setValue(-1);
                        sp.setPosition(sp.getSample().getLength());
                    }
                    //mix in the ring mod
                    Mult m = new Mult(ac, sp, modulator);
                    float pan = rng.nextFloat();
                    UGen panner = new UGen(ac, 1, 2) {
                        @Override
                        public void calculateBuffer() {
                            for (int i = 0; i < bufferSize; i++) {
                                float val = bufIn[0][i] * 0.5f;
                                bufOut[0][i] = val * pan;
                                bufOut[1][i] = val * (1 - pan);
                            }
                        }
                    };
                    panner.addInput(m);
                    //always remember to kill
                    sp.setKillListener(new KillTrigger(panner));
                    ac.out.addInput(panner);
                    //play with the ring mod value
                    if (rng.nextFloat() < 0.1f) {
                        modFreq.setValue(rng.nextFloat() * 500 + 100);
                    }
                    System.out.println(ac.out.getNumberOfConnectedUGens(0));
                }
            }
        });
        //recording code
        RecordToFile recorder = new RecordToFile(ac, 2, new File("test.wav"));
        ac.out.addDependent(recorder);
        recorder.addInput(ac.out);
        DelayTrigger dt = new DelayTrigger(ac, 20000, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                recorder.kill();
                System.out.println("RECORDING HAS ENDED");
            }
        });
        ac.out.addDependent(dt);
    }
}
