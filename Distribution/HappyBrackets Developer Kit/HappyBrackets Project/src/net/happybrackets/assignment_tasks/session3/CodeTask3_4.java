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
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Random;

/**
 * The following code creates noise, just like the Noise object, except it does so using a custom Function class.
 *
 * (Note this is much less efficient than using the Noise object because it actually uses a random number generator,
 * whereas the Noise class plays back a readymade buffer of generated noise. Also, the Function object is a bit less
 * efficient because it makes an individual method call each time it calculates a new sample).
 *
 * Change the code so that the Function object generates a saw tooth wave, oscillating between -0.1 and +0.1, with a
 * frequency defined by the float variable 'freq' that is given below. To do this you will need to know the sample rate,
 * using ac.getSampleRate().
 */
public class CodeTask3_4 extends Application implements BeadsChecker.BeadsCheckable {

    int bufPos = 0;

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
        //parameters that are used in this task
        float testFreq = 400f;
        //do your work here, using the function below
        task(ac, buf, new Object[]{testFreq});
        //poll StringBuffer for new console output
        new Thread(() -> {
            while(true) {
                String newText = buf.substring(bufPos);
                bufPos += newText.length();
                System.out.print(newText);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }).start();
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    @Override
    public void task(AudioContext ac, StringBuffer stringBuffer, Object... objects) {
        //********** do your work here ONLY **********
        float freq = (Float) objects[0]; //<-- this is the freq value you should use
        Random rng = new Random();
        //a function object
        Function f = new Function(ac.out) {
            @Override
            public float calculate() {
                float nextSampleVal = rng.nextFloat() * 0.2f - 0.1f;
                return nextSampleVal;
            }
        };
        ac.out.addInput(f);
        //********** do your work here ONLY **********
    }

}
