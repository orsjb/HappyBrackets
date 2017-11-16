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

package net.happybrackets.kadenze_course.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * This example shows the use of an Envelope object to control the frequency of a WavePlayer.
 * Try changing the arguments to the commands e.addSegment(), and add more segments to see what happens.
 * Look at the documentation for Envelope and WavePlayer (use the F1 key, or look at beadsproject.net/doc).
 */
public class Example2_1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //add the waveplayer, with frequency value controlled by an Envelope
        Envelope e = new Envelope(ac, 500);
        e.addSegment(1000, 2000);               //segment arguments are (destination, time_ms)
        e.addSegment(500, 200);                 //queue multiple segments
        WavePlayer wp = new WavePlayer(ac, e, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        //connect together
        g.addInput(wp);
        ac.out.addInput(g);
        //visualiser
        WaveformVisualiser.open(ac);
    }
}
