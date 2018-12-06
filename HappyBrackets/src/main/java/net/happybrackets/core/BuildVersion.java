package net.happybrackets.core;

import java.net.URL;

/**
 * Displays build information about build
 * When Releasing a version, the values in this class need to be changed
 */
public final class BuildVersion {
    static final int MAJOR = 3;
    static final int MINOR = 4;
    static final int BUILD = 5;

    public static int getMajor(){return MAJOR;}
    public static int getMinor() {return MINOR;}
    public static int getBuild(){return BUILD;}

    // this stores the number of days between our build verison Major.Min.or.Build and this compile
    public static String BUILD_COMPILE_NUM_FILE = "builddate.txt";

    // THis is the date in seconds when the MAJOR.MINOR.BUILD numbers we last updated
    public static String BUILD_VERSION_DATE = "versionddate.txt";


    // This file contains MAJOR.MINOR.BUILD.DAYS_SINCE_BUILD, eg 3.0.0.21
    public static String VERSION_FILE = "HBVersion.txt";


    /**
     * Gets the text to display minimum compatibility between device and plugin
     * @return Minimum device compatibility
     */
    public static String getMinimumCompatibilityVersion(){
        String ret = MAJOR + "." + MINOR + ".X.X";
        return ret;

    }


    /**
     * Gets the complete version and compile version text
     * @return version and build number. eg, 3.0.0.0
     */
    public static String getVersionBuildText(){
        return getVersionText() + "." + getCompile();
    }

    /**
     * Get The version info as a string
     * @return version details as a string eg. 3.0.0
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
