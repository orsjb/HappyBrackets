package net.happybrackets.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Read the startup classes file
 */
public class StartupClasses {

    static public final String STARTUP_FOLDER =  "data/classes/";
    static private final String DEFAULT_STARTUP_FILE =  "startup.txt";


    /**
     * Get the name of the startup file based on hostname
     * @param hostname hostname
     * @return the folder and name file
     */
    public static String getStartupFilename(String hostname){
        return STARTUP_FOLDER + hostname + "-" + DEFAULT_STARTUP_FILE;
    }

    /**
     * Get the defaule startup filename
     * @return the default startup filename including folder
     */
    public static String getDefaultStartupFilename(){
        return STARTUP_FOLDER + DEFAULT_STARTUP_FILE;
    }

    /**
     * Get the list of class names that we want to start automatically
     * @param filename The file that has a list of class names without the .class extension
     * @return a list of class names to auto start
     */
    public static List<String> getStartupClassnames(String filename){
        List<String> class_name_list = new ArrayList<>();

        try {
            File file = new File(filename);
            if (file.isFile()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while((line = bufferedReader.readLine()) != null){
                    if (!line.startsWith("#")) {
                        class_name_list.add(line);
                    }
                }
                fileReader.close();
            }
        }
        catch (Exception ex){}
        return class_name_list;
    }
}
