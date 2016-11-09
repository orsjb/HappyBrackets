package net.happybrackets.assignment_tasks.session1;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * This code is identical to the AboutTheCodeTasks example, which has the same functional behaviour as the HelloWorldBeads example.
 *
 * Let's play with some of the code. Since this is our first time coding together, this first task is mainly dedicated to seeing how well you understand Java code. We will only be looking at the task() function below. Ignore everything else.
 *
 * Remember that to run this code you should control-click, or right-click on this file and select "Run CodeTask1_1.main()". To re-run the code you can hit the green arrow up in the top right of the window in IntelliJ. If the program is already running, then there should be a Run window below. You can press the 're-run' button, a green circular arrow, to kill and restart the program.
 *
 * *** Step 1 ***
 *
 * Notice that the Gain object has three arguments in its constructor: (ac, 1, 0.1f).
 * These arguments are:
 *      ac = the AudioContext that manages all of our audio devices.
 *      1 = the number of channels that our Gain object should have.
 *      0.1f = the gain. This is the number we use to multiply the signal by.
 *
 * Try setting the gain to 0.2f and re-running the program.
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
 * Run the code.
 *
 * Now change Buffer.SINE to Buffer.SQUARE (try to use autocomplete to see the available options).
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
 * You should notice that you are changing which speaker your sound is coming out of (make sure you are listening in stereo!).
 *
 * *** Step 4 ***
 *
 * Now complete the task. Building on the code above, make changes to your final submission so that it has the following properties:
 * - a stereo gain with gain value 0.3.
 * - a WavePlayer object, with a SAW wave, playing at 1000hz plugged into the right channel.
 * - a Noise object plugged into the left channel.
 *
 */
public class CodeTask1_1 extends Application implements BeadsChecker.BeadsCheckable {

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
        //********** do your work here ONLY **********
    }
}
