package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.controller.gui.WaveformVisualiser;

/**
 * This example shows the use of Glide, which can be used to smooth the signal from a discrete action.
 * It also shows the use of the concept of triggers, and the use of the Beads class to receive trigger actions from an envelope.
 *
 * In this example the Glide controls to volume, and the Envelope controls the frequency.
 *
 * The code at the end is all JavaFX GUI code. It is not essential to the course, but gives a rough idea of how to program simple GUI elements in Java.
 *
 */
public class Example2_2 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //add the waveplayer
        //we control the frequency value of the WavePlayer with an Envelope object
        Envelope e = new Envelope(ac, 500);
        WavePlayer wp = new WavePlayer(ac, e, Buffer.SINE);
        //add the gain
        //we control the gain value of the Gain object with a Glide object
        Glide glide = new Glide(ac, 0.1f);
        //the Gain object itself takes the Glide object as its third argument
        Gain g = new Gain(ac, 1, glide);
        //now control what will happen to the frequency, and add an event at the end
        //this event simply prints out a message. Notice that IntelliJ intelligently converts single
        //line actions into "lambdas".
        e.addSegment(1000, 2000);
        e.addSegment(500, 200, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                System.out.println("NEW BEAD!");
            }
        });
        //connect together the audio elements
        g.addInput(wp);
        ac.out.addInput(g);
        //this is the end of the audio code. What follows sets up the visualiser and also a button
        //that allows you to control the gain value via the Glide object you created above.
        //visualiser
        WaveformVisualiser.open(ac);
        //graphics code
        //set up the scene
        VBox layout = new VBox();
        Scene s = new Scene(layout);
        primaryStage.setScene(s);
        primaryStage.show();
        //create a button
        Button b = new Button("Press Me!");
        b.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("BUTTON PRESSED");
                glide.setValue(0);
            }
        });
        layout.getChildren().add(b);
    }
}
