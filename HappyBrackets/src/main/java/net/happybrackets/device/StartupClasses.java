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
    static final String DEFAULT_STARTUP_FILE =  "data/classes/startup.txt";



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
                    class_name_list.add(line);
                }
                fileReader.close();
            }
        }
        catch (Exception ex){}
        return class_name_list;
    }
}
