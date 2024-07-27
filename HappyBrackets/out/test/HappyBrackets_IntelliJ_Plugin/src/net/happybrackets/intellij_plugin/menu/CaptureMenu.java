package net.happybrackets.intellij_plugin.menu;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import net.happybrackets.core.Device;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class CaptureMenu extends AnAction {

    static void pack(final Path folder, final Path zipFilePath) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String name = folder.relativize(file).toString().replace("\\", "/");

                    zos.putNextEntry(new ZipEntry(name));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String name = folder.relativize(dir).toString().replace("\\", "/") + "/";

                    zos.putNextEntry(new ZipEntry(name));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override
    public void update(AnActionEvent e) {
        try {
            Project project = e.getProject();

            // Uncomment out when debugging on My Mac
            //zipProject(project);
        } catch (Exception ex) {
        }
    }

    void zipProject(Project project) {
        String source_path = project.getBaseDir().getCanonicalPath();
        String project_name = project.getName();

        Path target_file = Paths.get((Paths.get(source_path).getParent()).toString() + "/" + createFilename(project_name));
        ;

        displayNotification("Created " + target_file.toString(), NotificationType.INFORMATION);

        try {
            pack(Paths.get(project.getBaseDir().getCanonicalPath()), target_file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Create a filename based on Scheduler time and device name as CSV
     *
     * @return the name of a file
     */
    String createFilename(String project_name) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String device_name = Device.getDeviceName() + "-" + project_name;
        return device_name + "-" + dateFormat.format(new Date()) + ".zip";
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            Project project = e.getProject();
            zipProject(project);

        } catch (Exception ex) {
        }


    }
    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT; // Use EDT (Event Dispatch Thread)
    }
}
