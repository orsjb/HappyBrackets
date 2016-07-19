package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * Created by ollie on 5/06/2016.
 *
 * Fix the bug in the following code (why can't you see the word 'tick' appearing in the console?).
 *
 */
public class CodeTask2_3 extends Application {

    public static void main(String[] args) {launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //audio stuff
        AudioContext ac = new AudioContext();
        ac.start();
        WaveformVisualiser.open(ac);
        //create a Clock
        Clock c = new Clock(ac, 500);
        //add some behaviour that responds to the clock
        c.addMessageListener(new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                System.out.println("tick");
            }
        });
    }
}
