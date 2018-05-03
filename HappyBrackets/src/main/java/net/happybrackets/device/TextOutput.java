package net.happybrackets.device;

import net.happybrackets.core.BuildVersion;
import org.apache.commons.lang3.StringUtils;

public class TextOutput {
    final static int BANNER_WIDTH = 60;
    final static int BORDER_WIDTH = 3;
    final static String BORDER_CHAR = "*";
    final static String BLANK_CHAR = " ";

    final static String [] progressChars = new String[]{"|", "/", "-", "\\"};
    /**
     * Get a line of text for a banner with the text in the centrem with a border and padded
     * @param print_text The text we want displayed
     * @param padding_char The char to pad the print_text with
     * @return the print text centred between the border and padded so string is banner_width
     */
    static String getBannerLine (String print_text,  String padding_char)
    {
        int str_len = print_text.length();
        // our text needs to fill this space
        int required_length = BANNER_WIDTH - BORDER_WIDTH * 2;
        int spaces_required = required_length - str_len;

        int leading_spaces = spaces_required / 2;
        // do this calulation to account for an odd amount of padding
        int trailing_spaces = spaces_required - leading_spaces;


        String ret = StringUtils.repeat(BORDER_CHAR, BORDER_WIDTH) +
                StringUtils.repeat(padding_char, leading_spaces) + print_text + StringUtils.repeat(padding_char, trailing_spaces)
                + StringUtils.repeat(BORDER_CHAR, BORDER_WIDTH);

        return ret;
    }

    public static void printBanner()
    {
        final int TOP_BORDER = 3;

        for (int i = 0; i < TOP_BORDER; i++)
        {
            System.out.println(getBannerLine(BORDER_CHAR,  BORDER_CHAR));
        }

        String version_text = "HappyBrackets Version " + BuildVersion.getMajor()+ "." + BuildVersion.getMinor() + "."
                + BuildVersion.getBuild() + "." + BuildVersion.getDate();
        System.out.println(getBannerLine(version_text, BLANK_CHAR));

        String beads_version =  "Beads Version " +  net.beadsproject.beads.core.BuildVersion.getVersionText();
        System.out.println(getBannerLine(beads_version, BLANK_CHAR));



        for (int i = 0; i < TOP_BORDER; i++)
        {
            System.out.println(getBannerLine(BORDER_CHAR, BORDER_CHAR));
        }
    }

    /**
     * Get a character that will make out put look like spinning wheel
     * @param count  The count we are displaying. This will use a modulo to get correct character
     * @return the string to display as though rotating
     */
    public static String getProgressChar(int count)
    {

        int index = count % progressChars.length;

        return progressChars[index];

    }
}
