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

    // change this variable to 500 and you will hear popping
    final float clockDuration = 2000;


    float currentFreq = 1000;

    @Override
    public void action(HB hb) {

        WavePlayer wp = new WavePlayer(hb.ac, currentFreq, Buffer.SINE);
        Envelope e = new Envelope(hb.ac, 0);
        Gain g = new Gain(hb.ac, 1, e);

        e.addSegment(0.1f, 100);

        g.addInput(wp);

        hb.sound(g);


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
                    currentFreq *= 1.2;

                    if (currentFreq > 15000) {
                        currentFreq = 100;
                    }

                    WavePlayer wp = new WavePlayer(hb.ac, currentFreq, Buffer.SINE);
                    Envelope e = new Envelope(hb.ac, 0);
                    Gain g = new Gain(hb.ac, 1, e);

                    e.addSegment(0.1f, 100);
                    e.addSegment(0.1f, 500);
                    // this will kill
                    e.addSegment(0, 1000, new KillTrigger(g));

                    g.addInput(wp);

                    hb.sound(g);

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
