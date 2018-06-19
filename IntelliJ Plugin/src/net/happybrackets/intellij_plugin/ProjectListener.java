package net.happybrackets.intellij_plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.project.VetoableProjectManagerListener;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import net.happybrackets.intellij_plugin.menu.HappyBracketsDebugMenu;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class ProjectListener implements VetoableProjectManagerListener {
    public void projectOpened(final Project project) {
        try {

            // populate menu the first time
            HappyBracketsDebugMenu.loadExamplesMenu(project);

            // change the menu to contain the opened project's menu items on focus gain
            IdeFrame project_frame = WindowManagerEx.getInstanceEx().findFrameFor(project);
            if (project_frame != null) {
                Component frame_component = project_frame.getComponent();
                SwingUtilities.windowForComponent(frame_component).addWindowFocusListener(new WindowFocusListener() {
                    @Override
                    public void windowGainedFocus(WindowEvent e) {
                        HappyBracketsDebugMenu.loadExamplesMenu(project);

                    }

                    @Override
                    public void windowLostFocus(WindowEvent e) {
                        // do nothing
                    }


                });
            }

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


    @Override
    public void projectClosed(Project project) {

    }

    @Override
    public boolean canClose(@NotNull Project project) {
        return true;
    }
}
