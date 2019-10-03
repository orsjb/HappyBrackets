package net.happybrackets.device;

import javafx.application.Application;
import javafx.stage.Stage;
import net.happybrackets.controller.gui.DynamicControlScreen;
import net.happybrackets.controller.gui.WaveformVisualiser;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;

import java.util.List;

/**
 * THis class enables us to Display the Dynamic Controls during Debug of Happy Brackets Program
 */
public class DebugApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //System.out.println("Program in Simulation mode");

        DynamicControlScreen debugControlsScreen = new DynamicControlScreen("Simulation");


        debugControlsScreen.addDynamicControlScreenLoadedListener(new DynamicControlScreen.DynamicControlScreenLoaded() {
            @Override
            public void loadComplete(DynamicControlScreen screen, boolean loaded) {
                // screen load is complete.
                //Now Add all controls
                ControlMap control_map = ControlMap.getInstance();

                List<DynamicControl> controls = control_map.GetSortedControls();

                for (DynamicControl control : controls) {
                    if (control != null) {
                        debugControlsScreen.addDynamicControl(control);
                        debugControlsScreen.show();
                    }
                }

                // Now make a listener for Dynamic Controls that are made during the HB Action event
                control_map.addDynamicControlCreatedListener(new ControlMap.dynamicControlCreatedListener() {
                    @Override
                    public void controlCreated(DynamicControl control) {
                        debugControlsScreen.addDynamicControl(control);
                        debugControlsScreen.show();
                    }
                });

            }
        });

        // now we have a listener to see when stage is made, let us load the stage
        debugControlsScreen.createDynamicControlStage();

        WaveformVisualiser.open(HB.HBInstance.ac);
    }
}
