package net.happybrackets.intellij_plugin.templates.factory;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HappyBracketsProjectFactory extends ProjectTemplatesFactory {

    public static final String[] HB_GROUP = new String[]{"HappyBrackets"};

    @NotNull
    @Override
    public String[] getGroups() {
        return HB_GROUP;
    }

    @Override
    public Icon getGroupIcon(String s) {
        return IconLoader.getIcon("/icons/hb.png");
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(final String s, final WizardContext wizardContext) {
        IdeFrame[] project_frame = WindowManagerEx.getInstanceEx().getAllProjectFrames();

        if (project_frame.length > 0) {
            JOptionPane.showMessageDialog(null, "You must close all open projects before trying to create a new HappyBrackets Project");
            //NotificationMessage.displayNotification("Close all open Projects before creating a new HappyBrackets Project", NotificationType.ERROR);
            return new ProjectTemplate[0];
        } else {

            final ProjectTemplate[] projectTemplates = new ProjectTemplate[1];
            projectTemplates[0] = new HappyBracketsProject();
            return projectTemplates;
        }

    }
}
