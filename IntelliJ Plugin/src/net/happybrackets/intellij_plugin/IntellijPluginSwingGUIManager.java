package net.happybrackets.intellij_plugin;

import com.intellij.ide.DataManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;
import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class IntellijPluginSwingGUIManager {
    static int FULL_WIDTH = 2000;
    DeviceConnection deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
    CommandManager commandManager = new CommandManager(deviceConnection);
    JTextField commandTextField;
    JButton sendCommandToSelectedDevicesButton = new JButton("Selected");

    JComponent getRootComponent() {
        JPanel panel = SwingUtilities.createContainer(BoxLayout.PAGE_AXIS);

        panel.add(createCustomCommandArea());
        panel.add(createGlobalButtonsArea());
        panel.add(createDevicesArea());
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    JComponent createCustomCommandArea() {
        JPanel list = SwingUtilities.createContainer(BoxLayout.PAGE_AXIS);

        list.add(createCommandTextArea());
        list.add(Box.createVerticalStrut(5));
        list.add(createCustomCommandButtons());

        return createTopLevelContainer("Send custom command", list);
    }

    JComponent createCommandTextArea() {
        commandTextField = new JTextField();
        commandTextField.setMargin(new Insets(2,2,2,2));
        commandTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                commandManager.processKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        commandManager.setUpdateCommandDelegate(new CommandManager.UpdateCommandDelegate() {
            @Override
            public void updateCommand(String command) {
                commandTextField.setText(command);
            }
        });
        return commandTextField;
    }

    JComponent createCustomCommandButtons() {
        JComponent panel = createHorizontalButtonPanel();

        JButton allButton = new JButton("All");
        allButton.addActionListener((ActionEvent e) ->
                commandManager.sendCommand(commandTextField.getText(), commandManager.ALL)
        );
        allButton.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(allButton);

        sendCommandToSelectedDevicesButton.addActionListener((ActionEvent e) ->
                commandManager.sendCommand(commandTextField.getText(), commandManager.SELECTED)
        );
        sendCommandToSelectedDevicesButton.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(sendCommandToSelectedDevicesButton);
        panel.setAlignmentX(LEFT_ALIGNMENT);

        return panel;
    }

    JComponent createGlobalButtonsArea() {
        // TODO: Check whether the probe_button from the previous JavaFX version of this class was actually necessary. It seemed to be dead code.

        JComponent panel = createHorizontalButtonPanel();

        JButton resetAllButton = new JButton("Reset all");
        resetAllButton.addActionListener((ActionEvent e) ->
                ControllerEngine.getInstance().getDeviceConnection().deviceReset()
        );
        panel.add(resetAllButton);

        JButton pingAllButton = new JButton("Ping all");
        pingAllButton.addActionListener((ActionEvent e) ->
            ControllerEngine.getInstance().getDeviceConnection().synchonisedPingAll(500)
        );
        panel.add(pingAllButton);

        JButton sendAllButton = new JButton("Send all");
        sendAllButton.addActionListener((ActionEvent e) -> {
            try {
                //Project project = projects[i];
                ApplicationManager.getApplication().invokeLater(() -> {
                    try {
                        DataContext dataContext = DataManager.getInstance().getDataContext();

                        Project project = DataKeys.PROJECT.getData(dataContext);
                        Document current_doc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
                        VirtualFile current_java_file = FileDocumentManager.getInstance().getFile(current_doc);

                        VirtualFile class_file = SendCompositionAction.getClassFileFromJava(project, current_java_file);

                        if (class_file != null) {
                            String full_class_name = SendCompositionAction.getFullClassName(class_file.getCanonicalPath());

                            try {
                                SendToDevice.send(full_class_name, ControllerEngine.getInstance().getDeviceConnection().getDevices());
                                displayNotification("Sent " + class_file.getNameWithoutExtension() + " to all devices", NotificationType.INFORMATION);
                            } catch (Exception exception) {
                                displayNotification(exception.getMessage(), NotificationType.ERROR);
                                displayNotification(class_file.getName() + " may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);
                            }
                        } else {
                            displayNotification("Unable to find class. The class may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);
                        }
                    } catch (Exception ex2) {

                    }
                });
            } catch (Exception ex) {
                displayNotification("Unable to find class. The class may not have finished compiling or you may have an error in your code.", NotificationType.ERROR);

            }
        });
        panel.add(sendAllButton);

        return createTopLevelContainer("Probe", panel);
    }

    JComponent createHorizontalButtonPanel() {
        JPanel panel = SwingUtilities.createContainer(BoxLayout.LINE_AXIS);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setPreferredSize(new Dimension(FULL_WIDTH, 32));
        return panel;
    }

    class DevicesListComponent extends JComponent implements DeviceRepresentationSwingCell.DeviceCellDelegate {
        private DeviceConnection deviceConnection;

        boolean selected = false;

        java.util.List<DeviceRepresentationSwingCell> cells = new ArrayList();

        DevicesListComponent(DeviceConnection deviceConnection) {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.deviceConnection = deviceConnection;

            // TODO: Listen to the list of devices somehow here.

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    selected = !selected;
                }
            });
        }

        void initialize() {
            updateDevices();
        }

        private void updateDevices() {
            this.removeAll();
            for (DeviceRepresentationSwingCell cell : cells) {
                cell.dispose();
            }
            cells.clear();
            java.util.List<LocalDeviceRepresentation> devices = deviceConnection.getDevicesList();

            for (LocalDeviceRepresentation device : devices) {
                DeviceRepresentationSwingCell cell = new DeviceRepresentationSwingCell(device, this);
                add(cell);
                cells.add(cell);
            }

            onDeviceSelectionChanged();
        }

        public void onCellClicked(DeviceRepresentationSwingCell clickedCell) {
            if (clickedCell.getSelected()) {
                clickedCell.setSelected(false);
                return;
            }

            for (DeviceRepresentationSwingCell cell : cells) {
                cell.setSelected(cell == clickedCell);
            }
            onDeviceSelectionChanged();
        }

        public void onDeviceSelectionChanged() {
            commandManager.setSelectedLocalDeviceRepresentations(getSelectedDevices());
        }

        public java.util.List<LocalDeviceRepresentation> getSelectedDevices() {
            ArrayList<LocalDeviceRepresentation> selectedDevices = new ArrayList();
            for (DeviceRepresentationSwingCell cell : cells) {
                if (cell.getSelected()) {
                    selectedDevices.add(cell.localDeviceRepresentation);
                }
            }
            return selectedDevices;
        }

//        private DeviceRepresentationSwingCell getCellForDevice(LocalDeviceRepresentation localDeviceRepresentation) {
//            for (DeviceRepresentationSwingCell cell : cells) {
//                if(cell.localDeviceRepresentation == localDeviceRepresentation) {
//                    return cell;
//                }
//            }
//        }
    }

    JComponent createDevicesArea() {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.PAGE_AXIS));

        DevicesListComponent listComponent = new DevicesListComponent(deviceConnection);
        listComponent.initialize();

        return createTopLevelContainer("Devices", SwingUtilities.createVerticallyScrollingArea(listComponent));
    }

    JPanel createTopLevelContainer(String name, JComponent content) {
        BorderLayout layout = new BorderLayout();
        layout.setVgap(10);

        JPanel panel = new JPanel(layout);

        // For some reason this seems to shrink to the content.
        panel.setMaximumSize(new Dimension(FULL_WIDTH, 0));

        // Label at the top
        JLabel label = new JLabel(name);
        panel.add(label, BorderLayout.NORTH);

        panel.add(content, BorderLayout.CENTER);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(new JSeparator(), BorderLayout.SOUTH);

        return panel;
    }
}
