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

package net.happybrackets.kadenze_course.assignment_tasks.session1;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Random;

/**
 * This task involves for-loops. It is assumed you have enough coding experience to know how to make one. If you're not
 * sure, take a look at the supporting material to see where you can learn about basic principles of Java programming.
 *
 * Remember to do all of your work in the task() function only. This will be tested.
 *
 * Using the example code, add an integer variable N that specifies the number of oscillators, and then write a for-loop
 * that creates N oscillators.
 *
 * Be sure to also regulate the volume to account for the fact that many oscillators will be louder than 1!
 *
 * Try this with 10 or 100 oscillators.
 *
 * The number 50 below dictates the random spread of the oscillator detune. See what happens when you vary this.
 *
 * Your final submission for grading should have the following properties:
 * - 50 SINE oscillators all playing directly into ac.out.
 * - Each oscillator has a random frequency uniformly distributed between 1000hz and 1100hz.
 * - The gain value of ac.out scaled appropriately so that the overall gain of the summed oscillators is 0.1.
 */
public class CodeTask1_3 extends Application implements BeadsChecker.BeadsCheckable {

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
        //create a random number generator
        Random rng = new Random();
        //create a WavePlayer with a slight random detune on the pitch
        WavePlayer wp = new WavePlayer(ac, 500 + rng.nextFloat() * 50, Buffer.SINE);
        ac.out.addInput(wp);
        //ac.out is actually a Gain object, so we can simply turn down the level here...
        //no need for another Gain object.
        ac.out.setGain(0.1f);
        //********** do your work here ONLY **********
    }
}
