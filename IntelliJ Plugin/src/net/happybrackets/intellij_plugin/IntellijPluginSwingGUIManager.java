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
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection.DeviceListChangeListener;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;
import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class IntellijPluginSwingGUIManager implements DeviceListChangeListener {

    static int FULL_WIDTH = 2000;
    DeviceConnection deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
    CommandManager commandManager = new CommandManager(deviceConnection);
    JTextField commandTextField;
    JButton sendCommandToSelectedDevicesButton = new JButton("Selected");
    DevicesListComponent devicesListComponent;
    private JLabel topLevelContainerLabel; //for updating the amount of devices in the name;
    private JPanel localListHere; //list of LocalDeviceRepresentations
    private DevicesListComponent listComponent;
    private JPanel devicePanel;

    public IntellijPluginSwingGUIManager(){
        System.out.println("NEW DEVICE LISTENER ADDED");

        deviceConnection.addDeviceListChangeListener(this);
        devicesListComponent = new DevicesListComponent(deviceConnection);
    }

    @Override
    public void onDeviceListChanged(List<LocalDeviceRepresentation> newDeviceList) {
        System.out.println("Device list changed: " + newDeviceList.size() + " devices");
        updateDeviceList(newDeviceList);
    }

    private void updateDeviceList(List<LocalDeviceRepresentation> newDeviceList) {
        if (devicesListComponent != null) {
            System.out.println("updating list!");

            devicePanel.updateUI();
            devicePanel.revalidate();
            devicePanel.repaint();

            //this did nothing
//            devicesListComponent.updateDevices(newDeviceList);
//            devicesListComponent.revalidate(); //not doing anything anywhere i tried
//            devicesListComponent.repaint();
//            devicesListComponent.updateUI();
            topLevelContainerLabel.setText( "Devices: [ " + newDeviceList.size() + " ]");
//            devicePanel.add((Component) newDeviceList);
        } else {
            System.out.println("device list null!");
        }
    }

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
                                SendToDevice.send(full_class_name, ControllerEngine.getInstance().getDeviceConnection().getDevicesList());
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

        //probe button
        JButton probeButton = new JButton("Probe");
        probeButton.addActionListener((ActionEvent e) -> {
            ControllerEngine.getInstance().doProbe();
            displayNotification("Probed Network for devices", NotificationType.INFORMATION);
            int num = deviceConnection.getDevicesList().size();
            topLevelContainerLabel.setText("Devices: " + " [ " + num + " ] ");
            updateDeviceList(deviceConnection.getDevicesList());
        });
        panel.add(probeButton);

        //TODO: REMOVE, not useful, just for testing.
        //rename button - for testing
        JButton renameButton = new JButton("rename");
        renameButton.addActionListener((ActionEvent e) -> {
        System.out.println("called ___Rename__" );
        String name = "hakushuDevice #" + Math.random() ;
        String hostname = "haku5hu!";
        String address = "127.0.0.1";
        LocalDeviceRepresentation hakushuDevice = new LocalDeviceRepresentation(name, hostname, address, 1, null, 0, true);
//            deviceConnection.getDevicesList().add();
// LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, ControllerConfig config, int reply_port, boolean isFakeDevice) {

//            deviceConnection.getDevicesList().add(hakushuDevice);
//            System.out.println("NUM!!!: " + deviceConnection.getDevicesList().size());

//            listComponent.cells.remove(0);
//
//            localListHere.getUIClassID();
//            System.out.println("WHY Y ?: " + localListHere.getY());
//            deviceConnection.getDevicesList().add(hakushuDevice);
//
//
//
//            listComponent.cells.remove(0);
//            devicesListComponent.cells.remove(0);
//            devicesListComponent.deviceConnection.theDevicesList.add(hakushuDevice);
//
//            devicesListComponent.deviceConnection.theDevicesList.remove(0);
//            devicesListComponent.cells.remove(0);
////            createDevicesArea();
//
//            System.out.println( "hakushu lives: " + hakushuDevice.deviceName);
//
//            System.out.println("update deviceConnection: " + deviceConnection.getDevicesList().size());
//            deviceConnection.getDevicesList().add(hakushuDevice);
//            deviceConnection.getDevicesList().size();
//            deviceConnection.getDevicesList().add(hakushuDevice);
//            devicesListComponent.deviceConnection.theDevicesList.add(hakushuDevice);
////            deviceConnection.createFakeTestDevices();
//            updateDeviceList(deviceConnection.getDevicesList());
//
//            System.out.println("update deviceConnection: " + deviceConnection.getDevicesList().size());
//            DeviceRepresentationSwingCell cell = new DeviceRepresentationSwingCell(hakushuDevice, devicesListComponent);
//            listComponent.cells.add(cell);
//            devicesListComponent.updateUI();
//            listComponent.updateUI();
//
//
//            //this does not update anything after adding to the list
//            listComponent.revalidate();
//            listComponent.repaint();
//
//            devicesListComponent.revalidate();
//            devicesListComponent.repaint();
//
//            localListHere.updateUI();
//            localListHere.revalidate();
//            localListHere.repaint();
//
//            topLevelContainerLabel.updateUI();
//            topLevelContainerLabel.revalidate();
//            topLevelContainerLabel.repaint();
//
//
//
//
            DeviceRepresentationSwingCell cell = new DeviceRepresentationSwingCell(hakushuDevice,devicesListComponent);
            devicesListComponent.cells.add(cell);
            listComponent.cells.add(cell);
            devicePanel.add(devicesListComponent, BorderLayout.CENTER);
            devicePanel.setBackground(Color.ORANGE);
            devicePanel.add(devicesListComponent,0);
//            devicePanel
//            devicePanel.remove(3);
//            devicePanel.getComponent(2).setBackground(JBColor.BLUE);
            /**
             *             DeviceRepresentationSwingCell cell = new DeviceRepresentationSwingCell(hakushuDevice,devicesListComponent);
             * The ticket
             * devicesListComponent.add(cell,0);
             *
             */
            devicesListComponent.remove(0);
            devicesListComponent.add(cell,0);
//    public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, ControllerConfig config, InetSocketAddress socketAddress, int reply_port) {

            synchronized (deviceConnection.devicesByHostnameLock) {
                deviceConnection.devicesByHostname.put(name, hakushuDevice);
                System.out.println("added in sync thread?");
            }

            deviceConnection.getDevicesList().add(hakushuDevice);
//    public LocalDeviceRepresentation(String deviceName, String hostname, String addr, int id, ControllerConfig config, int reply_port, boolean isFakeDevice) {
//            listComponent.cells.add(cell);

//            devicesListComponent.updateDevices(deviceConnection.getDevicesList());
            updateDeviceList(deviceConnection.getDevicesList());

//            createDevicesArea();
//            listComponent.updateUI();


//            listComponent.cells.remove(0);
//            deviceConnection.getDevicesList().remove(0);
//            listComponent.updateUI();
//                System.out.println("cells: " + listComponent.cells.size());
//                listComponent.remove(0);
//                listComponent.updateUI();
//            listComponent.add();

//            listComponent.remove(listComponent.cells.get(0));
//            System.out.println("names: " + listComponent.getParent());

//            int num = 99;
//            createDevicesArea(); //works but restarts osc server?
//            topLevelContainerLabel.setText("Devices: " + " [ " + num + " ] ");
//            topLevelContainerLabel.getParent().revalidate();
//            topLevelContainerLabel.getParent().repaint();
        });
        panel.add(renameButton);


        //this is the top level name
        return createTopLevelContainer("System Messages", panel);
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
            System.out.println("Listener here...");
            updateDevices(deviceConnection.getDevicesList());


            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    selected = !selected;
                }
            });
        }

        void initialize() {

            updateDevices(deviceConnection.getDevicesList());
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
            //updates added here--
            revalidate();
            repaint();
        }

        public void updateDevices(List<LocalDeviceRepresentation> devices) {
            this.removeAll();
            for (DeviceRepresentationSwingCell cell : cells) {
                cell.dispose();
            }
            cells.clear();


            System.out.println("updateDevicesCalled");

            for (LocalDeviceRepresentation device : devices) {
                System.out.println(device.deviceName);
                DeviceRepresentationSwingCell cell = new DeviceRepresentationSwingCell(device, this);
                add(cell);
                cells.add(cell);
                System.out.println("devices.size = " + devices.size());


//                this.remove(1);
                System.out.println();
//                if(this.getSize())
                System.out.println("cells size: " + cells.size());
            }

            this.updateUI();
//            this.remove(0);



//            onDeviceSelectionChanged();
//            revalidate();
//            repaint();
//            localListHere.revalidate();
//            localListHere.repaint();
//            updateUI();
        }

        public void onCellClicked(DeviceRepresentationSwingCell clickedCell) {
            if (clickedCell.getSelected()) {
                clickedCell.setSelected(false);
                return;
            }

            for (DeviceRepresentationSwingCell cell : cells) {
                cell.setSelected(cell == clickedCell);
//                clickedCell.setBackground(Color.ORANGE);
            }
            onDeviceSelectionChanged();
        }

        public void onDeviceSelectionChanged() {
            commandManager.setSelectedLocalDeviceRepresentations(getSelectedDevices());
//            cells.get(0).setBackground(Color.ORANGE); //this is the ticket
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
        localListHere = new JPanel();
        localListHere.setLayout(new BoxLayout(localListHere, BoxLayout.PAGE_AXIS));
        localListHere.getUIClassID();


        listComponent = new DevicesListComponent(deviceConnection);
        listComponent.initialize();


//        List<LocalDeviceRepresentation> devicesByHostname = ControllerEngine.getInstance().getDeviceConnection().getAllActiveDevices();

        int num = ControllerEngine.getInstance().getDeviceConnection().getAllActiveDevices().size();
        System.out.println("Total number of devices" + num);

        return createTopLevelContainer("Devices:" + " [ " + num + " ] ", SwingUtilities.createVerticallyScrollingArea(listComponent));
    }

    JPanel createTopLevelContainer(String name, JComponent content) {
        BorderLayout layout = new BorderLayout();
        layout.setVgap(10);

//        JPanel panel = new JPanel(layout);
        devicePanel = new JPanel(layout);

        // For some reason this seems to shrink to the content.
        devicePanel.setMaximumSize(new Dimension(FULL_WIDTH, 0));

        // Label at the top
//        JLabel label = new JLabel(name);
        topLevelContainerLabel = new JLabel(name);
        devicePanel.add(topLevelContainerLabel, BorderLayout.NORTH);

        devicePanel.add(content, BorderLayout.CENTER);

        devicePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        devicePanel.add(new JSeparator(), BorderLayout.SOUTH);

        return devicePanel;
    }
}
