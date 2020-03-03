package net.happybrackets.intellij_plugin.controller.gui;

import javax.swing.*;

/**
 * Displays a dialog in a separate thread
 */
public class DialogDisplay {
    /**
     * Display a message dialog
     * @param text the text to display
     */
    static public void displayDialog(String text)
    {
        new Thread(() -> {
            try {

                JOptionPane.showMessageDialog(null,
                        text);


            } catch (Exception ex) {
            }
        }).start();
    }
}
