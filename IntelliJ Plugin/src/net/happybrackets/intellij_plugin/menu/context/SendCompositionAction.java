package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ArrayListSet;
import javafx.collections.ObservableList;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.menu.examples.ExampleAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class used for sending classes, files and folders to device from controller
 * We need to detrermine whether we are sending pre-compiled classes, files or folders
 * and decide where we need to actually send them
 */
public abstract class SendCompositionAction extends AnAction {

    final static Logger logger = LoggerFactory.getLogger(SendCompositionAction.class);

    /**
     * Define what type of send action we are required to do based on context
     */
    enum SendDataType{
        SEND_DISABLE,
        SEND_CLASS,
        SEND_FILE,
        SEND_FOLDER
    };

    static public final String JAVA_EXTENSION = "java";
    static public final String CLASS_EXTENSION = "class";

    static final String OUTPUT_PATH = "/build/production/";
    static final String PROJECT_DEVICE_PATH = "/Device/HappyBrackets/";

    private DeviceConnection deviceConnection = null;

    private void initConnection(){
        if (deviceConnection == null)
        {
            deviceConnection = ControllerEngine.getInstance().getDeviceConnection();
        }
    }


    /**
     * Return a full list of files below a folder
     * @param virtualFolder the parent folder
     * @return an ArrayList of VirtualFiles
     */
    ArrayList<VirtualFile> getFilesInFolder(VirtualFile virtualFolder){
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

        for (VirtualFile folder : folders)
        {
            ArrayList<VirtualFile> children = getFilesInFolder(folder);
            for (VirtualFile child :
                    children) {
                files.add(child);
            }
        }

        return files;
    }
    /**
     * Determine the type of send action we need to do based on the selected file
     * @param selected_file the selected file or folder
     * @return the send type we are required to do
     */
    SendDataType getSendDataType(VirtualFile selected_file, AnActionEvent e){

        SendDataType ret = SendDataType.SEND_DISABLE;
        String file_ext = selected_file.getExtension();

        if (selected_file.isDirectory()){
            // we will determine whether directory is in our device tree

            ret = SendDataType.SEND_FOLDER;
        }
        else if (file_ext == null){
            ret = SendDataType.SEND_FILE;
        }
        else if (selected_file.getExtension().equalsIgnoreCase(JAVA_EXTENSION)) {
            String fileName = selected_file.getNameWithoutExtension();
            if (getClassFile(e) != null)
            {
                ret = SendDataType.SEND_CLASS;
            }
        }

        else
        {
            // Need to determine whether under device tree
            ret = SendDataType.SEND_FILE;
        }
        return ret;
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
     * Test if the selected file is under the Device/HappyBrackets folder
     * @param current_project the current project
     * @param virtualFile the file or folder we are testing
     * @return
     */
    public static boolean fileInDeviceFolder(Project current_project, VirtualFile virtualFile){
        boolean ret = false;

        String filename = virtualFile.getCanonicalPath();
        String project_path =  current_project.getBasePath();
        String device_path =  project_path + PROJECT_DEVICE_PATH;


        if (filename.startsWith(device_path)){
            ret = true;
        }
        return ret;

    }

    /**
     * Get the target folder in device
     * @param current_project the project
     * @param virtualFile the file or folder we want to send
     * @return the name of the target file or folder
     */
    public static String getTargetFolder(Project current_project, VirtualFile virtualFile){
        String filename = virtualFile.getCanonicalPath();
        String project_path =  current_project.getBasePath();
        String device_path =  project_path + PROJECT_DEVICE_PATH;
        return filename.replace(device_path, "");
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

                //VirtualFile moduleContentRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(contentEntryPath.replace('\\', '/'));

                VirtualFile[] project_roots = rootManager.getContentRoots();
                for (VirtualFile folder : project_roots) {
                    String folder_name = folder.getCanonicalPath();
                    if (source_folder.startsWith(folder_name)) {
                        String class_filename = folder_name + OUTPUT_PATH + project_name + file_path_name + CLASS_EXTENSION;
                        System.out.println(class_filename);
                        ret = LocalFileSystem.getInstance().refreshAndFindFileByPath(class_filename);
                        break;
                    }
                }


            }
        }


        return ret;
    }

    /**
     * Find the output path where our classes are written to by compiler
     * @param current_project the project
     * @return path where classes are written. Returns null if unable to find
     */
    public static String getOutputPath(Project current_project) {

        String ret = null;
        String project_name = current_project.getName();

        ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);

        // check our content roots to find root
        VirtualFile[] contentSourceRoots = rootManager.getContentSourceRoots();

        String source_folder = null;
        String file_path_name = null; // this is name minus leading path


        VirtualFile[] project_roots = rootManager.getContentRoots();
        for (VirtualFile folder : project_roots) {
            String folder_name = folder.getCanonicalPath();
                String output_path = folder_name + OUTPUT_PATH + project_name;
                ret = output_path;
                break;

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


    /**
     * Display a message dialog
     * @param text thext to display
     */
    void legacydisplayDialog(String text)
    {

        new Thread(() -> {
            try {

                JOptionPane.showMessageDialog(null,
                        text);


            } catch (Exception ex) {
            }
        }).start();
    }
}
