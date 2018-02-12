package net.happybrackets.intellij_plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.ex.ProjectManagerEx;

public class ApplicationMonitor implements ApplicationComponent {
    public void initComponent() {
        ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();
        projectManager.addProjectManagerListener(new ProjectListener());
        //...
    }
}
