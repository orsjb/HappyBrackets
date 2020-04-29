package net.happybrackets.device.config;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.OSCVocabulary;
import net.happybrackets.device.HB;
import net.happybrackets.device.StartupClasses;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class for Defining config files for device
 */
public class ConfigFiles {


    public final static String CONFIG_EXT = ".config";
    public final static String CONFIG_PATH = "config" + File.separatorChar;
    public final static String COMMON_CONFIG = "common" + CONFIG_EXT;
    public final static String JARS_FOLDER =  "data/jars";

    // Also do startup and classes and Jars


    /**
     *
     * Get the name of the Config File for this device
     * @return the name of device specific config file
     */
    public static String getDeviceConfigFilename(){

        return Device.getDeviceName() + CONFIG_EXT;
    }


    /**
     * Process OSC message for controller
     * @param msg the OSC Message
     * @return An OSC message with response
     */
    public static OSCMessage processOSCConfigMessage(OSCMessage msg){

        OSCMessage ret = null;

        List<String> configs = new ArrayList<>();

        if (OSCVocabulary.match(msg, OSCVocabulary.DeviceConfig.PRINT_CONFIG)){

            File config_file;

            config_file =  new File(CONFIG_PATH + getDeviceConfigFilename());
            if (config_file.exists()){
                try {
                    String filecontents =  new String(Files.readAllBytes(Paths.get(config_file.getPath())));
                    configs.add(config_file.getPath() + "\r\n" + filecontents);

                } catch (IOException e) {
                    e.printStackTrace();
                    configs.add(config_file.getPath() + "\r\n" + "Error Reading");
                }
            }


            config_file =  new File(CONFIG_PATH + COMMON_CONFIG);

            if (config_file.exists()){
                try {
                    String filecontents =  new String(Files.readAllBytes(Paths.get(config_file.getPath())));

                    configs.add(config_file.getPath() + "\r\n" + filecontents);

                } catch (IOException e) {
                    e.printStackTrace();
                    configs.add(config_file.getPath() + "\r\n" + "Error Reading");
                }
            }


            config_file = new File(StartupClasses.getStartupFilename(Device.getDeviceName()));

            if (config_file.exists()){
                try {
                    String filecontents =  new String(Files.readAllBytes(Paths.get(config_file.getPath())));

                    configs.add(config_file.getPath() + "\r\n" + filecontents);

                } catch (IOException e) {
                    e.printStackTrace();
                    configs.add(config_file.getPath() + "\r\n" + "Error Reading");
                }
            }

            config_file = new File(StartupClasses.getDefaultStartupFilename());

            if (config_file.exists()){
                try {
                    String filecontents =  new String(Files.readAllBytes(Paths.get(config_file.getPath())));

                    configs.add(config_file.getPath() + "\r\n" + filecontents);

                } catch (IOException e) {
                    e.printStackTrace();
                    configs.add(config_file.getPath() + "\r\n" + "Error Reading");
                }
            }

            // List all Jars
            String[] pathnames;
            // Creates a new File instance by converting the given pathname string
            // into an abstract pathname
            File f = new File(JARS_FOLDER);

            // Populates the array with names of files and directories
            pathnames = f.list();
            String directory = "Jar folder\r\n";
            // For each pathname in the pathnames array
            for (String pathname : pathnames) {
                // Print the names of files and directories
                directory += pathname + "\r\n";
            }

            configs.add(directory);

            // now List classes

            String class_list = "Classes \r\n";
            try (Stream<Path> walk = Files.walk(Paths.get(StartupClasses.STARTUP_FOLDER))) {
                // We want to find only regular files
                List<String> file_list = walk.filter(Files::isRegularFile)
                        .map(x -> x.toString()).collect(Collectors.toList());

                for (String filename:
                        file_list) {
                    class_list += filename + "\r\n";

                }

                configs.add(class_list);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (configs.isEmpty()){
                ret = HB.createOSCMessage(OSCVocabulary.DeviceConfig.PRINT_CONFIG, "No config files");
            }
            else {
                ret = HB.createOSCMessage(OSCVocabulary.DeviceConfig.PRINT_CONFIG, configs.toArray());
            }
        }
        else if (OSCVocabulary.match(msg, OSCVocabulary.DeviceConfig.DELETE_CONFIG)){
            List<String> deleted_files = deleteConfigFiles();
            ret = HB.createOSCMessage(OSCVocabulary.DeviceConfig.DELETE_CONFIG, deleted_files.toArray());
        }


        return ret;

    }

    public static List<String> deleteConfigFiles(){
        List<String> file_list = new ArrayList<>();

        File config_file;

        config_file =  new File(CONFIG_PATH + getDeviceConfigFilename());
        if (config_file.exists()){
            file_list.add(config_file.getPath());
        }

        config_file =  new File(CONFIG_PATH + COMMON_CONFIG);

        if (config_file.exists()){
            file_list.add(config_file.getPath());
        }


        config_file = new File(StartupClasses.getStartupFilename(Device.getDeviceName()));

        if (config_file.exists()){
            file_list.add(config_file.getPath());
        }


        config_file = new File(StartupClasses.getDefaultStartupFilename());

        if (config_file.exists()){
            file_list.add(config_file.getPath());
        }


        // List all Jars
        String[] pathnames;
        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        File f = new File(JARS_FOLDER);

        // Populates the array with names of files and directories
        pathnames = f.list();
        // For each pathname in the pathnames array
        for (String pathname : pathnames) {
            // Print the names of files and directories
            file_list.add(JARS_FOLDER + File.separatorChar + pathname);
        }


        // now List classes
        try (Stream<Path> walk = Files.walk(Paths.get(StartupClasses.STARTUP_FOLDER))) {
            // We want to find only regular files
            List<String> classes_list = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());

            for (String filename:
                    classes_list) {
                file_list.add(filename);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Delete the files
        for (String filename:
             file_list) {
            try {
                Files.deleteIfExists(Paths.get(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file_list;
    }

}
