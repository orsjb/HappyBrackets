package net.happybrackets.core;

import java.net.URL;
import java.net.*;
import java.util.Date;
import java.io.File;
/**
 * Displays build information about build
 * When Releasing a version, the values in this class need to be changed
 */
public final class BuildVersion {
    static final int MAJOR = 3;
    static final int MINOR = 0;
    static final int BUILD = 0;

    public static int getMajor(){return MAJOR;}
    public static int getMinor() {return MINOR;}
    public static int getBuild(){return BUILD;}

    public static String VERSION_FILE = "/version.txt";

    /**
     * Gets the text to display minimum compatibility between device and plugin
     * @return Minimum device compatibility
     */
    public static String getMinimumCompatibilityVersion(){
        String ret = MAJOR + "." + MINOR + ".X.X";
        return ret;

    }


    /**
     * Get The full version info as a string
     * @return version details as a string
     */
    public static String getVersionText()
    {
        String ret = MAJOR + "." + MINOR + "." + BUILD + "." + getDate();
        return ret;
    }
    /**
     * Get the date that the class was actually compiled
     * @return n integer representing date - not implemented yet
     */
    public static int getDate()
    {
        int ret =  0;
        java.io.InputStream inputStream = new BuildVersion().getClass().getClassLoader().getResourceAsStream(VERSION_FILE);

        if (inputStream != null)
        {
            try {
                java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                String val = s.hasNext() ? s.next() : "";
                ret = Integer.parseInt(val);
            }
            catch (Exception ex){}
        }

        return ret;
    }


}
