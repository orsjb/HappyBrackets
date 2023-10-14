package net.happybrackets.intellij_plugin.templates.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class to unzip Project
 */
public class ProjectUnzip {

    static final String MAC_FOLDER = "__MACOSX";

    String[] EXECUTABLE_EXT = new String[]{"sh", "command"};

    List<String> skipFileList = new ArrayList<>();
    List<String> skipIfExistsFileList = new ArrayList<>();


    /**
     * Add Files that we do not want to extract from the archive
     *
     * @param filename filename to ignore
     */
    public void addSkipFile(String filename) {
        skipFileList.add(filename);
    }

    /**
     * Add Files that we do not want to overwrite from the archive
     *
     * @param filename filename to ignore
     */
    public void addSkipFileIfExists(String filename) {
        skipIfExistsFileList.add(filename);
    }

    /**
     * Unzip Archive zipfile directly to a folder without giving the archive name
     *
     * @param source        the resource name as a zip file
     * @param target_folder the folder where the zip file will be archived to
     * @throws IOException if unable to do extraction
     */
    public void unzipReseourceProject(String source, String target_folder) throws IOException {
        byte[] buffer = new byte[1024];
        java.io.InputStream inputStream = getClass().getResourceAsStream(source);
        ZipInputStream zis = new ZipInputStream(inputStream);
        ZipEntry zipEntry = zis.getNextEntry();


        while (zipEntry != null) {
            String fileName = zipEntry.getName();


            // we are not going to extract the MAC_FOLDER
            if (!fileName.startsWith(MAC_FOLDER)) {


                boolean skip_file = skipFileList.contains(fileName);
                if (!skip_file) {
                    File newFile = new File(target_folder + File.separatorChar + fileName);

                    if (zipEntry.isDirectory()) {
                        newFile.mkdir();
                    } else {

                        boolean file_exists = newFile.exists();
                        if (!file_exists || !skipIfExistsFileList.contains(fileName)) {
                            FileOutputStream fos = new FileOutputStream(newFile);
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                            fos.close();

                            // we need to be a bit smarter and define which files we actually want to do this with
                            newFile.setExecutable(true);
                        } else {
                            System.out.println("Skip " + fileName);
                        }
                    }

                } // !skip file

            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }


}
