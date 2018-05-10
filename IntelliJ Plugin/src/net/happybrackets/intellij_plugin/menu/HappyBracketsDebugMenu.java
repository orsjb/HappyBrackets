package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.intellij_plugin.menu.examples.HappyBracketsExamplesFolderMenu;

import java.util.Hashtable;
import java.util.Map;


public class HappyBracketsDebugMenu extends DefaultActionGroup {

    static final String EXAMPLES_FOLDER = "examples";

    // The project that is currently active
    static String activeProjectHash = "";


    // create a Map of Example folder menu items so we can display the appropriate one based on project
    static private Map<String, HappyBracketsExamplesFolderMenu> examplesFolderMenuHashtable = new Hashtable<String, HappyBracketsExamplesFolderMenu>();;

    /**
     * Load the examples menu if a folder called Examples is in the source
     * First tests that we are loading a different project, otherwise leaves the menu the same
     * Adds it to our happy brackets menu if required
     * If there is already a menu item there, it will remove old one
     * Also sets the Active Project
     * @param activating_project the project that is being activated at the time
     */
    public synchronized static void loadExamplesMenu(Project activating_project){

        try {
            String new_project_hash = activating_project.getLocationHash();

            if (!new_project_hash.equals(activeProjectHash)) {

                ActionManager am = ActionManager.getInstance();
                DefaultActionGroup happy_brackets_menu = (DefaultActionGroup) am.getAction("HappyBracketsIntellijPlugin.MainMenu");

                if (!activeProjectHash.isEmpty()) {
                    // first remove old menu item
                    HappyBracketsExamplesFolderMenu existing_menu = examplesFolderMenuHashtable.get(activeProjectHash);

                    if (existing_menu != null) {
                        happy_brackets_menu.remove(existing_menu);
                    }

                }


                // see if we have already made one that we are going to put in
                HappyBracketsExamplesFolderMenu new_menu =  examplesFolderMenuHashtable.get(new_project_hash);

                if (new_menu != null)
                {
                    // we have already made a menu. Use this one
                    happy_brackets_menu.add(new_menu);
                }
                else {
                    //We need to create a new menu

                    // scan the project root to see if we have an examples folder
                    VirtualFile[] vFiles = ProjectRootManager.getInstance(activating_project).getContentSourceRoots();

                    for (VirtualFile file : vFiles) {

                        if (file.isDirectory()) {
                            for (VirtualFile root_child : file.getChildren()) {
                                if (root_child.getUrl().endsWith(EXAMPLES_FOLDER)) {
                                    if (root_child.isDirectory()) {
                                        // We are in examples folder.
                                        HappyBracketsExamplesFolderMenu new_folder_menu = new HappyBracketsExamplesFolderMenu(root_child);
                                        new_folder_menu.setPopup(true);
                                        happy_brackets_menu.add(new_folder_menu);
                                        examplesFolderMenuHashtable.put(new_project_hash, new_folder_menu);
                                    }
                                }
                            }

                        }

                    }
                }
                // Let us set the example project to this one
                activeProjectHash = new_project_hash;

            }
            } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent e) {
        try {

            Project current_project = e.getProject();
            loadExamplesMenu(current_project);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
