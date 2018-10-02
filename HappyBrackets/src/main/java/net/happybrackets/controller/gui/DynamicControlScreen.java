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
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.happybrackets.controller.gui.device.PingMenu;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;

import javax.swing.*;
import java.util.*;


/**
 * The stage looks like this
 *
 * dynamicControlStage
 * dynamicControlScene
 *
 * BorderPane main_container
 * ** scrollPane
 * ** ** dynamicControlGridPane - GridPane
 *
 * *********
 *      for each Control
 *       ControlCellGroup
 *          ** Label
 *          ** Control
 * *********
 *
 * ** debugPane
 */

public class DynamicControlScreen {
    // define default height and width
    static final int DEFAULT_SCREEN_WIDTH = 500;
    static final int DEFAULT_SCREEN_HEIGHT = 500;

    private static final int MIN_TEXT_AREA_HEIGHT = 200;
    private final int DEFAULT_ELEMENT_SPACING = 10;

    private int currentLogPage = 0;

    Button previousLogPageButton = new Button("<");
    Button nextLogPageButton = new Button(">");
    TextField logPageNumber = new TextField("1");

    ColumnConstraints column1 = new ColumnConstraints(100,100, Double.MAX_VALUE);
    ColumnConstraints column2 = new ColumnConstraints(200,300,Double.MAX_VALUE);

    public interface DynamicControlScreenLoaded{
        void loadComplete(DynamicControlScreen screen, boolean loaded);
    }

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
    private GridPane dynamicControlGridPane = new GridPane();
    private Scene dynamicControlScene = null;
    private int nextControlRow = 0;
    private Object controlCreateLock = new Object();
    private final ScrollBar scrollBar = new ScrollBar();
    private BorderPane main_container = new BorderPane();
    private ScrollPane scrollPane;

    private String screenTitle;

    TitledPane debugPane = null;

    private List<DynamicControlScreenLoaded> dynamicControlScreenLoadedList = new ArrayList<>();

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

    public void addDynamicControlScreenLoadedListener(DynamicControlScreenLoaded listener){
        dynamicControlScreenLoadedList.add(listener);
    }
    /**
     * Create a screen to display controls for a LocalDevice
     * @param local_device the local device we are displaying controls for
     */
    public DynamicControlScreen(LocalDeviceRepresentation local_device){
        localDevice = local_device;
        screenTitle = localDevice.getFriendlyName();
        setGridColumnAttributes();
    }

    private void setGridColumnAttributes()
    {
        column1.setHgrow(Priority.ALWAYS);
        column2.setHgrow(Priority.ALWAYS);

        dynamicControlGridPane.getColumnConstraints().addAll(column1, column2);
    }
    /**
     * Create a Dynamic ControlScreen without a LocalDeviceRepresentation
     * This means that it will run independantly
     * @param title The title to display on the screen
     */
    public DynamicControlScreen (String title){
        localDevice = null;
        screenTitle = title;
        setGridColumnAttributes();
    }

    /**
     * Set the title of Our Display Screen
     * @param title the title to display on the screen
     */
    public void setTitle(String title) {
        Platform.runLater(() -> {
            try {
                screenTitle = title;
                dynamicControlStage.setTitle(screenTitle);
            } catch (Exception ex) {
            }
                }
        );
    }
    /**
     * Erase the dynamic controls on this screen
     */
    public void eraseDynamicControls()
    {
        DynamicControlScreen this_screen = this;

        Platform.runLater(new Runnable() {
            public void run() {
                synchronized (controlCreateLock) {
                    if (dynamicControlStage != null) {
                        dynamicControlGridPane.getChildren().clear();
                        dynamicControlsList.clear();
                    }
                    nextControlRow = 0;
                    for (DynamicControlScreenLoaded dynamicControlScreenLoaded : dynamicControlScreenLoadedList) {
                        dynamicControlScreenLoaded.loadComplete (this_screen, false);
                    }

                }
            }
        });
    }

    public void removeDynamicControlScene()
    {
        DynamicControlScreen this_screen = this;

        Platform.runLater(new Runnable() {
            public void run() {
                synchronized (controlCreateLock) {
                    if (dynamicControlStage != null) {
                        dynamicControlStage.close();
                        dynamicControlGridPane.getChildren().clear();
                        dynamicControlsList.clear();
                    }
                    nextControlRow = 0;
                    for (DynamicControlScreenLoaded dynamicControlScreenLoaded : dynamicControlScreenLoadedList) {
                        dynamicControlScreenLoaded.loadComplete (this_screen, false);
                    }

                }
            }
        });
    }

    void rebuildGridList()
    {
        dynamicControlGridPane.getChildren().clear();
        nextControlRow = 0;
        Collection<ControlCellGroup> control_groupds =  dynamicControlsList.values();
        for (ControlCellGroup control_group : control_groupds) {
            dynamicControlGridPane.add(control_group.labelNode, 0, nextControlRow);
            dynamicControlGridPane.add(control_group.controlNode, 1, nextControlRow);
            nextControlRow++;
        }
    }

    /**
     * Remove a dynamic control from this window
     * @param control The control to remove. Will defer itself to main thread
     */
    public void  removeDynamicControl(DynamicControl control)
    {
        Platform.runLater(() -> {
            // find the control based on its hash from control table
            ControlCellGroup control_group = dynamicControlsList.get(control.getControlMapKey());

            if (control_group != null) {
                dynamicControlGridPane.getChildren().remove(control_group.controlNode);
                dynamicControlGridPane.getChildren().remove(control_group.labelNode);
                dynamicControlsList.remove(control.getControlMapKey());

                if (control_group.listener != null) {
                    control.removeControlListener(control_group.listener);
                }

                if (control_group.scopeChangedListener != null){
                    control.removeControlScopeChangedListener(control_group.scopeChangedListener);
                }
                rebuildGridList();
            }
        });
    }

    public void createDynamicControlStage(){

        DynamicControlScreen this_screen = this;

        Platform.runLater(() -> {
            synchronized (controlCreateLock) {
                if (dynamicControlStage == null) {


                    if (localDevice != null) {
                        debugPane = new TitledPane("Debug", makeDebugPane());

                        debugPane.setExpanded(false);
                    }

                    dynamicControlStage = new Stage();
                    dynamicControlStage.setTitle(screenTitle);

                    dynamicControlGridPane.setHgap(DEFAULT_ELEMENT_SPACING);
                    dynamicControlGridPane.setVgap(DEFAULT_ELEMENT_SPACING);
                    dynamicControlGridPane.setPadding(new Insets(DEFAULT_ELEMENT_SPACING * 2, DEFAULT_ELEMENT_SPACING * 2, 0, DEFAULT_ELEMENT_SPACING * 2));

                    dynamicControlScene = new Scene(main_container, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);

                    dynamicControlStage.setScene(dynamicControlScene);

                    scrollPane = new ScrollPane(dynamicControlGridPane);
                    scrollPane.setFitToHeight(true);
                    scrollPane.setFitToWidth(true);

                    if (debugPane != null) {
                        main_container.setBottom(debugPane);
                    }

                    main_container.setCenter(scrollPane);

                    scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
                        public void changed(ObservableValue<? extends Number> ov,
                                            Number old_val, Number new_val) {
                            dynamicControlGridPane.setLayoutY(-new_val.doubleValue());
                        }
                    });


                    ContextMenu contextMenu = new ContextMenu();
                    CheckMenuItem always_on_top = new CheckMenuItem("Always on top");
                    always_on_top.setSelected(alwaysOnTop);
                    contextMenu.getItems().addAll(always_on_top);

                    if (localDevice != null)
                    {
                        contextMenu.getItems().add(new SeparatorMenuItem());
                        PingMenu menus = new PingMenu(localDevice);
                        contextMenu.getItems().addAll(menus.getMenuItems());

                    }

                    scrollPane.setContextMenu(contextMenu);
                    if (debugPane != null) {
                        debugPane.setContextMenu(contextMenu);
                    }

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

                for (DynamicControlScreenLoaded dynamicControlScreenLoaded : dynamicControlScreenLoadedList) {
                    dynamicControlScreenLoaded.loadComplete (this_screen, true);
                }
            }
        });
    }

    /**
     * Read the devices from Local device and load them in order
     * @param localDevice the device to read them from
     */
    public void loadDynamicControls (LocalDeviceRepresentation localDevice){


        Platform.runLater(new Runnable() {
            public void run() {

                DynamicControl control = localDevice.popNextPendingControl();
                while (control != null) {

                    addDynamicControl(control, nextControlRow);
                    nextControlRow++;

                    control = localDevice.popNextPendingControl();
                }
            }
        });
    }

    /**
     * Adds a control to the display window. Must be called in context of main thread
     * @param control the control to add
     * @param control_row the row to display it on
     */
    private void addDynamicControl (DynamicControl control, int control_row){
        ControlCellGroup control_group = dynamicControlsList.get(control.getControlMapKey());

        if (control_group == null) {

            // fist add a listeners to notify us of when this control gets removed
            ControlMap.getInstance().addDynamicControlRemovedListener(new ControlMap.dynamicControlRemovedListener() {
                @Override
                public void controlRemoved(DynamicControl control) {
                    removeDynamicControl(control);
                }
            });

            Label control_label = new Label(control.getControlName());

            dynamicControlGridPane.add(control_label, 0, control_row);


            ControlType control_type = control.getControlType();
            switch (control_type) {
                case TRIGGER:
                    Button b = new Button();

                    control.setTooltipPrefix("Press button to generate a trigger event for this control");
                    b.setTooltip(new Tooltip(control.getTooltipText()));
                    b.setText("Send");
                    b.setDisable(control.getDisabled());
                    dynamicControlGridPane.add(b, 1, control_row);
                    control_group = new ControlCellGroup(control_label, b);
                    dynamicControlsList.put(control.getControlMapKey(), control_group);
                    b.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent e) {
                            // This is a number so we have a stop condition
                            control.setValue(System.currentTimeMillis());
                        }
                    });


                    control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
                        @Override
                        public void controlScopeChanged(ControlScope new_scope) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    b.setTooltip(new Tooltip(control.getTooltipText()));
                                }
                            });

                        }
                    };

                    control_group.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    b.setDisable(control.getDisabled());

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
                        //t.setMaxWidth(100);
                        t.setText(Integer.toString(control_value));
                        t.setDisable(control.getDisabled());
                        dynamicControlGridPane.add(t, 1, control_row);
                        control_group = new ControlCellGroup(control_label, t);
                        dynamicControlsList.put(control.getControlMapKey(), control_group);
                        t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                            @Override
                            public void handle(KeyEvent event) {
                                if (event.getCode().equals(KeyCode.ENTER)) {
                                    String text_val = t.getText();
                                    try {
                                        int control_value = Integer.valueOf(text_val);

                                        control.setValue(control_value);
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
                                }
                                catch (Exception ex){
                                    // we might want to put an exception here
                                }
                            }
                        });

                        control_group.listener = new DynamicControl.DynamicControlListener() {
                            @Override
                            public void update(DynamicControl control) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        t.setText(Integer.toString((int) control.getValue()));
                                        t.setDisable(control.getDisabled());

                                    }
                                });
                            }
                        };

                        control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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
                        s.setDisable(control.getDisabled());
                        s.setOrientation(Orientation.HORIZONTAL);
                        dynamicControlGridPane.add(s, 1, control_row);
                        control_group = new ControlCellGroup(control_label, s);
                        dynamicControlsList.put(control.getControlMapKey(), control_group);

                        s.valueProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
                                if (s.isFocused()) {
                                    if (oldval != newval) {
                                        control.setValue(newval.intValue());
                                        //localDevice.sendDynamicControl(control);
                                    }
                                }
                            }
                        });

                        control_group.listener = new DynamicControl.DynamicControlListener() {
                            @Override
                            public void update(DynamicControl control) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        if (!s.isFocused()) {
                                            s.setValue((int) control.getValue());
                                        }
                                        s.setDisable(control.getDisabled());
                                    }
                                });
                            }
                        };

                        control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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
                    boolean b_val = (boolean) control.getValue();
                    c.setSelected(b_val);
                    c.setDisable(control.getDisabled());
                    dynamicControlGridPane.add(c, 1, control_row);

                    control_group = new ControlCellGroup(control_label, c);
                    dynamicControlsList.put(control.getControlMapKey(), control_group);

                    c.selectedProperty().addListener(new ChangeListener<Boolean>() {
                        public void changed(ObservableValue<? extends Boolean> ov,
                                            Boolean oldval, Boolean newval) {
                            if (oldval != newval) {
                                control.setValue(newval);
                                //localDevice.sendDynamicControl(control);
                            }
                        }
                    });

                    control_group.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    if (!c.isFocused()) {
                                        boolean b_val = (boolean) control.getValue();
                                        c.setSelected(b_val);
                                    }
                                    c.setDisable(control.getDisabled());
                                }
                            });
                        }
                    };

                    control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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
                        t.setDisable(control.getDisabled());
                        t.setText(Float.toString(f_control_value));
                        dynamicControlGridPane.add(t, 1, control_row);
                        control_group = new ControlCellGroup(control_label, t);
                        dynamicControlsList.put(control.getControlMapKey(), control_group);
                        t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                            @Override
                            public void handle(KeyEvent event) {
                                if (event.getCode().equals(KeyCode.ENTER)) {
                                    String text_val = t.getText();
                                    try {
                                        float control_value = Float.valueOf(text_val);

                                        control.setValue(control_value);
                                        //localDevice.sendDynamicControl(control);
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
                                    //localDevice.sendDynamicControl(control);
                                }
                                catch (Exception ex){
                                    // we might want to put an exception here
                                }
                            }
                        });

                        control_group.listener = new DynamicControl.DynamicControlListener() {
                            @Override
                            public void update(DynamicControl control) {
                                Platform.runLater(new Runnable() {
                                    public void run() {
                                        t.setText(Float.toString((float) control.getValue()));
                                        t.setDisable(control.getDisabled());
                                    }
                                });
                            }
                        };


                        control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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
                        //f.setMaxWidth(100);
                        control.setTooltipPrefix("Change the slider value to generate an event for this control");
                        f.setTooltip(new Tooltip(control.getTooltipText()));
                        f.setOrientation(Orientation.HORIZONTAL);
                        f.setDisable(control.getDisabled());
                        dynamicControlGridPane.add(f, 1, control_row);
                        control_group = new ControlCellGroup(control_label, f);
                        dynamicControlsList.put(control.getControlMapKey(), control_group);

                        f.valueProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> obs, Number oldval, Number newval) {
                                if (f.isFocused()) {
                                    if (oldval != newval) {
                                        control.setValue(newval.floatValue());
                                        //localDevice.sendDynamicControl(control);
                                    }
                                }
                                f.setDisable(control.getDisabled());
                            }
                        });

                        control_group.listener = new DynamicControl.DynamicControlListener() {
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

                        control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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
                    t.setDisable(control.getDisabled());
                    t.setText((String) control.getValue());
                    dynamicControlGridPane.add(t, 1, control_row);
                    control_group = new ControlCellGroup(control_label, t);
                    dynamicControlsList.put(control.getControlMapKey(), control_group);
                    t.setOnKeyTyped(new EventHandler<KeyEvent>() {
                        @Override
                        public void handle(KeyEvent event) {
                            if (event.getCode().equals(KeyCode.ENTER)) {
                                String text_val = t.getText();
                                control.setValue(text_val);
                                ///localDevice.sendDynamicControl(control);
                            }
                        }
                    });

                    // set handlers
                    t.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            String text_val = t.getText();
                            control.setValue(text_val);
                            //localDevice.sendDynamicControl(control);
                        }
                    });

                    control_group.listener = new DynamicControl.DynamicControlListener() {
                        @Override
                        public void update(DynamicControl control) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    t.setText((String) control.getValue());
                                    t.setDisable(control.getDisabled());
                                }
                            });
                        }
                    };

                    control_group.scopeChangedListener = new DynamicControl.ControlScopeChangedListener() {
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

            if (control_group.listener != null) {
                control.addControlListener(control_group.listener);
            }
            if (control_group.scopeChangedListener != null){
                control.addControlScopeListener(control_group.scopeChangedListener);
            }


            show();
        }

    }
    /**
     * Add A dynamic Control to window. Will run later in main thread
     * @param control The DynamicControl to add
     */
    public void addDynamicControl(DynamicControl control)
    {

        int control_row = nextControlRow;
        nextControlRow++;

        Platform.runLater(new Runnable() {
            public void run() {
                addDynamicControl(control, control_row); }
        });
    }


    public void show(){
        dynamicControlStage.show();
        dynamicControlStage.toFront();
    }

    /**
     * Set the next and previous log page button states
     */
    private void setLogPageButtons(){
        previousLogPageButton.setDisable(currentLogPage < 1 || localDevice.numberLogPages() < 2);
        nextLogPageButton.setDisable(currentLogPage >= localDevice.numberLogPages() -1 || localDevice.numberLogPages() < 2);
        int page_display_num = currentLogPage + 1;
        logPageNumber.setText("Page " + page_display_num + " of " + localDevice.numberLogPages());
    }
    /**
     * Add a debug pane
     * @return
     */
    private Node makeDebugPane() {
        TextArea log_output_text_area = new TextArea();
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

        /*
        localDevice.addLoggingStateListener(new LocalDeviceRepresentation.ConnectedUpdateListener() {
            @Override
            public void update(boolean logging_enabled) {
                enable_button.setText(logging_enabled ? stop_text : start_text);
                enable_button.setTooltip(logging_enabled ? stop_tooltip : start_tooltip);

            }
        });

*/
        previousLogPageButton.setTooltip(new Tooltip("Displays the previous page of the log"));
        previousLogPageButton.setOnMouseClicked(event ->
                {
                    int previous_page_num = currentLogPage - 1;
                    if (previous_page_num >= 0) {
                        String log = localDevice.getDeviceLog(previous_page_num);
                        currentLogPage = previous_page_num;
                        log_output_text_area.setText(log);
                    }
                    setLogPageButtons();
                }

        );


        nextLogPageButton.setTooltip(new Tooltip("Displays the next page of the log"));
        nextLogPageButton.setOnMouseClicked(event ->
                {
                    int next_page_num = currentLogPage + 1;
                    if (next_page_num < localDevice.numberLogPages()) {
                        String log = localDevice.getDeviceLog(next_page_num);
                        currentLogPage = next_page_num;
                        log_output_text_area.setText(log);
                    }
                    setLogPageButtons();
                }

        );

        log_output_text_area.setMinHeight(MIN_TEXT_AREA_HEIGHT);

        localDevice.addLogListener(deviceLogListener = (message, page) -> {
            Platform.runLater(()-> {
                try {
                    // let us see if the text is too big for us
                    if (currentLogPage == page) {
                        log_output_text_area.appendText(message);
                    } else {
                        // we have moved pages
                        String log = localDevice.getDeviceLog(page);
                        currentLogPage = page;
                        log_output_text_area.setText(log);
                    }
                    setLogPageButtons();


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        });


        // add a button to rebuild controls list
        Button rebuild_button = new Button("Rebuild Contols");
        rebuild_button.setTooltip(new Tooltip("Clear the controls displayed and request controls from the device"));

        rebuild_button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                eraseDynamicControls();
                localDevice.sendControlsRequest();

            }
        });

        logPageNumber.setDisable(true);
        HBox hBox  = new HBox(DEFAULT_ELEMENT_SPACING);
        hBox.getChildren().addAll(enable_button, previousLogPageButton, logPageNumber, nextLogPageButton);
        VBox pane = new VBox(DEFAULT_ELEMENT_SPACING);
        pane.getChildren().addAll(hBox, log_output_text_area, rebuild_button);
        setLogPageButtons();
        return pane;
    }

}
