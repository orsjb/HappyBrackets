package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class SendCompositionAction extends AnAction {
    /**
     * Get the selected file from this action
     * @param e the action from action event
     * @return Virtual File if it exists
     */
    VirtualFile selectedFile(AnActionEvent e)
    {
        return (VirtualFile) e.getDataContext().getData(DataKeys.VIRTUAL_FILE.getName());
    }

    VirtualFile getClassFile(AnActionEvent e)
    {
        Project current_project = e.getProject();
        //VirtualFile current_file = selectedFile(e);
        VirtualFile[] srcFiles = ProjectRootManager.getInstance(current_project).getContentSourceRoots();

        VirtualFile[] projectfiles = ProjectRootManager.getInstance(current_project).getContentRoots();

        for (VirtualFile file : projectfiles)
        {
            //System.out.println(file.getCanonicalPath());
        }

        return null;
    }
}
