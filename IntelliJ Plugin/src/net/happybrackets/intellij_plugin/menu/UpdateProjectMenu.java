package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import net.happybrackets.core.BuildVersion;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.gui.DialogDisplay;
import net.happybrackets.intellij_plugin.menu.context.SendCompositionAction;
import net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject;
import net.happybrackets.intellij_plugin.templates.project.ProjectUnzip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.regex.Pattern;

import static net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject.*;

//import javax.help.TryMap;

public class UpdateProjectMenu extends AnAction {

    private static final String VERSION_FILE = "HBVersion.txt";
    private static final String HB_JAR = "HB.jar";

    public static void updateProject(Project current_project) {
        final String AUDIO_FOLDER = File.separatorChar + "data" + File.separatorChar + "audio";


        String base_path = current_project.getBaseDir().getCanonicalPath();
        String[] ARCHIVE_SKIP_FILES = HappyBracketsProject.ARCHIVE_SKIP_FILES;
        String[] HB_JAR_LOCATION = HappyBracketsProject.HB_JAR_LOCATION;
        String[] ARCHIVE_NO_UPDATE_FILES = HappyBracketsProject.NO_UPDATE_FILES;


        ProjectUnzip unzip = new ProjectUnzip();


        // do not add the files we are going to overwrite
        for (int i = 0; i < ARCHIVE_SKIP_FILES.length; i++) {
            unzip.addSkipFile(ARCHIVE_SKIP_FILES[i]);
        }

        // do not add the files we do not want to overwrite, but create if they don't exist
        for (int i = 0; i < ARCHIVE_NO_UPDATE_FILES.length; i++) {
            unzip.addSkipFileIfExists(ARCHIVE_NO_UPDATE_FILES[i]);
        }


        unzip.addSkipFile(HappyBracketsProject.MODULES_FILE);

        try {
            unzip.unzipReseourceProject(HAPPY_BRACKETS_PROJECT_ZIP, base_path);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            unzip.unzipReseourceProject(HAPPY_BRACKETS_JAVDOCS_ZIP, base_path + HB_JAVADOCS_FOLDER);
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
        try {
            Files.deleteIfExists(Paths.get(base_path + HB_LIBS_FOLDER + HB_JAR));
            Files.deleteIfExists(Paths.get(base_path + HB_LIBS_FOLDER + VERSION_FILE));
        } catch (Exception ex) {
        }

        try {
            if (Files.isDirectory(Paths.get(base_path + AUDIO_FOLDER))) {
                DialogDisplay.displayDialog("You will need to manually move your audio, scripts, classes and jars files across to " + base_path + HAPPY_BRACKETS_DEVICE_FOLDER);
            }
        } catch (Exception ex) {
        }

        // copy our simulator file

        // remove Build Files
        eraseProductionFiles(base_path);
        // we are not going top remove their old data files
        DialogDisplay.displayDialog("Updated project to " + BuildVersion.getVersionBuildText() + "  Please close your project and re-open it");

    }

    private static void eraseProductionFiles(String base_path) {
        final String path = base_path + SendCompositionAction.OUTPUT_PATH;
        // unzip our archived project
        try {

            File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {

                    System.out.println("Delete " + path);
                    Files.walk(file.toPath())
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void checkProjectVersionCompatibility(Project current_project) {
        try {
            // Read the Version Number of Version Text file
            String source_folder = current_project.getBaseDir().getCanonicalPath() + HAPPY_BRACKETS_DEVICE_FOLDER;
            String version_file = source_folder + VERSION_FILE;

            String hb_path = source_folder + HB_JAR;
            String version = new String("");

            int major_version = 0;
            int minor_version = 0;

            //DialogDisplay.displayDialog(" VERSION " + version_file);
            try {
                // compare the Jar version with plugin version.
                // Look only at Major and Minor
                version = new String(Files.readAllBytes(Paths.get(version_file)));
                String[] fields = version.split(Pattern.quote("."));

                major_version = Integer.parseInt(fields[0]);
                minor_version = Integer.parseInt(fields[1]);

            } catch (Exception ex) {
                // there is no file - that is OK
            }

            // See if we have a HB jar file
            if (Files.size(Paths.get(hb_path)) > 0) {


                if (BuildVersion.getMajor() != major_version ||
                        BuildVersion.getMinor() != minor_version) {
                    // we need to give warning and option to upgrade project
                    new Thread(() -> {
                        try {

                            String message = "This project needs to be updated to use this version of the HappyBrackets, otherwise, your compositions may not run properly. Select Update Project from HappyBrackets menu";
                            DialogDisplay.displayDialog(message);


                        } catch (Exception ex) {
                        }
                    }).start();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            Project current_project = e.getProject();

            updateProject(current_project);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent e) {
        try {
            Project current_project = e.getProject();
            if(current_project == null){
                System.out.println("current_project == null!!");
                return;
            }
            // Read the Version Number of Version Text file
            String source_folder = current_project.getBaseDir().getCanonicalPath() + HAPPY_BRACKETS_DEVICE_FOLDER;
            String version_file = source_folder + VERSION_FILE;

            String hb_path = source_folder + HB_JAR;
            String version = new String("");

            //DialogDisplay.displayDialog(" VERSION " + version_file);
            try {
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
            } else {
                e.getPresentation().setEnabled(false);
            }


        } catch (Exception ex) {
            e.getPresentation().setEnabled(false);
            ex.printStackTrace();
        }

    }
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT; // Use EDT (Event Dispatch Thread)
    }
}
