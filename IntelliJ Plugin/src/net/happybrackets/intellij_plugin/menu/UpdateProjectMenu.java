package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.gui.DialogDisplay;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject;
import net.happybrackets.intellij_plugin.templates.project.ProjectUnzip;

import javax.help.TryMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject.*;

public class UpdateProjectMenu extends AnAction {

    private static final String VERSION_FILE = "HBVersion.txt";
    private static final String HB_JAR = "HB.jar";

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            final String AUDIO_FOLDER = File.separatorChar + "data" + File.separatorChar + "audio";

            Project current_project = e.getProject();
            String base_path = current_project.getBaseDir().getCanonicalPath();
            String [] ARCHIVE_SKIP_FILES = HappyBracketsProject.ARCHIVE_SKIP_FILES;
            String [] HB_JAR_LOCATION = HappyBracketsProject.HB_JAR_LOCATION;

            // unzip our archived project
            ProjectUnzip unzip = new ProjectUnzip();


            // do not add the files we are going to overwrite
            for (int i= 0; i < ARCHIVE_SKIP_FILES.length; i++) {
                unzip.addSkipFile(ARCHIVE_SKIP_FILES[i]);
            }

            unzip.addSkipFile(HappyBracketsProject.MODULES_FILE);

            try {
                unzip.unzipReseourceProject( HAPPY_BRACKETS_PROJECT_ZIP, base_path);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                unzip.unzipReseourceProject( HAPPY_BRACKETS_JAVDOCS_ZIP, base_path + HB_JAVADOCS_FOLDER);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Now unzip HB.jar into Device and Project
            try {
                for (int i = 0; i < HB_JAR_LOCATION.length; i++) {
                    unzip.unzipReseourceProject(HappyBracketsProject.HAPPY_BRACKETS_JAR_ZIP, base_path + HB_JAR_LOCATION[i]);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // we now need to remove old library
            try
            {
                Files.deleteIfExists(Paths.get(base_path + HB_LIBS_FOLDER + HB_JAR));
                Files.deleteIfExists(Paths.get(base_path + HB_LIBS_FOLDER + VERSION_FILE));
            }
            catch (Exception ex){}

            try{
                if (Files.isDirectory(Paths.get(base_path + AUDIO_FOLDER))){
                    DialogDisplay.displayDialog("You will need to manually move your audio, scripts, classes and jars files across to " + base_path + HAPPY_BRACKETS_DEVICE_FOLDER);
                }
            }catch (Exception ex){}

            // we are not going top remove their old data files
            DialogDisplay.displayDialog("Updated project to " + BuildVersion.getVersionBuildText());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent e) {
        try {
            Project current_project = e.getProject();

            // Read the Version Number of Version Text file
            String source_folder = current_project.getBaseDir().getCanonicalPath() + HAPPY_BRACKETS_DEVICE_FOLDER;
            String version_file = source_folder + VERSION_FILE;

            String hb_path = source_folder + HB_JAR;
            String version = new String("");

            //DialogDisplay.displayDialog(" VERSION " + version_file);
            try
            {
                version = new String(Files.readAllBytes(Paths.get(version_file)));
            } catch (Exception ex) {
                // there is no file - that is OK
            }

            // See if we have a HB jar file
            if (Files.size(Paths.get(hb_path)) > 0) {

                String current_version = BuildVersion.getVersionBuildText();

                if (current_version.equalsIgnoreCase(version)) {
                    e.getPresentation().setEnabled(false);
                } else {
                    e.getPresentation().setEnabled(true);
                }
            }else {
                e.getPresentation().setEnabled(false);
            }


        } catch (Exception ex) {
            e.getPresentation().setEnabled(false);
            ex.printStackTrace();
        }

    }
}
