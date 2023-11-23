package net.happybrackets.intellij_plugin;

import com.intellij.util.ui.JBFont;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class DeviceRepresentationSwingCell extends JPanel {
    private IntellijPluginSwingGUIManager intellijPluginSwingGUIManager;
    private LocalDeviceRepresentation localDeviceRepresentation;
    private boolean selected = false;
    final Color defaultBackgroundColor;

    DeviceRepresentationSwingCell(IntellijPluginSwingGUIManager intellijPluginSwingGUIManager, LocalDeviceRepresentation localDeviceRepresentation) {
        this.intellijPluginSwingGUIManager = intellijPluginSwingGUIManager;
        defaultBackgroundColor = this.getBackground();
        this.localDeviceRepresentation = localDeviceRepresentation;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setAlignmentY(TOP_ALIGNMENT);
        this.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 70));

        JPanel topPanel = intellijPluginSwingGUIManager.createContainer(BoxLayout.LINE_AXIS);
        topPanel.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 20));

        JLabel deviceLabel = intellijPluginSwingGUIManager.createFixedWidthLabel(localDeviceRepresentation.deviceName, 100);
        deviceLabel.setFont(JBFont.create(deviceLabel.getFont()).asBold());
        topPanel.add(deviceLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("Status: Status unknown"));
        this.add(topPanel);

        this.add(Box.createVerticalStrut(3));

        JPanel bottomPanel = intellijPluginSwingGUIManager.createContainer(BoxLayout.LINE_AXIS);
        bottomPanel.setPreferredSize(new Dimension(IntellijPluginSwingGUIManager.FULL_WIDTH, 32));

        bottomPanel.add(intellijPluginSwingGUIManager.createSmallButton("Reset"));
        bottomPanel.add(intellijPluginSwingGUIManager.createSmallButton("Ping"));
        bottomPanel.add(intellijPluginSwingGUIManager.createSmallButton("Send"));

        JSlider slider = new JSlider(JSlider.HORIZONTAL,
                0, 10, 5);
        bottomPanel.add(slider);
        this.add(bottomPanel);

        this.add(new JSeparator());

        this.setBorder(new EmptyBorder(5, 10, 0, 10));

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                updateSelectionStatus(!selected);
            }
        });

        updateSelectionStatus(false);
    }

    void updateSelectionStatus(boolean selected) {
        this.selected = selected;

        this.setBackground(selected ? Color.RED : defaultBackgroundColor);
    }
}
