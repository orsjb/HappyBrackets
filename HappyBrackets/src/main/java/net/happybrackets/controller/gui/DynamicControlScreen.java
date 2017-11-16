package net.happybrackets.controller.gui;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

public class DynamicControlScreen {
    // define default height and width
    static final int DEFAULT_SCREEN_WIDTH = 500;
    static final int DEFAULT_SCREEN_HEIGHT = 500;

    private static final int MIN_TEXT_AREA_HEIGHT = 200;
    private final int DEFAULT_ELEMENT_SPACING = 10;

    private class ControlCellGroup {

        public ControlCellGroup(Node label, Node control){
            labelNode = label;
            controlNode = control;
        }
        Node labelNode;
        Node controlNode;
        DynamicControl.DynamicControlListener listener = null;
        DynamicControl.ControlScopeChangedListener scopeChangedListener = null;

    }

    private Map<String, ControlCellGroup> dynamicControlsList = new Hashtable<String, ControlCellGroup>();

    private final LocalDeviceRepresentation localDevice;
    private Stage dynamicControlStage = null;
    private GridPane dynamicControlPane = new GridPane();
    private Scene dynamicControlScene = null;
    private int next_control_row = 0;
    private Object controlCreateLock = new Object();
    private final ScrollBar scrollBar = new ScrollBar();
    private BorderPane main_container = new BorderPane();
    private ScrollPane scrollPane;

    TitledPane debugPane = null;

    private TextArea logOutputTextArea = new TextArea();
    private LocalDeviceRepresentation.LogListener deviceLogListener = null;

    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public void setAlwaysOnTop(boolean always_on_top) {
        try {
            if (dynamicControlStage != null) {
                dynamicControlStage.setAlwaysOnTop(always_on_top);
                alwaysOnTop = always_on_top;
            }
        }
        catch (Exception ex){}
    }

    private boolean alwaysOnTop = false;

    /**
     * Create a screen to display controls for a LocalDevice
     * @param local_device the local device we are displaying controls for
     */
    public DynamicControlScreen(LocalDeviceRepresentation local_device){
        localDevice = local_device;
    }

    public void removeDynamicControlScene()
    {
        Platform.runLater(new Runnable() {
            public void run() {
                synchronized (controlCreateLock) {
                    if (dynamicControlStage != null) {
                        dynamicControlStage.close();
                        dynamicControlPane.getChildren().clear();
                        dynamicControlsList.clear();
                    }
                    next_control_row = 0;
                    localDevice.removeLogListener(deviceLogListener);
                }
            }
        });
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
    }

    /**
     * Remove a dynamic control from this window
     * @param control The control to remove. Will defer itself to main thread
     */
    public void  removeDynamicControl(DynamicControl control)
    {
        Platform.runLater(new Runnable() {
            public void run() {
                // find the control based on its hash from control table
                ControlCellGroup control_pair = dynamicControlsList.get(control.getControlMapKey());

                if (control_pair != null) {
                    dynamicControlPane.getChildren().remove(control_pair.controlNode);
                    dynamicControlPane.getChildren().remove(control_pair.labelNode);
                    dynamicControlsList.remove(control.getControlMapKey());

                    if (control_pair.listener != null) {
                        control.removeControlListener(control_pair.listener);
                    }

                    if (control_pair.scopeChangedListener != null){
                        control.removeControlScopeChangedListener(control_pair.scopeChangedListener);
                    }
                    rebuildGridList();
                }
            }
        });
    }

    public void createDynamicControlStage(){
        Platform.runLater(new Runnable() {
            public void run() {
                synchronized (controlCreateLock) {
                    if (dynamicControlStage == null) {


                        debugPane = new TitledPane("Debug", makeDebugPane());

                        debugPane.setExpanded(false);

                        dynamicControlStage = new Stage();
                        dynamicControlStage.setTitle(localDevice.deviceName);
                        ;
                        dynamicControlPane.setHgap(DEFAULT_ELEMENT_SPACING);
                        dynamicControlPane.setVgap(DEFAULT_ELEMENT_SPACING);
                        dynamicControlPane.setPadding(new Insets(DEFAULT_ELEMENT_SPACING * 2, DEFAULT_ELEMENT_SPACING * 2, 0, DEFAULT_ELEMENT_SPACING * 2));

                        dynamicControlScene = new Scene(main_container, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);

                        dynamicControlStage.setScene(dynamicControlScene);

                        scrollPane = new ScrollPane(dynamicControlPane);
                        scrollPane.setFitToHeight(true);

                        main_container.setBottom(debugPane);
                        main_container.setCenter(scrollPane);

                        scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
                            public void changed(ObservableValue<? extends Number> ov,
                                                Number old_val, Number new_val) {
                                dynamicControlPane.setLayoutY(-new_val.doubleValue());
                            }
                        });


                        final ContextMenu contextMenu = new ContextMenu();
                        CheckMenuItem always_on_top = new CheckMenuItem("Always on top");
                        always_on_top.setSelected(alwaysOnTop);
                        contextMenu.getItems().addAll(always_on_top);

                        scrollPane.setContextMenu(contextMenu);
                        debugPane.setContextMenu(contextMenu);
                        always_on_top.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        setAlwaysOnTop(!alwaysOnTop);
                                        always_on_top.setSelected(alwaysOnTop);
                                    }
                                });
                            }
                        });
                    }

                }
            }
        });
    }

    /**
     * Add A dynamic Control to window. Will execute in main thread
     * @param control The DynamicControl to add
     */
    public void addDynamicControl(DynamicControl control)
    {


        Platform.runLater(new Runnable() {
            public void run() {

                ControlCellGroup control_pair = dynamicControlsList.get(control.getControlMapKey());

                if (control_pair == null) {

                    Label control_label = new Label(control.getControlName());

                    dynamicControlPane.add(control_label, 0, next_control_row);


                    ControlType control_type = control.getControlType();
                    switch (control_type) {
                        case TRIGGER:
                            Button b = new Button();

                            control.setTooltipPrefix("Press button to generate a trigger event for this control");
                            b.setTooltip(new Tooltip(control.getTooltipText()));
                            b.setText("Send");
                            dynamicControlPane.add(b, 1, next_control_row);
                            control_pair = new ControlCellGroup(control_label, b);
                            dynamicControlsList.put(control.getControlMapKey(), control_pair);
                            b.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent e) {
                                    control.setValue(1);
                                    localDevice.sendDynamicControl(control);
                                }
                            });


                            control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                @Override
                                public void controlScopeChanged(ControlScope new_scope) {
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                            b.setTooltip(new Tooltip(control.getTooltipText()));
                                        }
                                    });

                                }
                            };

                            break;

                        case INT:
                            int control_value = (int) control.getValue();
                            // If we have no difference between Maximum and Minimum, we will make a textboox
                            if (control.getMinimumDisplayValue().equals(control.getMaximumDisplayValue())) {
                                TextField t = new TextField();
                                control.setTooltipPrefix("Type in an integer value and press enter to generate an event for this control");
                                t.setTooltip(new Tooltip(control.getTooltipText()));
                                t.setMaxWidth(100);
                                t.setText(Integer.toString(control_value));
                                dynamicControlPane.add(t, 1, next_control_row);
                                control_pair = new ControlCellGroup(control_label, t);
                                dynamicControlsList.put(control.getControlMapKey(), control_pair);
                                t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                                    @Override
                                    public void handle(KeyEvent event) {
                                        if (event.getCode().equals(KeyCode.ENTER)) {
                                            String text_val = t.getText();
                                            try {
                                                int control_value = Integer.valueOf(text_val);

                                                control.setValue(control_value);
                                                localDevice.sendDynamicControl(control);
                                            }
                                            catch (Exception ex){
                                                // we might want to put an exception here
                                            }
                                        }
                                    }
                                });

                                // set handlers
                                t.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        String text_val = t.getText();
                                        try {
                                            int control_value = Integer.valueOf(text_val);

                                            control.setValue(control_value);
                                            localDevice.sendDynamicControl(control);
                                        }
                                        catch (Exception ex){
                                            // we might want to put an exception here
                                        }
                                    }
                                });

                                control_pair.listener = new DynamicControl.DynamicControlListener() {
                                    @Override
                                    public void update(DynamicControl control) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                t.setText(Integer.toString((int) control.getValue()));

                                            }
                                        });
                                    }
                                };

                                control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                    @Override
                                    public void controlScopeChanged(ControlScope new_scope) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                t.setTooltip(new Tooltip(control.getTooltipText()));
                                            }
                                        });

                                    }
                                };

                            }
                            else {
                                Slider s = new Slider((int) control.getMinimumDisplayValue(), (int) control.getMaximumDisplayValue(), (int) control.getValue());
                                control.setTooltipPrefix("Change the slider value to generate an event for this control");
                                s.setTooltip(new Tooltip(control.getTooltipText()));
                                s.setMaxWidth(100);
                                s.setOrientation(Orientation.HORIZONTAL);
                                dynamicControlPane.add(s, 1, next_control_row);
                                control_pair = new ControlCellGroup(control_label, s);
                                dynamicControlsList.put(control.getControlMapKey(), control_pair);

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

                                control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                    @Override
                                    public void controlScopeChanged(ControlScope new_scope) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                s.setTooltip(new Tooltip(control.getTooltipText()));
                                            }
                                        });

                                    }
                                };
                            }
                            break;

                        case BOOLEAN:
                            CheckBox c = new CheckBox();
                            control.setTooltipPrefix("Change the check state to generate an event for this control");
                            c.setTooltip(new Tooltip(control.getTooltipText()));
                            int i_val = (int) control.getValue();
                            c.setSelected(i_val != 0);
                            dynamicControlPane.add(c, 1, next_control_row);

                            control_pair = new ControlCellGroup(control_label, c);
                            dynamicControlsList.put(control.getControlMapKey(), control_pair);

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

                            control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                @Override
                                public void controlScopeChanged(ControlScope new_scope) {
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                            c.setTooltip(new Tooltip(control.getTooltipText()));
                                        }
                                    });

                                }
                            };
                            break;

                        case FLOAT:
                            float f_control_value = (float) control.getValue();
                            // If we have no difference between Maximum and Minimum, we will make a textboox
                            if (control.getMinimumDisplayValue().equals(control.getMaximumDisplayValue())) {
                                TextField t = new TextField();
                                control.setTooltipPrefix("Type in a float value and press enter to generate an event for this control");
                                t.setTooltip(new Tooltip(control.getTooltipText()));
                                t.setMaxWidth(100);
                                t.setText(Float.toString(f_control_value));
                                dynamicControlPane.add(t, 1, next_control_row);
                                control_pair = new ControlCellGroup(control_label, t);
                                dynamicControlsList.put(control.getControlMapKey(), control_pair);
                                t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                                    @Override
                                    public void handle(KeyEvent event) {
                                        if (event.getCode().equals(KeyCode.ENTER)) {
                                            String text_val = t.getText();
                                            try {
                                                float control_value = Float.valueOf(text_val);

                                                control.setValue(control_value);
                                                localDevice.sendDynamicControl(control);
                                            }
                                            catch (Exception ex){
                                                // we might want to put an exception here
                                            }
                                        }
                                    }
                                });

                                // set handlers
                                t.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        String text_val = t.getText();
                                        try {
                                            float control_value = Float.valueOf(text_val);

                                            control.setValue(control_value);
                                            localDevice.sendDynamicControl(control);
                                        }
                                        catch (Exception ex){
                                            // we might want to put an exception here
                                        }
                                    }
                                });

                                control_pair.listener = new DynamicControl.DynamicControlListener() {
                                    @Override
                                    public void update(DynamicControl control) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                t.setText(Float.toString((float) control.getValue()));
                                            }
                                        });
                                    }
                                };


                                control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                    @Override
                                    public void controlScopeChanged(ControlScope new_scope) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                t.setTooltip(new Tooltip(control.getTooltipText()));

                                            }
                                        });

                                    }
                                };
                            }
                            else {
                                Slider f = new Slider((float) control.getMinimumDisplayValue(), (float) control.getMaximumDisplayValue(), (float) control.getValue());
                                f.setMaxWidth(100);
                                control.setTooltipPrefix("Change the slider value to generate an event for this control");
                                f.setTooltip(new Tooltip(control.getTooltipText()));
                                f.setOrientation(Orientation.HORIZONTAL);
                                dynamicControlPane.add(f, 1, next_control_row);
                                control_pair = new ControlCellGroup(control_label, f);
                                dynamicControlsList.put(control.getControlMapKey(), control_pair);

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

                                control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                    @Override
                                    public void controlScopeChanged(ControlScope new_scope) {
                                        Platform.runLater(new Runnable() {
                                            public void run() {
                                                f.setTooltip(new Tooltip(control.getTooltipText()));
                                            }
                                        });

                                    }
                                };
                            }
                            break;

                        case TEXT:
                            TextField t = new TextField();
                            control.setTooltipPrefix("Type in text and press enter to generate an event for this control");
                            t.setTooltip(new Tooltip(control.getTooltipText()));
                            t.setMaxWidth(100);
                            t.setText((String) control.getValue());
                            dynamicControlPane.add(t, 1, next_control_row);
                            control_pair = new ControlCellGroup(control_label, t);
                            dynamicControlsList.put(control.getControlMapKey(), control_pair);
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

                            control_pair.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                                @Override
                                public void controlScopeChanged(ControlScope new_scope) {
                                    Platform.runLater(new Runnable() {
                                        public void run() {
                                            t.setTooltip(new Tooltip(control.getTooltipText()));
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
                    if (control_pair.scopeChangedListener != null){
                        control.addControlScopeListener(control_pair.scopeChangedListener);
                    }

                    next_control_row++;

                    show();
                }
            }
        });
    }


    public void show(){
        dynamicControlStage.show();
        dynamicControlStage.toFront();
    }

    /**
     * Add a debug pane
     * @return
     */
    private Node makeDebugPane() {
        String start_text = "Start device logging";
        Tooltip start_tooltip = new Tooltip("Tell this devices to start sending its logging information.");
        Tooltip stop_tooltip = new Tooltip("Tell this devices to stop sending its logging information.");
        String stop_text = "Stop device logging";
        boolean logging_enabled = localDevice.isLoggingEnabled();


        Button enable_button = new Button(logging_enabled ? stop_text : start_text);
        enable_button.setTooltip(logging_enabled ? stop_tooltip : start_tooltip);
        enable_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean logging_enabled = !localDevice.isLoggingEnabled();

                localDevice.setLogging(logging_enabled);
                enable_button.setText(logging_enabled ? stop_text : start_text);
                enable_button.setTooltip(logging_enabled ? stop_tooltip : start_tooltip);
            }
        });

        logOutputTextArea.setMinHeight(MIN_TEXT_AREA_HEIGHT);

        localDevice.addLogListener(deviceLogListener = new LocalDeviceRepresentation.LogListener() {
            @Override
            public void newLogMessage(String message) {
                logOutputTextArea.appendText(message);

            }
        });


        VBox pane = new VBox(DEFAULT_ELEMENT_SPACING);
        pane.getChildren().addAll(enable_button, logOutputTextArea);
        return pane;
    }

}
