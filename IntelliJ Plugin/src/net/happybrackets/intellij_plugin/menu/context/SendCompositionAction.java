package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ArrayListSet;
import javafx.collections.ObservableList;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public abstract class SendCompositionAction extends AnAction {
    static public final String JAVA_EXTENSION = "java";
    static public final String CLASS_EXTENSION = "class";

    static final String OUTPUT_PATH = "/build/production/";
    private DeviceConnection deviceConnection = null;

    private void initConnection(){
        if (deviceConnection == null)
        {
            deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
        }
    }

    /**
     * Get the devices in our list
     * @return the devices in our list
     */
    protected ObservableList<LocalDeviceRepresentation> getDevices() {
        initConnection();
        return deviceConnection.getDevices();
    }

    /**
     * Get the selected device
     * @param project the project
     * @return the selected device for that project. Can be null
     */
    protected LocalDeviceRepresentation getSelectedDevice(Project project){
        initConnection();
        return  deviceConnection.getSelectedDevice(project.getLocationHash());
    }
    /**
     * Get the selected file from this action
     * @param e the action from action event
     * @return Virtual File if it exists
     */
    VirtualFile selectedFile(AnActionEvent e)
    {
        return (VirtualFile) e.getDataContext().getData(DataKeys.VIRTUAL_FILE.getName());
    }

    /**
     * Return the claas name minus any extension
     * @param full_class_path fill path of class on disk
     * @return the full path minus extension
     */
    public static String getFullClassName(@NotNull String full_class_path){
        return full_class_path.substring(0, full_class_path.length() - (CLASS_EXTENSION.length() + 1));
    }

    /**
     * Get the class File for the defined Java Virtual file
     * @param current_project the project we are in
     * @param java_file the java file
     * @return the virtual file that represents the class file
     */
    public static VirtualFile getClassFileFromJava(Project current_project, VirtualFile java_file){
        VirtualFile ret = null;

        String project_name = current_project.getName();

        ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);

        if (java_file != null) {
            // make sure it is a java file first
            if (java_file.getExtension().equalsIgnoreCase(JAVA_EXTENSION)) {

                String filename = java_file.getCanonicalPath();

                // check our content roots to find root
                VirtualFile[] contentSourceRoots = rootManager.getContentSourceRoots();

                String source_folder = null;
                String file_path_name = null; // this is name minus leading path

                for (VirtualFile folder : contentSourceRoots) {
                    String folder_name = folder.getCanonicalPath();
                    if (filename.startsWith(folder_name)) {
                        String relative_path = filename.replace(folder_name, "");
                        //System.out.println(relative_path);
                        // get name minus extension
                        file_path_name = relative_path.substring(0, relative_path.length() - JAVA_EXTENSION.length());
                        source_folder = folder_name;
                        break;
                    }
                }

                VirtualFile[] project_roots = rootManager.getContentRoots();
                for (VirtualFile folder : project_roots) {
                    String folder_name = folder.getCanonicalPath();
                    if (source_folder.startsWith(folder_name)) {
                        String class_filename = folder_name + OUTPUT_PATH + project_name + file_path_name + CLASS_EXTENSION;
                        System.out.println(class_filename);
                        ret = LocalFileSystem.getInstance().findFileByPath(class_filename);
                        break;
                    }
                }


            }
        }


        return ret;
    }

    /**
     * Get the class file associated with this action
     * @param e ActionEvent from menu
     * @return the Full Virtual path if it exists
     */
    VirtualFile getClassFile(AnActionEvent e)
    {
        VirtualFile ret = null;
        Project current_project = e.getProject();

        VirtualFile current_file = selectedFile(e);

        return getClassFileFromJava(current_project, current_file);
        /*
        String project_name = current_project.getName();

        ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);


        if (current_file != null) {
            // make sure it is a java file first
            if (current_file.getExtension().equalsIgnoreCase(JAVA_EXTENSION)) {

                String filename = current_file.getCanonicalPath();

                // check our content roots to find root
                VirtualFile[] contentSourceRoots = rootManager.getContentSourceRoots();

                String source_folder = null;
                String file_path_name = null; // this is name minus leading path

                for (VirtualFile folder : contentSourceRoots) {
                    String folder_name = folder.getCanonicalPath();
                    if (filename.startsWith(folder_name)) {
                        String relative_path = filename.replace(folder_name, "");
                        //System.out.println(relative_path);
                        // get name minus extension
                        file_path_name = relative_path.substring(0, relative_path.length() - JAVA_EXTENSION.length());
                        source_folder = folder_name;
                        break;
                    }
                }

                VirtualFile[] project_roots = rootManager.getContentRoots();
                for (VirtualFile folder : project_roots) {
                    String folder_name = folder.getCanonicalPath();
                    if (source_folder.startsWith(folder_name)) {
                        String class_filename = folder_name + OUTPUT_PATH + project_name + file_path_name + CLASS_EXTENSION;
                        System.out.println(class_filename);
                        ret = LocalFileSystem.getInstance().findFileByPath(class_filename);
                        break;
                    }
                }


            }
        }
        return ret;*/
    }
}
