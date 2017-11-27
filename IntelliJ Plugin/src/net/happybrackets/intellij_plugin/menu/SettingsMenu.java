package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import javafx.application.Platform;
import net.happybrackets.intellij_plugin.ConfigurationScreen;

public class SettingsMenu extends AnAction {

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
