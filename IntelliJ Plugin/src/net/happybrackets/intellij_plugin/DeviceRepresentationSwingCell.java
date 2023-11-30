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
import com.intellij.util.ui.JBFont;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

class DeviceRepresentationSwingCell extends JPanel {
    public final LocalDeviceRepresentation localDeviceRepresentation;
    private boolean selected = false;
    final Color defaultBackgroundColor;
    final Color selectedColor;
    final DeviceCellDelegate delegate;
    JSlider slider;
    JLabel statusIcon;
    final int sliderGainMultiplier = 1000;

    final String RED_IMAGE_NAME = "/icons/red.png";
    final String RED_STAR_NAME = "/icons/redstar.png";
    final String GREEN_IMAGE_NAME = "/icons/green.png";
    final String GREEN_STAR_NAME = "/icons/greenstar.png";
    Image disconnectedImage;
    Image disconnectedFavouriteImage;
    Image connectedImage;
    Image connectedFavouriteImage;

    interface DeviceCellDelegate {
        void onCellClicked(DeviceRepresentationSwingCell cell);
    }

    DeviceRepresentationSwingCell(LocalDeviceRepresentation localDeviceRepresentation, DeviceCellDelegate delegate) {
        try {
            disconnectedImage = ImageIO.read(getClass().getResourceAsStream(RED_IMAGE_NAME));
            disconnectedFavouriteImage = ImageIO.read(getClass().getResourceAsStream(RED_STAR_NAME));
            connectedImage = ImageIO.read(getClass().getResourceAsStream(GREEN_IMAGE_NAME));
            connectedFavouriteImage = ImageIO.read(getClass().getResourceAsStream(GREEN_STAR_NAME));
        }catch (Exception e) {

        }

        this.delegate = delegate;
        this.localDeviceRepresentation = localDeviceRepresentation;
        defaultBackgroundColor = this.getBackground();
        selectedColor = defaultBackgroundColor.darker();

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setAlignmentY(TOP_ALIGNMENT);
        this.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 70));

        JPanel topPanel = SwingUtilities.createContainer(BoxLayout.LINE_AXIS);
        topPanel.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 20));

        statusIcon = new JLabel();
        updateStatusIcon();
        topPanel.add(statusIcon);
        topPanel.add(Box.createHorizontalStrut(5));

        JLabel deviceLabel = SwingUtilities.createFixedWidthLabel(localDeviceRepresentation.deviceName, 100);
        deviceLabel.setFont(JBFont.create(deviceLabel.getFont()).asBold());
        topPanel.add(deviceLabel);
        topPanel.setOpaque(false);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(Box.createHorizontalStrut(10));

        topPanel.add(new JLabel("Status: Status unknown"));
        this.add(topPanel);

        this.add(Box.createVerticalStrut(3));

        JPanel bottomPanel = SwingUtilities.createContainer(BoxLayout.LINE_AXIS);
        bottomPanel.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 32));
        bottomPanel.setOpaque(false);

        JButton resetButton = SwingUtilities.createSmallButton("Reset");
        resetButton.addActionListener((ActionEvent e) ->
                localDeviceRepresentation.resetDevice()
        );
        bottomPanel.add(resetButton);

        JButton pingButton = SwingUtilities.createSmallButton("Ping");
        pingButton.addActionListener((ActionEvent e) ->
                localDeviceRepresentation.send(OSCVocabulary.Device.BLEEP)
        );
        bottomPanel.add(pingButton);

        JButton sendButton = SwingUtilities.createSmallButton("Send");
        sendButton.addActionListener((ActionEvent e) ->
                    sendCompositionToDevice()
        );
        bottomPanel.add(sendButton);

        slider = createGainSlider();
        bottomPanel.add(slider);

        // TODO: Implement favouriting
        // TODO: Implement status updatin

        this.add(bottomPanel);

        this.add(new JSeparator());

        this.setBorder(new EmptyBorder(5, 10, 0, 10));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                delegate.onCellClicked(DeviceRepresentationSwingCell.this);
            }
        });
    }

    void updateStatusIcon() {
        Image image;
        if (localDeviceRepresentation.isFavouriteDevice()) {
            image = localDeviceRepresentation.getIsConnected() ? connectedFavouriteImage : disconnectedFavouriteImage;
        } else {
            image = localDeviceRepresentation.getIsConnected() ? connectedImage : disconnectedImage;
        }
        statusIcon.setIcon(new ImageIcon(image));
    }

    void dispose() {
//        localDeviceRepresentation.removeStatusUpdateListener(gupdateListener);
//        localDeviceRepresentation.removeDeviceIdUpdateListener(deviceIdUpdateListener);
//        localDeviceRepresentation.removeConfigUpdateListener(configListener);
//        localDeviceRepresentation.removeConnectedUpdateListener(connectedUpdateListener);
//        localDeviceRepresentation.removeFriendlyNameUpdateListener(friendlyNameListener);
        localDeviceRepresentation.removeGainCHangedListener(gainChangedListener);
    }

    void sendCompositionToDevice() {
        // TODO: Consider moving this method to LocalDeviceRepresentation.
        try {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {

                    DataContext dataContext = DataManager.getInstance().getDataContext();

                    Project project = DataKeys.PROJECT.getData(dataContext);
                    Document current_doc = FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument();
                    VirtualFile current_java_file = FileDocumentManager.getInstance().getFile(current_doc);

                    VirtualFile class_file = SendCompositionAction.getClassFileFromJava(project, current_java_file);

                    if (class_file != null) {
                        String full_class_name = SendCompositionAction.getFullClassName(class_file.getCanonicalPath());
                        List<LocalDeviceRepresentation> selected = new ArrayList<>();
                        selected.add(localDeviceRepresentation);
                        try {
                            SendToDevice.send(full_class_name, selected);
                            displayNotification("Sent " + class_file.getNameWithoutExtension() + " to " + localDeviceRepresentation.getFriendlyName(), NotificationType.INFORMATION);
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
    }

    JSlider createGainSlider() {
        // JSlider only supports integers, so we use a multiplier to allow for continuous adjustment.
        JSlider slider = new JSlider(JSlider.HORIZONTAL,
                0, 2 * sliderGainMultiplier, sliderGainMultiplier);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                localDeviceRepresentation.send(OSCVocabulary.Device.GAIN, ((float) slider.getValue()) / sliderGainMultiplier - 0.05, 50f);
            }
        });

        localDeviceRepresentation.addGainChangedListener(gainChangedListener);
        return slider;
    }

    LocalDeviceRepresentation.GainChangedListener gainChangedListener = new LocalDeviceRepresentation.GainChangedListener() {
        @Override
        public void gainChanged(float newGain) {
            slider.setValue((int)(newGain * sliderGainMultiplier));
        }
    };

    void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;

        this.setBackground(selected ? selectedColor : defaultBackgroundColor);
    }

    boolean getSelected() {
        return selected;
    }
}
