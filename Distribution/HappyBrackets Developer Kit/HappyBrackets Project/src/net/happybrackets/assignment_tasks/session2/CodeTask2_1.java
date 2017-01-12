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
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Let's look at Envelopes!
 *
 * Run the following code. Then uncomment the line at the very end (shortcut for comment/uncomment = command-/) and run
 * the code again. You will hear the frequency sweeping upwards.
 *
 * Duplicate that last line so that there are two copies of it, one after the other. Then change the 1000 in the second
 * instance to 100. Run the code again. Notice that the segments run one after the other.
 *
 * Now complete the following tasks:
 *
 * 1) Make the frequency rise from 500hz to 1000hz over 1 second, stay there for 1 second, then rise again to 2000hz
 * over two seconds.
 *
 * 2) Create a new Envelope that will be used to control the Gain object. This should be called 'gainEnv' and should be
 * initialised to 0. In the constructor for Gain, you can replace the value 0.1f by gainEnv.
 *
 * 3) Use your new gainEnv object to create an attack-decay-sustain-release envelope. Make your sound's gain increase
 * from 0 to 0.5f over 50ms, then drop to 0.1f over 50ms, then remain at 0.1f for 1000ms, then decay to 0 over 5
 * seconds.
 *
 * 4) Add the additional argument 'new KillTrigger(g)' to the last element of your ADSR gainEnv. You won't notice any
 * difference because you already faded the volume to zero, but this last line will remove all of the audio processing
 * elements from the signal chain.
 *
 * In summary, your sound should rise in frequency from 500-1000hz over 1 second, stay on 1000hz for 1 second, then rise
 * to 2000hz over two seconds. Its gain should rise from zero to 0.5 over 50ms, then drop to 0.1 over 50ms, then remain
 * at 0.1 for 1 second, then decay to zero over 5 seconds. After the completion of the gain envelope, the sound should
 * destroy itself, leaving the signal chain in its original empty state.
 */
public class CodeTask2_1 extends Application implements BeadsChecker.BeadsCheckable {

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
        //here is the Envelope, initialised to 500.
        Envelope freqEnv = new Envelope(ac, 500);
        //notice that the second argument to WavePlayer is no longer a number, but is the Envelope object.
        WavePlayer wp = new WavePlayer(ac, freqEnv, Buffer.SINE);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(wp);
        ac.out.addInput(g);
        g.setGain(0.1f);
        //once you've run the above code once, uncomment the following line and run it again...
//        freqEnv.addSegment(1000, 4000);
        //********** do your work here ONLY **********
    }
}
