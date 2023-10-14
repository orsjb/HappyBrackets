package net.happybrackets.intellij_plugin.controller.gui;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.ControlType;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.intellij_plugin.controller.gui.device.PingMenu;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

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
    private boolean isShowing = false;

    /**
     * See if the Screen has been shown before
     * @return true if it has been shown
     */
    public boolean getIsShowing(){
        return isShowing;
    }

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

        DynamicControl.DISPLAY_TYPE currentDisplayType =  DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT;
    }

    /**
     * Store the Buddy as its own control
     */
    private class BuddyControl extends VBox{

        DynamicControl.DISPLAY_TYPE lastDisplayType = DynamicControl.DISPLAY_TYPE.DISPLAY_HIDDEN;

        Object last_max = 0;
        Object last_min = 0;

        TextField textField;
        Slider slider;
        public BuddyControl(TextField text_display, Slider slider_display){
            textField = text_display;
            slider = slider_display;
        }

        /**
         * Update Display of this control
         * @param control the Dynamic Control
         */
        public void updateDisplay(DynamicControl control) {
            DynamicControl.DISPLAY_TYPE display_type = control.getDisplayType();

            boolean change = !control.getMinimumDisplayValue().equals(last_min) ||
                    !control.getMaximumDisplayValue().equals(last_max) ||
                    lastDisplayType != display_type;

            if (change) {

                if (display_type == DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY ||
                        display_type == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY) {

                    setSliderSize(control);
                    showBuddy();
                } else if (setSliderSize(control)) {
                    showSlider();
                } else {
                    showText();
                }

                lastDisplayType = display_type;
                last_min = control.getMinimumDisplayValue();
                last_max = control.getMaximumDisplayValue();
            }
        }

        /**
         * Make sure we set slider size for control
         * @param control the {@link DynamicControl}
         * @return true if we are able to set it
         */
        boolean setSliderSize(DynamicControl control){
            boolean ret = false;

            if (!control.getMinimumDisplayValue().equals(control.getMaximumDisplayValue())) {
                if (control.getControlType() == ControlType.INT) {
                    int min = (int) control.getMinimumDisplayValue();
                    int max = (int) control.getMaximumDisplayValue();

                    slider.setMin(min);
                    slider.setMax(max);
                    ret = true;
                }
                else if (control.getControlType() == ControlType.FLOAT) {
                    double min = (double) control.getMinimumDisplayValue();
                    double max = (double) control.getMaximumDisplayValue();

                    slider.setMin(min);
                    slider.setMax(max);
                    ret = true;
                }

            }
            return ret;
        }

        void showBuddy(){
            this.getChildren().clear();
            this.getChildren().add(textField);
            this.getChildren().add(slider);
        }

        void showSlider(){
            this.getChildren().clear();
            this.getChildren().add(slider);
        }

        void showText(){
            this.getChildren().clear();
            this.getChildren().add(textField);
        }

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
    public DynamicControlScreen(LocalDeviceRepresentation local_device) {
        localDevice = local_device;
        screenTitle = localDevice.getFriendlyName();
        setGridColumnAttributes();
    }

    private void setGridColumnAttributes() {
        column1.setHgrow(Priority.ALWAYS);
        column2.setHgrow(Priority.ALWAYS);

        dynamicControlGridPane.getColumnConstraints().addAll(column1, column2);
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


                    MenuItem rebuild_controls_menu = new MenuItem("Rebuild Controls");
                    rebuild_controls_menu.setOnAction(event2 -> {
                                eraseDynamicControls();

                                localDevice.sendControlsRequest();
                            }
                    );


                    contextMenu.getItems().add(rebuild_controls_menu);



                    if (localDevice != null)
                    {
                        contextMenu.getItems().add(new SeparatorMenuItem());
                        PingMenu menus = new PingMenu(localDevice, "");
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

                    addDisplayDynamicControl(control);


                    control = localDevice.popNextPendingControl();
                }
            }
        });
    }

    /**
     * Add a Trigger control to control Group
     * @param control
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addTriggerControl(DynamicControl control){
        Button button = new Button();
        Label control_label = new Label(control.getControlName());
        boolean disable = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

        control.setTooltipPrefix("Press button to generate a trigger event for this control");
        button.setTooltip(new Tooltip(control.getTooltipText()));
        button.setText("Send");
        button.setDisable(disable);

        ControlCellGroup control_group = new ControlCellGroup(control_label, button);

        button.setOnAction(e -> {
            // This is a number so we have a stop condition
            control.setValue(System.currentTimeMillis());
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(() -> button.setTooltip(new Tooltip(control.getTooltipText())));

        control_group.listener = control1 -> Platform.runLater(() -> button.setDisable(control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED));

        return control_group;
    }

    /**
     * Add a addBooleanControl control to control Group
     * @param control the DynamicControl
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addBooleanControl(DynamicControl control){
        CheckBox checkBox = new CheckBox();
        Label control_label = new Label(control.getControlName());
        boolean disable = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;


        control.setTooltipPrefix("Change the check state to generate an event for this control");
        checkBox.setTooltip(new Tooltip(control.getTooltipText()));
        boolean b_val = (boolean) control.getValue();
        checkBox.setSelected(b_val);
        checkBox.setDisable(disable);

        ControlCellGroup control_group = new ControlCellGroup(control_label, checkBox);

        checkBox.selectedProperty().addListener((ov, oldval, newval) -> {
            if (oldval != newval) {
                control.setValue(newval);
                //localDevice.sendDynamicControl(control);
            }
        });

        control_group.listener = control1 -> Platform.runLater(new Runnable() {
            public void run() {
                if (!checkBox.isFocused()) {
                    boolean b_val1 = (boolean) control1.getValue();
                    checkBox.setSelected(b_val1);
                }
                boolean disable1 = control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                        || control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

                checkBox.setDisable(disable1);
            }
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                checkBox.setTooltip(new Tooltip(control.getTooltipText()));
            }
        });

        return control_group;
    }

    /**
     * Add an Object Control control to control Group
     * @param control the DynamicControl
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addObjectControl(DynamicControl control){
        TextField textField = new TextField();
        Label control_label = new Label(control.getControlName());

        control.setTooltipPrefix("Object Message. Read only");
        textField.setTooltip(new Tooltip(control.getTooltipText()));
        textField.setDisable(true);
        textField.setText(control.getValue().toString());

        ControlCellGroup control_group = new ControlCellGroup(control_label, textField);

        control_group.listener = control1 -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setText(control1.getValue().toString());
            }
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setTooltip(new Tooltip(control.getTooltipText()));
            }
        });


        return control_group;
    }

    /**
     * Add an Text Control control to control Group
     * @param control the DynamicControl
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addTextControl(DynamicControl control){
        TextField textField = new TextField();
        Label control_label = new Label(control.getControlName());
        boolean disable = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

        control.setTooltipPrefix("Type in text and press enter to generate an event for this control");
        textField.setTooltip(new Tooltip(control.getTooltipText()));
        textField.setDisable(disable);
        textField.setText((String) control.getValue());

        ControlCellGroup control_group = new ControlCellGroup(control_label, textField);

        textField.setOnKeyTyped(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                String text_val = textField.getText();
                control.setValue(text_val);
            }
        });

        // set handlers
        textField.setOnAction(actionEvent -> {
            String text_val = textField.getText();
            control.setValue(text_val);
        });

        control_group.listener = control1 -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setText((String) control1.getValue());
                boolean disable1 = control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                        || control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;
                textField.setDisable(disable1);
            }
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setTooltip(new Tooltip(control.getTooltipText()));
            }
        });

        return control_group;
    }

    /**
     * Add a Integer control to control Group
     * @param control
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addIntegerControl(DynamicControl control) {
        ControlCellGroup control_group = null;

        int control_value = (int) control.getValue();
        boolean disable = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;


        Label control_label = new Label(control.getControlName());

        // we will put slider and buddy in VBox
        TextField textField = new TextField();
        Slider slider = new Slider((int) control.getMinimumDisplayValue(), (int) control.getMaximumDisplayValue(), (int) control.getValue());

        BuddyControl vBox = new BuddyControl(textField, slider);
        vBox.getChildren().add(textField);
        vBox.getChildren().add(slider);

        vBox.updateDisplay(control);

        control_group = new ControlCellGroup(control_label, vBox);

        control.setTooltipPrefix("Type in an integer value and press enter to generate an event for this control");

        textField.setTooltip(new Tooltip(control.getTooltipText()));

        textField.setText(Integer.toString(control_value));
        textField.setDisable(disable);


        textField.setOnKeyTyped(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                String text_val = textField.getText();
                try {
                    int control_value1 = Integer.valueOf(text_val);

                    control.setValue(control_value1);
                } catch (Exception ex) {
                    // we might want to put an exception here
                }
            }
        });

        // set handlers
        textField.setOnAction(actionEvent -> {
            String text_val = textField.getText();
            try {
                int control_value12 = Integer.valueOf(text_val);

                control.setValue(control_value12);
            } catch (Exception ex) {
                // we might want to put an exception here
            }
        });

        // We need to check if we are switching to buddy or changing display types here
        ControlCellGroup finalControl_group = control_group;
        control_group.listener = control1 -> Platform.runLater(() -> {
            boolean disable1 = control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                    || control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

            textField.setText(Integer.toString((int) control1.getValue()));
            textField.setDisable(disable1);

            vBox.updateDisplay(control1);
            if (finalControl_group.currentDisplayType != control.getDisplayType()) {
                System.out.println("Need to Change Display Type");
            }
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setTooltip(new Tooltip(control.getTooltipText()));
            }
        });

        // we need to make a copy so we can add to events
        final TextField buddyTextControl = textField;

        control.setTooltipPrefix("Change the slider value to generate an event for this control");
        slider.setTooltip(new Tooltip(control.getTooltipText()));
        slider.setDisable(disable);
        slider.setOrientation(Orientation.HORIZONTAL);

        slider.valueProperty().addListener((obs, oldval, newval) -> {
            if (slider.isFocused()) {
                if (oldval != newval) {
                    if (buddyTextControl != null) {
                        buddyTextControl.setText(Integer.toString(newval.intValue()));
                    }
                }
            }
        });

        slider.valueProperty().addListener((obs, oldval, newval) -> {
            if (slider.isFocused()) {
                if (oldval != newval) {
                    control.setValue(newval.intValue());
                    if (buddyTextControl != null) {
                        buddyTextControl.setText(Integer.toString(newval.intValue()));
                    }
                }
            }
        });

        // Need to check for Display Type change
        ControlCellGroup finalControl_group1 = control_group;
        control_group.listener = control12 -> Platform.runLater(() -> {

            if (!slider.isFocused()) {
                slider.setValue((int) control12.getValue());
                if (buddyTextControl != null) {
                    buddyTextControl.setText(Integer.toString((int) control12.getValue()));
                }
            }
            boolean disable12 = control12.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                    || control12.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

            slider.setDisable(disable12);
            if (buddyTextControl != null) {
                buddyTextControl.setDisable(disable12);
            }

            vBox.updateDisplay(control12);
            if (finalControl_group1.currentDisplayType != control.getDisplayType()) {
                System.out.println("Need to Change Display Type");
            }
        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(() -> {
            slider.setTooltip(new Tooltip(control.getTooltipText()));
            if (buddyTextControl != null) {
                buddyTextControl.setTooltip(new Tooltip(control.getTooltipText()));
            }
        });
        return control_group;
    }

    /**
     * Add an Float Control control to control Group
     * @param control the DynamicControl
     * @return A encapsulated control group with Label, Control, and buddy if required
     */
    private ControlCellGroup addFloatControl(DynamicControl control) {
        double f_control_value = (double) control.getValue();
        ControlCellGroup control_group = null;

        boolean disable = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

        boolean display_buddy = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_ENABLED_BUDDY ||
                control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

        Label control_label = new Label(control.getControlName());
        TextField textField = new TextField();
        Slider slider = new Slider((double) control.getMinimumDisplayValue(), (double) control.getMaximumDisplayValue(), (double) control.getValue());

        BuddyControl vBox = new BuddyControl(textField, slider);
        vBox.getChildren().add(textField);
        vBox.getChildren().add(slider);

        vBox.updateDisplay(control);
        control_group = new ControlCellGroup(control_label, vBox);

        control.setTooltipPrefix("Type in a float value and press enter to generate an event for this control");
        textField.setTooltip(new Tooltip(control.getTooltipText()));
        textField.setDisable(disable);
        textField.setText(Double.toString(f_control_value));

        // now set events
        textField.setOnKeyTyped(event -> {
            KeyCode keyEvent = event.getCode();
            String char_val = event.getCharacter();
            if (event.getCode().equals(KeyCode.ENTER) || char_val.equalsIgnoreCase("\r")) {
                String text_val = textField.getText();
                try {
                    double control_value = Double.valueOf(text_val);

                    control.setValue(control_value);
                } catch (Exception ex) {
                    // we might want to put an exception here
                }
            }
        });

        // set handlers
        textField.setOnAction(actionEvent -> {
            String text_val = textField.getText();
            try {
                double control_value = Double.valueOf(text_val);

                control.setValue(control_value);
                //localDevice.sendDynamicControl(control);
            } catch (Exception ex) {
                // we might want to put an exception here
            }
        });

        control_group.listener = control1 -> Platform.runLater(new Runnable() {
            public void run() {
                boolean disable1 = control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                        || control1.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;

                textField.setText(Double.toString((double) control1.getValue()));
                textField.setDisable(disable1);
                vBox.updateDisplay(control);
            }
        });


        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                textField.setTooltip(new Tooltip(control.getTooltipText()));

            }
        });

        // we have this one as a buddy. It must be declared as final so we can use in events
        final TextField buddyTextControl = textField;

        //f.setMaxWidth(100);
        control.setTooltipPrefix("Change the slider value to generate an event for this control");
        slider.setTooltip(new Tooltip(control.getTooltipText()));
        slider.setOrientation(Orientation.HORIZONTAL);
        slider.setDisable(disable);

        // we need to add a listener to update text box
        slider.valueProperty().addListener((obs, oldval, newval) -> {
            if (slider.isFocused()) {
                if (oldval != newval) {
                    if (buddyTextControl != null) {
                        buddyTextControl.setText(Double.toString(newval.doubleValue()));
                    }
                }
            }
        });

        // we need to create this final one for events blow
        slider.valueProperty().addListener((obs, oldval, newval) -> {

            if (slider.isFocused()) {
                if (oldval != newval) {
                    control.setValue(newval.doubleValue());
                    //localDevice.sendDynamicControl(control);
                }
            }
            boolean disable12 = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED
                    || control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED_BUDDY;
            slider.setDisable(disable12);
            if (buddyTextControl != null) {
                buddyTextControl.setDisable(disable12);
            }


        });

        control_group.listener = control12 -> Platform.runLater(new Runnable() {
            public void run() {

                if (!slider.isFocused()) {

                    slider.setValue((double) control12.getValue());

                    // We also need to set value of buddy if we have one
                    if (buddyTextControl != null) {
                        buddyTextControl.setText(Double.toString((double) control12.getValue()));
                    }
                }
                vBox.updateDisplay(control12);
            }

        });

        control_group.scopeChangedListener = new_scope -> Platform.runLater(new Runnable() {
            public void run() {
                slider.setTooltip(new Tooltip(control.getTooltipText()));
                // We also need to set value of buddy if we have one
                if (buddyTextControl != null) {
                    buddyTextControl.setTooltip(new Tooltip(control.getTooltipText()));
                }
            }
        });

        return control_group;
    }

    /**
     * Adds a control to the display window. Must be called in context of main thread
     * @param control the control to add
     */
    private void addDisplayDynamicControl (DynamicControl control){
        ControlCellGroup control_group = dynamicControlsList.get(control.getControlMapKey());

        if (control_group == null) {

            // first add a listeners to notify us of when this control gets removed
            ControlMap.getInstance().addDynamicControlRemovedListener(control1 -> removeDynamicControl(control1));

            ControlType control_type = control.getControlType();

            boolean hidden = control.getDisplayType() == DynamicControl.DISPLAY_TYPE.DISPLAY_HIDDEN;

            switch (control_type) {
                case TRIGGER:
                    if (hidden){
                        break;
                    }
                    control_group = addTriggerControl(control);
                    break;

                case INT:
                    if (hidden){
                        break;
                    }

                    control_group = addIntegerControl(control);
                    break;

                case BOOLEAN:
                    if (hidden){
                        break;
                    }
                    control_group = addBooleanControl(control);
                    break;

                case FLOAT:
                    if (hidden){
                        break;
                    }

                    control_group = addFloatControl(control);
                    break;

                case OBJECT:
                    control_group = addObjectControl(control);

                    break;
                case TEXT:
                    if (hidden){
                        break;
                    }
                    control_group = addTextControl(control);
                    break;

                default:
                    break;
            }

            if (control_group != null) {
                control_group.currentDisplayType = control.getDisplayType();

                dynamicControlGridPane.add(control_group.labelNode, 0, nextControlRow);
                dynamicControlGridPane.add(control_group.controlNode, 1, nextControlRow++);

                dynamicControlsList.put(control.getControlMapKey(), control_group);

                if (control_group.listener != null) {
                    control.addControlListener(control_group.listener);
                }
                if (control_group.scopeChangedListener != null) {
                    control.addControlScopeListener(control_group.scopeChangedListener);
                }
            }

            show();
        }

    }

    public void show(){
        dynamicControlStage.show();
        dynamicControlStage.toFront();
        isShowing = true;
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

        localDevice.addLogListener((message, page) -> {
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
        Button rebuild_button = new Button("Rebuild Controls");
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
