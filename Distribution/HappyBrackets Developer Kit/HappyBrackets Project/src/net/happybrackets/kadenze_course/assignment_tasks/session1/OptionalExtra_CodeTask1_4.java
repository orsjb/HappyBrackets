/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.kadenze_course.assignment_tasks.session1;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.WavePlayer;

/**
 * This example is more of a teaser into making a full desktop application. It uses JavaFX to create a button.
 *
 * We won't go through this code in detail, and you shouldn't worry if there are parts that don't make sense.
 * However, as an optional extra task, hack the below code to make a simple keyboard that plays the notes C, C#, D, D#
 * etc. You can use HBox instead of VBox to lay buttons out horizontally.
 *
 * This task will not be checked.
 */
public class OptionalExtra_CodeTask1_4 extends Application {

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

