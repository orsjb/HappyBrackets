package net.happybrackets.core;

import java.net.URL;

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

    // this stores our compile number filename  that gets incremented from tess run in gradle
    public static String BUILD_COMPILE_NUM_FILE = "builddate.txt";

    // This file contains MAJOR.MINOR.BUILD, eg 3.0.0
    public static String VERSION_FILE = "HBVersion.txt";

    // This is the version that is a combination of VERSION_FILE and BUILD_COMPILE_NUM_FILE
    // So it will be say 3.0.0.21
    public static String PLIUGIN_VERSION_FILE = "plugin.txt";


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
     * Get the compile increment each time tests are run from gradle
     * The values is store in the JAR resource
     * When the getVersionText changes, this value becomes zero
     * The actual value gets written from tests in gradle
     * @return build number
     */
    public static int getCompile()
    {
        int ret =  0;
        java.io.InputStream inputStream = new BuildVersion().getClass().getResourceAsStream("/" + BUILD_COMPILE_NUM_FILE);

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
