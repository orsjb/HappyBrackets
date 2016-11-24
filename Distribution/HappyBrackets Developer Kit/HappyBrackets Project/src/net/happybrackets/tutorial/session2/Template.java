package net.happybrackets.tutorial.session2;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This code just gives you a button to use as an input. Experiment with the use of the button to control Beads elements, as in the lesson.
 */
public class Template extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //graphics setup
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
                //button action here
            }
        });
        layout.getChildren().add(b);
    }
}
