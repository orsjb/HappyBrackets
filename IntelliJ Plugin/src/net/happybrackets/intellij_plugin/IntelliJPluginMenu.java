package net.happybrackets.intellij_plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import javafx.application.Platform;

public class IntelliJPluginMenu extends AnAction {

    public IntelliJPluginMenu() {
        // Set the menu item name.
        super("HappyBrackets");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = DataKeys.PROJECT.getData(dataContext);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ConfigurationScreen settings = new  ConfigurationScreen(project);
                settings.show();

            }
        });
    }

}
