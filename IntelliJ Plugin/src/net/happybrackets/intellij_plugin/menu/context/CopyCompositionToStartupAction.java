package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.org.apache.xml.internal.dtm.ref.sax2dtm.SAX2DTM;
import net.happybrackets.controller.network.SendToDevice;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class CopyCompositionToStartupAction extends SendCompositionAction {

    final static String CLASSES_FOLDER = "classes";
    final static String DATA_FOLDER = "Device/HappyBrackets/data";

    @Override
    public void update(AnActionEvent e) {
        try {
            if (e != null) {
                VirtualFile vfile = selectedFile(e);
                boolean enable = false;

                if (vfile != null) {

                    if (vfile.getExtension().equalsIgnoreCase(JAVA_EXTENSION)) {
                        String fileName = vfile.getNameWithoutExtension();
                        e.getPresentation().setText("Copy " + fileName + " to startup");
                        enable = getClassFile(e) != null;
                    }

                }

                e.getPresentation().setEnabled(enable);
            }
        }
        catch (Exception ex){
            if (e != null){
                try {
                    e.getPresentation().setEnabled(false);
                }
                catch (Exception ex2){}
            }
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = getClassFile(e);

        if (vfile != null) {

            try {
                String class_name = vfile.getNameWithoutExtension();
                File folder = new File(vfile.getCanonicalPath());

                boolean success = false;

                String parent_path = folder.getParent();
                ArrayList<String> filenames =  SendToDevice.allFilenames(parent_path, class_name);

                Project current_project = e.getProject();
                // we have a list of class names without paths. We now need to copy to our startup in the project
                ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);

                VirtualFile[] roots = rootManager.getContentRoots();


                // look fort data folder
                for (VirtualFile file : roots){

                    String root_path = file.getCanonicalPath();
                    String data_path = root_path + "/" + DATA_FOLDER;
                    File data_folder = new File(data_path);
                     if (data_folder.isDirectory()){

                         String class_path =  data_path + "/" + CLASSES_FOLDER;
                         File class_folder = new File(class_path);

                         if (!class_folder.exists()){
                             class_folder.mkdir();
                         }
                         // now see if our classes is a valid directory. If so, we can copy our files here
                         if (class_folder.isDirectory()) {
                             // First get rot of our classes output
                             String build_folder = getOutputPath(current_project);
                             // sanity check
                             if (build_folder != null) {
                                 String relative_path = parent_path.replace(build_folder, "");
                                 String absolute_class_target_path =  class_folder.getCanonicalPath() + relative_path;

                                 File target_dir = new File(absolute_class_target_path);
                                 if (!target_dir.exists()){
                                     target_dir.mkdirs();
                                 }


                                 // now copy each of the files in there
                                 if (target_dir.isDirectory()){
                                     for(String filename: filenames){
                                         File src = new File(parent_path + "/" + filename);
                                         FileUtils.copyFileToDirectory(src, target_dir);
                                         success = true;
                                         LocalFileSystem.getInstance().refreshAndFindFileByIoFile(new File(target_dir + "/" + filename));
                                     }
                                 }


                             }


                         }


                     }
                    if (success){
                        displayNotification("Copied " + class_name + " to " + DATA_FOLDER + "/" + CLASSES_FOLDER, NotificationType.INFORMATION);
                    }
                    else{
                        displayNotification("Unable to copy " + class_name + " to class startuo ", NotificationType.ERROR);
                        //displayDialog("Unable to copy " + class_name + " to class startuo ");
                    }
                }

            } catch (Exception e1) {
                displayNotification(e1.getMessage(), NotificationType.ERROR);
            }
        }
        else{
            displayNotification("Unable to find class", NotificationType.ERROR);
        }

    }


}
