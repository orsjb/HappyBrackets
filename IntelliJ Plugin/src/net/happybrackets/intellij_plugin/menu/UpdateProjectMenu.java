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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject.HAPPY_BRACKETS_PROJECT_ZIP;

public class UpdateProjectMenu extends AnAction {

    private static final String VERSION_FILE = "HBVersion.txt";
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
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

            // Now unzip HB.jar into Device and Project
            try {
                for (int i = 0; i < HB_JAR_LOCATION.length; i++) {
                    unzip.unzipReseourceProject(HappyBracketsProject.HAPPY_BRACKETS_JAR_ZIP, base_path + HB_JAR_LOCATION[i]);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            DialogDisplay.displayDialog("Updated project to " + BuildVersion.getVersionText());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent e) {
        try {

            Project current_project = e.getProject();

            // Read the Version Number of Version Text file
            String source_file = current_project.getBaseDir().getCanonicalPath() + File.separatorChar + "libs" + File.separatorChar + VERSION_FILE;

            String version = new String("");
            try
            {
                version = new String(Files.readAllBytes(Paths.get(source_file)));
            } catch (Exception ex) {
                // there is no file - that is OK
            }

            String current_version = BuildVersion.getVersionText();

            if (current_version.equalsIgnoreCase(version)) {
                e.getPresentation().setEnabled(false);
            }
            else
            {
                e.getPresentation().setEnabled(true);
            }

            //e.getPresentation().setText(menu_text);
            //e.getPresentation().setDescription(menu_descript);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
