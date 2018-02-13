package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.intellij_plugin.menu.examples.HappyBracketsExamplesFolder;
import org.jetbrains.annotations.NotNull;


public class HappyBracketsDebugMenu extends DefaultActionGroup {

    static final String EXAMPLES_FOLDER = "examples";

    static HappyBracketsExamplesFolder happyBracketsExamplesFolder = null;
    static Project examplesProject = null;

    static boolean displayingPlugin = false;

    public static boolean loadExamplesMenu(Project current_project){

        boolean ret = false;

        try {
            ActionManager am = ActionManager.getInstance();
            DefaultActionGroup happy_brackets_menu = (DefaultActionGroup) am.getAction("HappyBracketsIntellijPlugin.MainMenu");

            if (happyBracketsExamplesFolder == null) {

                String projectName = current_project.getName();
                StringBuilder sourceRootsList = new StringBuilder();
                VirtualFile[] vFiles = ProjectRootManager.getInstance(current_project).getContentSourceRoots();
                for (VirtualFile file : vFiles) {

                    if (file.isDirectory()) {
                        for (VirtualFile root_child : file.getChildren()) {
                            if (root_child.getUrl().endsWith(EXAMPLES_FOLDER)) {
                                if (root_child.isDirectory()) {

                                    // Let us set the example project to this one
                                    examplesProject = current_project;
                                    // We are in examples folder.
                                    happyBracketsExamplesFolder = new HappyBracketsExamplesFolder(root_child);
                                    happyBracketsExamplesFolder.setPopup(true);
                                    // we need to set its folder so it can iterate and create it's sub menus
                                    //happyBracketsExamplesFolder.setFolder(root_child);
                                    happy_brackets_menu.add(happyBracketsExamplesFolder);
                                    displayingPlugin = true;
                                    ret = true;
                                }
                            }
                        }

                    }

                }


            }
            else { // we have already created our menu. See if it is supposed to be displayed based on project
                ret = true;
                /*if (current_project == examplesProject) {
                    if (!displayingPlugin) {
                        happy_brackets_menu.add(happyBracketsExamplesFolder);
                        displayingPlugin = true;
                    }
                } else {
                    if (displayingPlugin) {
                        happy_brackets_menu.remove(happyBracketsExamplesFolder);
                        displayingPlugin = false;
                    }
                }*/
            }

            } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    @Override
    public void update(AnActionEvent e) {
        try {
            Project current_project = e.getProject();
            ActionManager am = ActionManager.getInstance();
            DefaultActionGroup happy_brackets_menu = (DefaultActionGroup) am.getAction("HappyBracketsIntellijPlugin.MainMenu");

            if (happyBracketsExamplesFolder == null) {

                String projectName = current_project.getName();
                StringBuilder sourceRootsList = new StringBuilder();
                VirtualFile[] vFiles = ProjectRootManager.getInstance(current_project).getContentSourceRoots();
                for (VirtualFile file : vFiles) {

                    if (file.isDirectory()) {
                        for (VirtualFile root_child : file.getChildren()) {
                            if (root_child.getUrl().endsWith(EXAMPLES_FOLDER)) {
                                if (root_child.isDirectory()) {

                                    // Let us set the example project to this one
                                    examplesProject = current_project;
                                    // We are in examples folder.
                                    happyBracketsExamplesFolder = new HappyBracketsExamplesFolder(root_child);
                                    happyBracketsExamplesFolder.setPopup(true);
                                    // we need to set its folder so it can iterate and create it's sub menus
                                    //happyBracketsExamplesFolder.setFolder(root_child);
                                    happy_brackets_menu.add(happyBracketsExamplesFolder);
                                }
                            }
                        }

                    }

                }

            } else { // we have already created our menu. See if it is supposed to be displayed based on project
                /*
                if(current_project == examplesProject){
                    if (!displayingPlugin)
                    {
                        happy_brackets_menu.add(happyBracketsExamplesFolder);
                        displayingPlugin = true;
                    }
                }
                else
                {
                    if (displayingPlugin)
                    {
                        happy_brackets_menu.remove(happyBracketsExamplesFolder);
                        displayingPlugin = false;
                    }
                }

              */
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
