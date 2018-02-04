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
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

/**
 * To run this code, make sure you have installed the HappyBrackets IntelliJ plugin.
 */
public class ClockTrigger implements HBAction {


    final float clockDuration = 200;


    final int centrePitch = 60;
    int currentStartPitch = 0;
    int nextScaleNote = 0;

    float currentFreq = 1000;

    @Override
    public void action(HB hb) {


        // Create a clock with beat interval of clockDuration ms
        Clock clock = new Clock(hb.ac, clockDuration);
        // connect the clock to HB
        hb.ac.out.addDependent(clock);

        // let us handle triggers
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {

                // see if we are at the start of a beat
                if (clock.getCount() % clock.getTicksPerBeat() == 0) {

                    // see if we are at the start of a pattern
                    if (nextScaleNote % Pitch.major.length == 0){
                        // move around circle of fourths
                        currentStartPitch = (currentStartPitch + 5) % 12;
                    }


                    int pitch = centrePitch + currentStartPitch + Pitch.major[nextScaleNote % Pitch.major.length];

                    // convert to a frequency
                    currentFreq = Pitch.mtof(pitch);


                    nextScaleNote++;

                    WavePlayer wp = new WavePlayer(hb.ac, currentFreq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);

                    e.addSegment(0.1f, 100);
                    e.addSegment(0.1f, 500);
                    // this will kill
                    e.addSegment(0, 10, new KillTrigger(g));

                    g.addInput(wp);

                    hb.ac.out.addInput(g);

                }
            }
        });


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
