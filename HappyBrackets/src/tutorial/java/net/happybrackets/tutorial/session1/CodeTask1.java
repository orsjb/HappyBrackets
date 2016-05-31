package net.happybrackets.tutorial.session1;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Noise;

/**
 * Created by ollie on 10/05/2016.
 *
 * This is the "Hello World" of Beads, the Java realtime audio library used in HappyBrackets.
 * From IntelliJ, control-click or right-click on this file (i.e., click right here). About 1/3 the way down you will see the option to "Run CodeTask1.main()", with a green triangle next to it. Select this option. You should hear some white noise playing back through your speakers. If you do not hear anything, check your sound is on, and look below in the "Run" window to see if there are any Java exceptions. These are blocks of code that alert you to an error.
 *
 * To stop the program, you can click the red square down below in the Run window. Note if you can't see the "Run" window you can go to the "View" menu and look under "Tool Windows".
 *
 * Congratulations. You are now running your first Beads program.
 *
 * Let's play with some of the code.
 *
 * Step 1) Notice that Gain has three arguments. The first argument
 *
 *
 *
 */
public class CodeTask1 {

    public static void main(String[] args) {
        //set up the AudioContext and start it
        AudioContext ac = new AudioContext();
        ac.start();
        //create a Noise genrator and a Gain controller
        Noise n = new Noise(ac);
        Gain g = new Gain(ac, 1, 0.2f);
        //plug it all together
        g.addInput(n);
        ac.out.addInput(g);
        //say something to the console output
        System.out.println("Hello World! We are running Beads.");
    }
}
