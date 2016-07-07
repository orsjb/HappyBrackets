package net.happybrackets.controller.gui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.beadsproject.beads.core.AudioContext;

/**
 * Created by ollie on 7/07/2016.
 */
public abstract class WaveformVisualiser {

    public static void open(AudioContext ac) {
        open(ac, true);
    }

    public static void open(AudioContext ac, boolean killAudioOnClose) {
        Stage stage = new Stage();
        stage.setResizable(false);
        Canvas c = new Canvas();
        c.setWidth(500);
        c.setHeight(300);
        Label l = new Label("Waveform");
        l.setAlignment(Pos.CENTER);
        VBox vb = new VBox(4, l, c);
        vb.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vb));
        stage.show();
        if(killAudioOnClose) {
            //we have to make sure stops when the window closes, else the program never exits
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    ac.stop();
                }       //not always desired behaviour
            });
        }
        stage.show();
        new Thread() {
            public void run() {
                while(stage.isShowing()) {
                    GraphicsContext g2d = c.getGraphicsContext2D();
                    g2d.clearRect(0, 0, c.getWidth(), c.getHeight());
                    float[] buf = ac.out.getOutBuffer(0);
                    for(int chan = 0; chan < ac.out.getOuts(); chan++) {
                        for (int i = 0; i < buf.length; i++) {
                            float f = buf[i];
                            int x = (int) ((float) i / buf.length * c.getWidth());
                            int y = (int) (((f * 0.5f + 0.5) + chan) * c.getHeight()/ac.out.getOuts());
                            g2d.fillOval(x, y, 0.7, 0.7);
                        }
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
