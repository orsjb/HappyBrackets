package net.happybrackets.intellij_plugin;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;
import java.awt.*;

public class IntellijPluginSwingGUIManager {
    JComponent getRootComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // Label at the top
        JLabel label = new JLabel("Label");
        panel.add(label);

        // Text area for editing
        JTextArea textArea = new JTextArea(13, 60);
        textArea.setText("Some text");
        panel.add(textArea);

        System.out.println("Creating root component");
        return panel;
    }
}
