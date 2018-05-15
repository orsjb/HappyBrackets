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
import net.happybrackets.controller.network.LocalDeviceRepresentation;
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

    static final int MAX_LOG_DISPLAY_CHARS = 5000;

    private static final int MIN_TEXT_AREA_HEIGHT = 200;
    private final int DEFAULT_ELEMENT_SPACING = 10;

    private boolean logFull = false;

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
        screenTitle = localDevice.deviceName;
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

    public void createContextMenus(ContextMenu contextMenu) {
        MenuItem copy_name_command_menu = new MenuItem("Copy " + localDevice.getAddress() + " to clipboard");
        copy_name_command_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(localDevice.getAddress());
                clipboard.setContent(content);
            }
        });

        MenuItem copy_ssh_command_menu = new MenuItem("Copy SSH " + localDevice.getAddress() + " to clipboard");
        copy_ssh_command_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                //content.putString(buildSSHCommand(localDevice.getAddress()));
                clipboard.setContent(content);
            }
        });


        MenuItem copy_host_command_menu = new MenuItem("Copy " + localDevice.deviceName + " to clipboard");
        copy_host_command_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                content.putString(localDevice.deviceName);
                clipboard.setContent(content);
            }
        });

        MenuItem request_status_menu = new MenuItem("Request status");
        request_status_menu.setDisable(localDevice.isIgnoringDevice());
        request_status_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                localDevice.sendStatusRequest();
            }
        });


        MenuItem request_version_menu = new MenuItem("Request Version");
        request_version_menu.setDisable(localDevice.isIgnoringDevice());
        request_version_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                localDevice.sendVersionRequest();
            }
        });


        CheckMenuItem favourite_item_menu = new CheckMenuItem("Favourite");
        favourite_item_menu.setSelected(localDevice.isFavouriteDevice());
        favourite_item_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                localDevice.setFavouriteDevice(!localDevice.isFavouriteDevice());
                Platform.runLater(() -> {
                    favourite_item_menu.setSelected(localDevice.isFavouriteDevice());
                });
            }
        });

        CheckMenuItem encrypt_item_menu = new CheckMenuItem("Encrypt Classes");
        encrypt_item_menu.setSelected(localDevice.isEncryptionEnabled());
        encrypt_item_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                localDevice.setEncryptionEnabled(!localDevice.isEncryptionEnabled());
                Platform.runLater(() -> {
                    encrypt_item_menu.setSelected(localDevice.isFavouriteDevice());
                });
            }
        });

        MenuItem reboot_menu = new MenuItem("Reboot Device");
        reboot_menu.setDisable(localDevice.isIgnoringDevice());
        reboot_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                new Thread(() -> {
                    try {

                        int dialog_button = JOptionPane.YES_NO_OPTION;
                        int dialog_result = JOptionPane.showConfirmDialog(null,
                                "Are you sure you want to reboot " + localDevice.getFriendlyName() + "?", "Rebooting " + localDevice.getFriendlyName(), dialog_button);

                        if (dialog_result == JOptionPane.YES_OPTION) {
                            localDevice.rebootDevice();
                        }
                    } catch (Exception ex) {
                    }
                }).start();


            }
        });

        MenuItem shutdown_menu = new MenuItem("Shutdown Device");
        shutdown_menu.setDisable(localDevice.isIgnoringDevice());
        shutdown_menu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                new Thread(() -> {
                    try {

                        int dialog_button = JOptionPane.YES_NO_OPTION;
                        int dialog_result = JOptionPane.showConfirmDialog(null,
                                "Are you sure you want to shutdown " + localDevice.getFriendlyName() + "?", "Shutting Down " + localDevice.getFriendlyName(), dialog_button);

                        if (dialog_result == JOptionPane.YES_OPTION) {
                            localDevice.shutdownDevice();
                        }
                    } catch (Exception ex) {
                    }
                }).start();

            }
        });

        contextMenu.getItems().addAll(copy_name_command_menu, copy_ssh_command_menu, copy_host_command_menu, request_status_menu,
                request_version_menu, favourite_item_menu, encrypt_item_menu,
                 reboot_menu, shutdown_menu);
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
        Platform.runLater(new Runnable() {
            public void run() {
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
            }
        });
    }

    public void createDynamicControlStage(){

        DynamicControlScreen this_screen = this;

        Platform.runLater(new Runnable() {
            public void run() {
                synchronized (controlCreateLock) {
                    if (dynamicControlStage == null) {


                        if (localDevice != null) {
                            debugPane = new TitledPane("Debug", makeDebugPane());

                            debugPane.setExpanded(false);
                        }

                        dynamicControlStage = new Stage();
                        dynamicControlStage.setTitle(screenTitle);
                        ;
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
                            createContextMenus(contextMenu);
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
            }
        });
    }

    /**
     * Add A dynamic Control to window. Will execute in main thread
     * @param control The DynamicControl to add
     */
    public void addDynamicControl(DynamicControl control)
    {

        int control_row = nextControlRow;
        nextControlRow++;

        Platform.runLater(new Runnable() {
            public void run() {

                ControlCellGroup control_group = dynamicControlsList.get(control.getControlMapKey());

                if (control_group == null) {

                    Label control_label = new Label(control.getControlName());

                    dynamicControlGridPane.add(control_label, 0, control_row);


                    ControlType control_type = control.getControlType();
                    switch (control_type) {
                        case TRIGGER:
                            Button b = new Button();

                            control.setTooltipPrefix("Press button to generate a trigger event for this control");
                            b.setTooltip(new Tooltip(control.getTooltipText()));
                            b.setText("Send");
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
                                //s.setMaxWidth(100);
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
                                //t.setMaxWidth(100);
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
                            //t.setMaxWidth(100);
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

        localDevice.addLoggingStateListener(new LocalDeviceRepresentation.ConnectedUpdateListener() {
            @Override
            public void update(boolean logging_enabled) {
                enable_button.setText(logging_enabled ? stop_text : start_text);
                enable_button.setTooltip(logging_enabled ? stop_tooltip : start_tooltip);

            }
        });

        Button clear_button = new Button("Clear Log");
        clear_button.setTooltip(new Tooltip("Erases the log on this screen"));
        clear_button.setOnMouseClicked(event -> log_output_text_area.clear());

        log_output_text_area.setMinHeight(MIN_TEXT_AREA_HEIGHT);

        localDevice.addLogListener(deviceLogListener = new LocalDeviceRepresentation.LogListener() {
            @Override
            public void newLogMessage(String message) {
                try {
                    // let us see if the text is too big for us

                    // how big will our new log be
                    int new_total_length = message.length() + log_output_text_area.getLength();

                    if (new_total_length > MAX_LOG_DISPLAY_CHARS) {
                        if (!logFull) {
                            log_output_text_area.appendText("Log Full - Delete some of the text to get more");
                            logFull = true;
                        }
                    }
                    else {
                        log_output_text_area.appendText(message);
                        logFull = false;
                    }
                } catch (Exception ex) {
                    String error_message = ex.getMessage();
                    System.out.println(error_message);
                }
            }
        });


        VBox pane = new VBox(DEFAULT_ELEMENT_SPACING);
        pane.getChildren().addAll(enable_button, log_output_text_area);
        return pane;
    }

}
