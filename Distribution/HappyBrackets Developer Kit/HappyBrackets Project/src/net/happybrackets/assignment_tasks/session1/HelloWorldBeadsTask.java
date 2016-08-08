package net.happybrackets.assignment_tasks.session1;

import de.sciss.net.OSCMessage;
import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * Created by ollie on 10/05/2016. 
 *
 * This is the "Hello World" of Beads, the Java realtime audio library used in HappyBrackets. First let's run this code.
 *
 * From IntelliJ, control-click or right-click on this file (i.e., click right here). About 1/3 the way down you will see the option to "Run HelloWorldBeadsTask.main()", with a green triangle next to it. Select this option. You should hear some white noise playing back through your speakers. If you do not hear anything, check your sound is on, and look below in the "Run" window to see if there are any Java exceptions. These are blocks of code that alert you to an error.
 *
 * Congratulations. You are now running your first Beads program!
 *
 * To stop the program, you can click the red square down below in the Run window. Note if you can't see the "Run" window you can go to the "View" menu and look under "Tool Windows". You'll notice in the top right of this window now that you can re-run HelloWorldBeadsTask by pressing the green arrow.
 *
 *
 */
public class HelloWorldBeadsTask extends Application implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        /*
        Note for those used to Java, you may not be familiar with a JavaFX application.
        JavaFX applications look a little different to regular Java programs.
        This 'launch()' function does some Application setup under the hood. Once that's done, the 'start()' function below gets called. That is where you should do your initialisation in a JavaFX program.
         */
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //set up the AudioContext and start it
        AudioContext ac = new AudioContext();
        ac.start();
        task(ac);
        //say something to the console output
        System.out.println("Hello World! We are running Beads.");
        //finally, this creates a window to visualise the waveform
        WaveformVisualiser.open(ac);
    }

    public String task(AudioContext ac, Object... args) {
        //create a Noise generator and a Gain controller

        //DO YOUR WORK HERE
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.2f);
        //plug it all together
        g.addInput(n);
        ac.out.addInput(g);
        StringBuffer sb = new StringBuffer("");

        Clock c = new Clock(ac, 500);
        ac.out.addDependent(c);
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                ac.out.addInput(new Noise(ac));
                sb.append("xx ");
            }
        });

        return sb.toString();
    }
}
