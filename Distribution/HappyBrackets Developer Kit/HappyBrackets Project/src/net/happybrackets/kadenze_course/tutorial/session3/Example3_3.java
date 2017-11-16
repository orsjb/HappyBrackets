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

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * In this example we demonstrate a simple use of ring modulation, affecting a sample.
 */
public class Example3_3 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //load sample
        Sample s = SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        SamplePlayer sp = new SamplePlayer(ac, s);
        //set loop parameters
        sp.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        Envelope loopEndEnv = new Envelope(ac, 500);
        sp.setLoopEnd(loopEndEnv);
        loopEndEnv.addSegment(1, 5000);
        //Envelope controlling the frequency of the ringmod modulation signal
        Envelope modFreq = new Envelope(ac, 500);
        modFreq.addSegment(1000, 3000);
        //the ringmod oscillator
        WavePlayer ringmodulator = new WavePlayer(ac, modFreq, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(ringmodulator);
        //one way to get the ringmod effect is multiply the source signal by our oscillator's signal
        Mult m = new Mult(ac, sp, g);
        ac.out.addInput(m);
        //visualiser
        WaveformVisualiser.open(ac);

    }
}
