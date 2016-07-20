package net.happybrackets.tutorial.session1;

import de.sciss.net.OSCMessage;
import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * Created by ollie on 10/05/2016. 
 *
 * This is the "Hello World" of Beads, the Java realtime audio library used in HappyBrackets. First let's run this code.
 *
 * From IntelliJ, control-click or right-click on this file (i.e., click right here). About 1/3 the way down you will see the option to "Run CodeTask1_1.main()", with a green triangle next to it. Select this option. You should hear some white noise playing back through your speakers. If you do not hear anything, check your sound is on, and look below in the "Run" window to see if there are any Java exceptions. These are blocks of code that alert you to an error.
 *
 * Congratulations. You are now running your first Beads program!
 *
 * To stop the program, you can click the red square down below in the Run window. Note if you can't see the "Run" window you can go to the "View" menu and look under "Tool Windows". You'll notice in the top right of this window now that you can re-run CodeTask1_1 by pressing the green arrow.
 *
 * Let's play with some of the code. Since this is our first time coding together, this first task is mainly dedicated to seeing how well you understand Java code.
 *
 * *** Step 1 ***
 *
 * Notice that the Gain object has three arguments in its constructor: (ac, 1, 0.2f).
 * These arguments are:
 *      ac = the AudioContext that manages all of our audio devices.
 *      1 = the number of channels that our Gain object should have.
 *      0.2f = the gain. This is the number we use to multiply the signal by.
 *
 * Try setting the gain to 0.4f and re-running the program. (If the program is already running, then in the Run window you can select the 're-run' button, a green circular arrow.
 *
 * *** Step 2 ***
 *
 * Let's create some other audio devices. We will create a WavePlayer object that will play back a sine tone.
 *
 * Below the line: Noise n = new Noise(ac);
 * Add the following code: WavePlayer wp = new WavePlayer(ac, 500f, Buffer.SINE);
 * Type this out. Don't copy and paste it. As you are typing the word Wave, notice that IntelliJ suggests code completions. When IntelliJ suggests "WavePlayer", hit return instead of typing the rest of the word. This not only completes the word but also imports the WavePlayer class into your project. Try to let IntelliJ do as much of the coding for you as possible. Use it to autocomplete the constructor, new WavePlayer(etc.), and the Buffer.SINE component.
 *
 * Two useful key commands are as follows:
 *      control-space will prompt an autocomplete.
 *      alt-return will try to automatically fix any problems at the place your caret is positioned.
 *
 * Now connect your WavePlayer to the Gain object.
 * In the line g.addInput(n), replace "n" with "wp".
 * Run the code. If you did this right you will now hear a sine tone instead of white noise.
 *
 * You can experiment by changing Buffer.SINE to Buffer.SQUARE and use autocomplete to see the other sounds.
 *
 * *** Step 3 ***
 *
 * Lastly, we will see how we can connect a sound to just one channel.
 * Go to the line Gain g = new Gain(ac, 1, 0.2f), and change the "1" to "2".
 * If you run the code after this change, nothing will be different. The "addInput()" command automatically connects a mono signal (from the WavePlayer) to both sides of the stereo signal (into the Gain object).
 *
 * Now go to the line g.addInput(wp) and change it to g.addInput(0, wp, 0);
 * Run that.
 * Now change it to g.addInput(1, wp, 0);
 * Run that.
 *
 * You should notice that you are changing which speaker your sound is coming out of (make sure you are listening in stereo!). The first argument is the choice of input channel on the gain object. As you change this from 0 to 1 you switch from the left speaker to the right speaker.
 *
 */
public class CodeTask1_1 extends Application {

    public static void main(String[] args) {
        /*
        Note for those used to Java, you may not be familiar with a JavaFX application.
        JavaFX applications look a little different to regular Java programs.
        This 'launch()' function does some Application setup under the hood. Once that's done, the 'start()' function below gets called. This is where you should do your initialisation in a JavaFX program.
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
