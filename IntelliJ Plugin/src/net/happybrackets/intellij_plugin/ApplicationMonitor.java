package net.happybrackets.intellij_plugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.ex.ProjectManagerEx;

/**
 * This class will detect when our project gains focus so we can load examples menu
 */
public class ApplicationMonitor implements ApplicationComponent {
    public void initComponent() {

        ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();
        projectManager.addProjectManagerListener(new ProjectListener());

    }
}
