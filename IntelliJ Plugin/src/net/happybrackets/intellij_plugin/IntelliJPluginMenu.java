package net.happybrackets.intellij_plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class IntelliJPluginMenu extends AnAction {

    public IntelliJPluginMenu() {
        // Set the menu item name.
        super("HappyBrackets");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        System.out.println("IntelliJPluginMenu");
    }
}
