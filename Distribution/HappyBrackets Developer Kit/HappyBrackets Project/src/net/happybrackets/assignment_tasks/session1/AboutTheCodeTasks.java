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
 * The following code gives the outline for these tasks. You will put your solutions to the task in the task() function. You can run this to see what the resulting code sounds like. You can also submit these code task files to the Kadenze server and it will autorun the task command and autograde the output.
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
        //do your work here
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.1f);
        g.addInput(n);
        ac.out.addInput(g);
        //write output like this
        buf.append("Hello World!\n");   //'\n' means new line
    }
}
