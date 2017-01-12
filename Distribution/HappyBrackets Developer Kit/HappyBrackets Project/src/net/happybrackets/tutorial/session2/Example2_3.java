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

package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

import java.util.Random;

/**
 * In this example, we do some more complex melodic manipulation and create multiple patterns running off the same
 * clock.
 * We use the Pitch class to create notes from a major scale.
 */
public class Example2_3 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //random number generator
        Random random = new Random();
        //the clock
        Clock clock = new Clock(ac, 500);
        ac.out.addDependent(clock);
        int basePitch = 50;
        clock.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (clock.getCount() % 16 == 0) {
                    //add the waveplayer
                    int pitch = basePitch + 12 + Pitch.major[random.nextInt(7)];
                    float freq = Pitch.mtof(pitch);
                    WavePlayer wp = new WavePlayer(ac, freq, Buffer.SINE);
                    //add the gain
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0, 200, new KillTrigger(g));
                    //connect together
                    g.addInput(wp);
                    ac.out.addInput(g);
                }
                if (clock.getCount() % 6 == 0) {
                    //add the waveplayer
                    int pitch = basePitch + Pitch.major[random.nextInt(7)];
                    float freq = Pitch.mtof(pitch);
                    WavePlayer wp = new WavePlayer(ac, freq, Buffer.SQUARE);
                    //add the gain
                    Envelope e = new Envelope(ac, 0.1f);
                    Gain g = new Gain(ac, 1, e);
                    e.addSegment(0, 200, new KillTrigger(g));
                    //connect together
                    g.addInput(wp);
                    ac.out.addInput(g);
                }
            }
        });
        //visualiser
        WaveformVisualiser.open(ac);
    }

}
