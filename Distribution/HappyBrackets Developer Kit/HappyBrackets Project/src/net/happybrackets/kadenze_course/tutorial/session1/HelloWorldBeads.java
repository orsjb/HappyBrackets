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

package net.happybrackets.kadenze_course.tutorial.session1;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * This is the "Hello World" of Beads, the Java realtime audio library used in HappyBrackets. First let's run this
 * code.
 *
 * From IntelliJ, control-click or right-click on this file (i.e., click right here). About 1/3 the way down you will
 * see the option to "Run HelloWorldBeads.main()", with a green triangle next to it. Select this option. You should hear
 * some white noise playing back through your speakers. If you do not hear anything, check your sound is on, and look
 * below in the "Run" window to see if there are any Java exceptions. These are blocks of code that alert you to an
 * error.
 *
 * Congratulations. You are now running your first Beads program!
 *
 * To stop the program, you can click the red square down below in the Run window. Note if you can't see the "Run"
 * window you can go to the "View" menu and look under "Tool Windows". You'll notice in the top right of this window now
 * that you can re-run HelloWorldBeads by pressing the green arrow.
 */
public class HelloWorldBeads extends Application {

    public static void main(String[] args) {
        /*
        Note for those used to Java, you may not be familiar with a JavaFX application.
        JavaFX applications look a little different to regular Java programs.
        This 'launch()' function does some Application setup under the hood. Once that's done, the 'start()' function
        below gets called. That is where you should do your initialisation in a JavaFX program.
         */
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //set up the AudioContext and start it
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Noise generator and a Gain controller
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.2f);
        //plug it all together
        g.addInput(n);
        ac.out.addInput(g);
        //say something to the console output
        System.out.println("Hello World! We are running Beads.");
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }
}
