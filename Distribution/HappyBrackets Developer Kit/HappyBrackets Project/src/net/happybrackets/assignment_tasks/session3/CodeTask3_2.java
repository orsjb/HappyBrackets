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

package net.happybrackets.assignment_tasks.session3;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Continue on from your code in CodeTask3_1.
 *
 * 4) Replace the fixed playback rate of the SamplePlayer with an Envelope (use setPitch()), and randomly add a downward
 * pitch bend to 1 in 5 of the notes.
 *
 * 5) Switch the SamplePlayer to a GranularSamplePlayer.
 * Use the method 'setRate()' to set the GranularSamplePlayer to play back at half speed.
 * Note that with SamplePlayer 'getRate()' and 'getPitch()' are the same method, whereas for GranularSamplePlayer they
 * return different things.
 *
 * Using the methods 'getGrainSizeUGen().setValue()', 'getGrainIntervalUGen().setValue()',
 * 'getRandomnessUGen().setValue()', find suitable granulation settings that make the guitar sound as natural as
 * possible.
 */
public class CodeTask3_2 extends Application implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        launch(args);
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
        //clock
        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                if (c.getCount() % 32 == 0) {
                    ac.out.addInput(new SamplePlayer(ac, SampleManager.sample("data/audio/Nylon_Guitar/Clean_A_harm.wav")));
                }
            }
        });
        //********** do your work here ONLY **********
    }
}
