package net.happybrackets.intellij_plugin.menu.examples;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ExampleAction extends AnAction{

    VirtualFile virtualFile;

    /**
     * Creates a menu item where the target file is the file we want to open
     * @param target_file the file we want to open
     */
    public  ExampleAction (VirtualFile target_file){
        super();
        virtualFile = target_file;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

        Project project = anActionEvent.getProject();
        FileEditorManager.getInstance(project).openFile(virtualFile, true, true);

    }

    @Override
    public void update(AnActionEvent event) {

        event.getPresentation().setText(virtualFile.getName());
        event.getPresentation().setDescription("HappyBrackets Example Composition");
    }
}
