package net.happybrackets.intellij_plugin;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;

import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;

public class SwingUtilities {
    static JComponent createVerticallyScrollingArea(JComponent contents) {
        JScrollPane listScroller = new JBScrollPane(contents);
        listScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setAlignmentX(LEFT_ALIGNMENT);
        return listScroller;
    }

    static JPanel createContainer(int axis) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, axis));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setAlignmentY(TOP_ALIGNMENT);
        return panel;
    }

    static JLabel createFixedWidthLabel(String text, int width) {
        JLabel label = new JLabel(text);
        label.setMaximumSize(new Dimension(width, label.getMaximumSize().height));
        return label;
    }

    static JButton createSmallButton(String title) {
        JButton button = new JButton(title);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setMaximumSize(new Dimension(25, button.getMaximumSize().height));
        return button;
    }
}
