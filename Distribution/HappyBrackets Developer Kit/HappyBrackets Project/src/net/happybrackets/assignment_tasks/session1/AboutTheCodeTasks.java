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

package net.happybrackets.assignment_tasks.session1;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Throughout this course you will be performing code assignment tasks that will be autograded on the Kadenze server.
 *
 * The following code gives the outline for these tasks. You will put your solutions to the task in the task() function.
 * You can run this to see what the resulting code sounds like. You can also submit these code task files to the Kadenze
 * server and it will autorun the task command and autograde the output.
 *
 * Some things to note:
 * - You only need to enter code into the task() function. You can also add your own functions. Anything you add to the
 * start() function won't have any effect on the autograder.
 * - the task() function gives you an AudioContext, already setup. It also gives you an object called a StringBuffer. If
 * you need to print things to the output for the purpose of the task, then use buf.append("your text");. Don't use
 * System.out.println();. Use '\n' to print a new line (as in the example below.
 * - the task() function also gives you an array of Objects which will be task specific. The code tasks will show you
 * how to extract arguments from this array where necessary.
 */

public class AboutTheCodeTasks extends Application implements BeadsChecker.BeadsCheckable {

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
    public void task(AudioContext ac, StringBuffer buf, Object... args) {
        //********** do your work here ONLY **********
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(n);
        ac.out.addInput(g);
        //write output like this
        buf.append("Hello World!\n");   //'\n' means new line
        //********** do your work here ONLY **********
    }
}
