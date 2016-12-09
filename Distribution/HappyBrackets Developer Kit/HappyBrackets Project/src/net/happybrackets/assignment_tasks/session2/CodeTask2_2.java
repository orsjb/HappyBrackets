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

package net.happybrackets.assignment_tasks.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Here is some basic code that shows how to set up a Clock. If you run it you won't hear anything, but you will see the clock ticks outputting to the console.
 *
 * Complete the following tasks:
 *
 * 1) Add a WavePlayer object that plays continually.
 * 2) Create a Glide object that controls the frequency of the WavePlayer, with a glide time of 500ms.
 * 3) Use the Clock to update the Glide every 4 beats (note beats are not the same as ticks). Each time choose a new frequency, using pitches from a pentatonic scale (use the Pitch class with Pitch.pentatonic) in the octave starting from middle-C (MIDI note 60). Your sequence should loop through this pentatonic scale sequentially. Use the Pitch class to calculate frequencies from the MIDI note numbers.
 * 4) Add a new one-hit bass note that plays every 8 beats. The note should be a square wave, with a pitch C, whichever octave suits you. The note should play through an ADSR envelope with a total length of 1 second, and be removed once played, using a KillTrigger.
 *
 */
public class CodeTask2_2 extends Application implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //the AudioContext
        AudioContext ac = new AudioContext();
        ac.start();
        //a StringBuffer used to record anything you want to print out
        StringBuffer buf = new StringBuffer();
        //do your work here, using the function below
        task(ac, buf);
        //say something to the console output
        System.out.println(buf.toString());
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    @Override
    public void task(AudioContext ac, StringBuffer stringBuffer, Object... objects) {
        //********** do your work here ONLY **********
        //create a Clock
        Clock c = new Clock(ac, 500);
        //important! Make sure your clock is running by adding it as a 'dependent' to some other UGen.
        ac.out.addDependent(c);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if(c.isBeat()) {
                    stringBuffer.append("-----BEAT------\n");
                }
                stringBuffer.append("tick " + c.getCount() + " (beat " + c.getBeatCount() + ")\n");
            }
        });
        //********** do your work here ONLY **********
    }
}
