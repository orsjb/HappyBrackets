package net.happybrackets.core;

import java.net.URL;

/**
 * Displays build information about build
 * When Releasing a version, the values in this class need to be changed
 */
public final class BuildVersion {
    static final int MAJOR = 3;
    static final int MINOR = 0;
    static final int BUILD = 1;

    public static int getMajor(){return MAJOR;}
    public static int getMinor() {return MINOR;}
    public static int getBuild(){return BUILD;}

    public static String BUILD_COMPILE_NUM_FILE = "/builddate.txt";

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
        String ret = MAJOR + "." + MINOR + "." + BUILD;
        return ret;
    }
    /**
     * Get the date that the class was actually compiled
     * @return n integer representing date - not implemented yet
     */
    public static int getCompile()
    {
        int ret =  0;
        java.io.InputStream inputStream = new BuildVersion().getClass().getResourceAsStream(BUILD_COMPILE_NUM_FILE);

        if (inputStream != null)
        {
            try {
                java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                String val = s.hasNext() ? s.next() : "";
                ret = Integer.parseInt(val);
            }
            catch (Exception ex){}
        }

        try {

            URL url = new BuildVersion().getClass().getResource(BUILD_COMPILE_NUM_FILE);

            if (url != null){
                java.io.InputStream inputStream2 = new BuildVersion().getClass().getResourceAsStream(BUILD_COMPILE_NUM_FILE);
                if (inputStream2 != null)
                {
                    try {
                        java.util.Scanner s = new java.util.Scanner(inputStream2).useDelimiter("\\A");
                        String val = s.hasNext() ? s.next() : "";
                        ret = Integer.parseInt(val);
                    }
                    catch (Exception ex){}
                }
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return ret;
    }


}
