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
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Function;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Random;

/**
 * In this task you will make a sample-and-hold LFO that modulates a filter.
 * The speed and output range of the LFO will be determined by lfoFreq, freqLow and freqHigh.
 * There are two obvious ways to do this. Either create a Clock which is then used to control the Glide, or use a
 * Function that replaces the Glide.
 */
public class CodeTask3_5 extends Application implements BeadsChecker.BeadsCheckable {

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
        float lfoFreq = 0.1f;
        float freqLow = 500f;
        float freqHigh = 1000f;
        //do your work here, using the function below
        task(ac, buf, new Object[]{lfoFreq, freqLow, freqHigh});
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
        float lfoFreq = (Float) objects[0];
        float freqLow = (Float) objects[1];
        float freqHigh = (Float) objects[2];
        Noise n = new Noise(ac);
        BiquadFilter bf = new BiquadFilter(ac, 1, BiquadFilter.Type.LP);
        Glide filtFreq = new Glide(ac, freqLow);   //either replace the Glide object with a Function object, or use a Clock to control the Glide.
        bf.setFrequency(filtFreq);
        bf.addInput(n);
        ac.out.addInput(bf);
        //********** do your work here ONLY **********
    }

}
