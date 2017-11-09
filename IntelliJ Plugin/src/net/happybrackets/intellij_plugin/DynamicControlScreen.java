package net.happybrackets.intellij_plugin;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class DynamicControlScreen {
    // define default height and width
    static final int DEFAULT_SCREEN_WIDTH = 500;
    static final int DEFAULT_SCREEN_HEIGHT = 500;

    private class ControlCellGroup {

        public ControlCellGroup(Node label, Node control){
            labelNode = label;
            controlNode = control;
        }
        Node labelNode;
        Node controlNode;
        DynamicControl.DynamicControlListener listener = null;

    }

    private Map<Integer, ControlCellGroup> dynamicControlsList = new Hashtable<Integer, ControlCellGroup>();

    private final LocalDeviceRepresentation localDevice;
    Stage dynamicControlStage = null;
    GridPane dynamicControlPane = new GridPane();
    Scene dynamicControlScenen = null;
    int next_control_row = 0;
    Object controlCreateLock = new Object();
    final ScrollBar scrollBar = new ScrollBar();

    /**
     * Create a screen to display controls for a LocalDevice
     * @param local_device the local device we are displaying controls for
     */
    public DynamicControlScreen(LocalDeviceRepresentation local_device){
        localDevice = local_device;
    }

    void removeDynamicControlScene()
    {
        synchronized (controlCreateLock) {
            if (dynamicControlStage != null) {
                dynamicControlStage.close();
                dynamicControlPane.getChildren().clear();
                dynamicControlsList.clear();
            }
            next_control_row = 0;
        }
    }

    void rebuildGridList()
    {
        dynamicControlPane.getChildren().clear();
        next_control_row = 0;
        Collection<ControlCellGroup> control_pairs =  dynamicControlsList.values();
        for (ControlCellGroup control_pair : control_pairs) {
            dynamicControlPane.add(control_pair.labelNode, 0, next_control_row);
            dynamicControlPane.add(control_pair.controlNode, 1, next_control_row);
            next_control_row++;
        }

        setScrollPreferences();

    }

    void  removeDynamicControl(DynamicControl control)
    {
        // find the control based on its hash from control table
        ControlCellGroup control_pair = dynamicControlsList.get(control.getControlHashCode());

        if (control_pair != null)
        {
            dynamicControlPane.getChildren().remove(control_pair.controlNode);
            dynamicControlPane.getChildren().remove(control_pair.labelNode);
            dynamicControlsList.remove(control.getControlHashCode());

            if (control_pair.listener != null) {
                control.removeControlListener(control_pair.listener);
            }
            rebuildGridList();
        }
    }

    void createDynamicControlStage(){
        synchronized (controlCreateLock)
        {
            if (dynamicControlStage == null) {

                Group root = new Group();
                dynamicControlStage = new Stage();
                dynamicControlStage.setTitle(localDevice.deviceName);;
                dynamicControlPane.setHgap(10);
                dynamicControlPane.setVgap(10);
                dynamicControlPane.setPadding(new Insets(20, 20, 0, 20));

                dynamicControlScenen = new Scene(root, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
                //dynamicControlScenen = new Scene(dynamicControlPane, 500, 500);

                dynamicControlStage.setScene(dynamicControlScenen);

                root.getChildren().addAll(dynamicControlPane, scrollBar);

                setScrollPreferences();

                scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue<? extends Number> ov,
                                        Number old_val, Number new_val) {
                        dynamicControlPane.setLayoutY(-new_val.doubleValue());
                    }
                });

                // Modify scroll bar position if we resize the window
                dynamicControlStage.widthProperty().addListener((obs, oldVal, newVal) -> {
                    setScrollPreferences();
                });

                dynamicControlStage.heightProperty().addListener((obs, oldVal, newVal) -> {
                    setScrollPreferences();
                });
            }

        }
    }

    /**
     * Add A dynamic Control to window. Must be called in context of main thread
     * @param control
     */
    void addDynamicControl(DynamicControl control)
    {
        ControlCellGroup control_pair = dynamicControlsList.get(control.getControlHashCode());

        if (control_pair == null) {

            Label control_label = new Label(control.getControlName());

            dynamicControlPane.add(control_label, 0, next_control_row);


            ControlType control_type = control.getControlType();
            switch (control_type) {
                case BUTTON:
                    Button b = new Button();
                    b.setText("Send");
                    dynamicControlPane.add(b, 1, next_control_row);
                    control_pair = new ControlCellGroup(control_label, b);
                    dynamicControlsList.put(control.getControlHashCode(), control_pair);
                    b.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            control.setValue(1);
                            localDevice.sendDynamicControl(control);
                        }
                    });


                    break;

                case SLIDER:
                    Slider s = new Slider((int) control.getMinimumValue(), (int) control.getMaximumValue(), (int) control.getValue());
                    s.setMaxWidth(100);
                    s.setOrientation(Orientation.HORIZONTAL);
                    dynamicControlPane.add(s, 1, next_control_row);
                    control_pair = new ControlCellGroup(control_label, s);
                    dynamicControlsList.put(control.getControlHashCode(), control_pair);

                    s.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
                            if (s.isFocused()) {
                                if (oldval != newval) {
                                    control.setValue(newval.intValue());
                                    localDevice.sendDynamicControl(control);
                                }
                            }
                        }
                    });

                    control_pair.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    if (!s.isFocused()) {
                                        s.setValue((int) control.getValue());
                                    }
                                }
                            });
                        }
                    };

                    break;

                case CHECKBOX:
                    CheckBox c = new CheckBox();
                    int i_val = (int) control.getValue();
                    c.setSelected(i_val != 0);
                    dynamicControlPane.add(c, 1, next_control_row);

                    control_pair = new ControlCellGroup(control_label, c);
                    dynamicControlsList.put(control.getControlHashCode(), control_pair);

                    c.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        public void changed(ObservableValue<? extends Boolean> ov,
                                            Boolean oldval, Boolean newval) {
                            if (oldval != newval) {
                                control.setValue(newval ? 1 : 0);
                                localDevice.sendDynamicControl(control);
                            }
                        }
                    });

                    control_pair.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    if (!c.isFocused()) {
                                        int i_val = (int) control.getValue();
                                        c.setSelected(i_val != 0);
                                    }
                                }
                            });
                        }
                    };
                    break;

                case FLOAT:
                    Slider f = new Slider((float) control.getMinimumValue(), (float) control.getMaximumValue(), (float) control.getValue());
                    f.setMaxWidth(100);
                    f.setOrientation(Orientation.HORIZONTAL);
                    dynamicControlPane.add(f, 1, next_control_row);
                    control_pair = new ControlCellGroup(control_label, f);
                    dynamicControlsList.put(control.getControlHashCode(), control_pair);

                    f.valueProperty().addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
                            if (f.isFocused()) {
                                if (oldval != newval) {
                                    control.setValue(newval.floatValue());
                                    localDevice.sendDynamicControl(control);
                                }
                            }
                        }
                    });

                    control_pair.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    if (!f.isFocused()) {
                                        f.setValue((float) control.getValue());
                                    }
                                }
                            });
                        }
                    };
                    break;

                case TEXT:
                    TextField t = new TextField();
                    t.setMaxWidth(100);
                    t.setText((String) control.getValue());
                    dynamicControlPane.add(t, 1, next_control_row);
                    control_pair = new ControlCellGroup(control_label, t);
                    dynamicControlsList.put(control.getControlHashCode(), control_pair);
                    t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent event) {
                            if (event.getCode().equals(KeyCode.ENTER)) {
                                String text_val = t.getText();
                                control.setValue(text_val);
                                localDevice.sendDynamicControl(control);
                            }
                        }
                    });

                    // set handlers
                    t.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            String text_val = t.getText();
                            control.setValue(text_val);
                            localDevice.sendDynamicControl(control);
                        }
                    });

                    control_pair.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {

                                    t.setText((String) control.getValue());
                                }
                            });
                        }
                    };
                    break;

                default:
                    break;
            }

            if (control_pair.listener != null) {
                control.addControlListener(control_pair.listener);
            }

            next_control_row++;
            setScrollPreferences();

            show();
        }
    }

    private void setScrollPreferences() {
        scrollBar.setLayoutX(dynamicControlScenen.getWidth()- scrollBar.getWidth());
        scrollBar.setMin(0);
        scrollBar.setOrientation(Orientation.VERTICAL);

        scrollBar.setPrefHeight(dynamicControlScenen.getHeight());
        scrollBar.setMax(dynamicControlPane.getHeight() - dynamicControlScenen.getHeight() / 2);
    }

    public void show(){
        dynamicControlStage.show();
        dynamicControlStage.toFront();
    }

}
