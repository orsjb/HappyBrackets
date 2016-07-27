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
 * Created by ollie on 25/07/2016.
 */
public class Example2_1 extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //set up the audio context
        AudioContext ac = new AudioContext();
        ac.start();
        //add the waveplayer
        Envelope e = new Envelope(ac, 500);
        WavePlayer wp = new WavePlayer(ac, e, Buffer.SINE);
        //add the gain
        Glide glide = new Glide(ac, 0.1f);
        Gain g = new Gain(ac, 1, glide);
        e.addSegment(1000, 2000);
        e.addSegment(500, 200, new Bead() {
            @Override
            protected void messageReceived(Bead bead) {
                System.out.println("NEW BEAD!");
            }
        });
        //connect together
        g.addInput(wp);
        ac.out.addInput(g);
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
