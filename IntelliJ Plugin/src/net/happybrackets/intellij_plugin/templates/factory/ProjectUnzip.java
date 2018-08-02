package net.happybrackets.intellij_plugin.templates.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to unzip Project
 */
public class ProjectUnzip {

    static final String MAC_FOLDER = "__MACOSX";

    String [] EXECUTABLE_EXT = new String [] {"sh", "command"};

    /**
     * Unzip Archive zipfile directly to a folder without giving the archive name
     * @param source the resource name as a zip file
     * @param target_folder the folder where the zip file will be archived to
     * @throws IOException if unable to do extraction
     */
    public void unzipReseourceProject(String source, String target_folder)throws IOException {
        byte[] buffer = new byte[1024];
        java.io.InputStream inputStream = getClass().getResourceAsStream(source);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry = zis.getNextEntry();

        String archive_root_name =  "";

        while(zipEntry != null) {
            String fileName = zipEntry.getName();

            // we are not going to extract the MAC_FOLDER
            if (!fileName.startsWith(MAC_FOLDER)) {
                // We will not write our archive name.
                // we just want the files in this folder
                if (archive_root_name.isEmpty()){
                    archive_root_name = fileName;
                } else {

                    // we will strip the name out of our filename
                    String stripped_filename =  fileName.replace(archive_root_name, "");

                    File newFile = new File(target_folder + File.separatorChar + stripped_filename);

                    if (zipEntry.isDirectory()) {
                        newFile.mkdir();
                    } else {

                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();

                        // we need to be a bit smarter and define which files we actually want to do this with
                        newFile.setExecutable(true);
                    }



                }
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
}
