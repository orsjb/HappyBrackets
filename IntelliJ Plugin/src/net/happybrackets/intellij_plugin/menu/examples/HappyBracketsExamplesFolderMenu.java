package net.happybrackets.intellij_plugin.menu.examples;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;

public class HappyBracketsExamplesFolderMenu extends DefaultActionGroup {

    // The folder we are representing in our examples
    VirtualFile virtualFolder = null;

    // Flag to indicate whether we have done our load of subdirectories
    boolean menuLoaded = false;

    /**
     * Creates and Menu Group and populates it with files and subfolders below it
     * @param folder the Virtual folder to load
     */
    public HappyBracketsExamplesFolderMenu(VirtualFile folder){
        virtualFolder = folder;

    }


    @Override
    public void update(AnActionEvent e) {
        try {
            e.getPresentation().setText(virtualFolder.getName());
            e.getPresentation().setDescription("HappyBrackets example compositions");

            if (!menuLoaded) {
                menuLoaded = loadFolder();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Load the Subfolders and java files as menus and menu groups
     * @return
     */
    private boolean loadFolder() {

        ArrayList<VirtualFile> files = new ArrayList<>();
        ArrayList<VirtualFile> folders = new ArrayList<>();

        for (VirtualFile root_child :  virtualFolder.getChildren())
        {
            if (root_child.isDirectory())
            {
                folders.add(root_child);
            }
            else
            {
                files.add(root_child);
            }
        }

        // Now for each file, add menu item

        for (VirtualFile file: files) {
            this.add(new ExampleAction(file));
        }

        for (VirtualFile folder : folders)
        {
            HappyBracketsExamplesFolderMenu folder_group = new HappyBracketsExamplesFolderMenu(folder);
            folder_group.setPopup(true);
            // we need to set its folder so it can iterate and create it's sub menus
            //folder_group.setFolder(folder);
            this.add(folder_group);
        }
        return true;
    }
}
