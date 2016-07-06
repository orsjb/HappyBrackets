package net.happybrackets.tutorial.session1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * Created by ollie on 5/06/2016.
 *
 * This example is more of a teaser into making a full desktop application. It uses JavaFX.
 *
 * We won't go through this code in detail, and you shouldn't worry if it doesn't make any sense.
 * However, as an optional extra task, hack the below code to make a simple keyboard that plays the notes C, C#, D, D# etc. You can use HBox instead of VBox to lay buttons out horizontally.
 *
 */
public class CodeTask1_4 extends Application {

    public static void main(String[] args) {
        /*
        JavaFX applications look a little different to regular Java programs.
        This 'launch()' function does some Application setup under the hood. Once that's done, the 'start()' function below gets called. This is where you should do your initialisation in a JavaFX program.
         */
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Audio stuff, once again we play a simple sine tone
        AudioContext ac = new AudioContext();
        ac.start();
        WavePlayer wp = new WavePlayer(ac, 500, Buffer.SINE);
        ac.out.addInput(wp);
        ac.out.setGain(0.1f);
        //create two buttons and give them actions
        Button btnH = new Button("High");
        btnH.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                wp.setFrequency(500);
            }
        });
        Button btnL = new Button("Low");
        btnL.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                wp.setFrequency(250f);
            }
        });
        //lay the buttons out in a window
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(btnH, btnL);
        primaryStage.setScene(new Scene(root, 300, 250));
        //we have to make sure Beads stops when the window closes, else the program never exits
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                ac.stop();
            }
        });
        primaryStage.show();
    }
}

