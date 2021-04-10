package net.happybrackets.rendererengine;

import net.beadsproject.beads.core.UGen;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;

import java.util.HashMap;


public class Renderer {

    public String hostname;
    public float x;
    public float y;
    public float z;
    public String name;
    public int id;
    public enum Type {
        SPEAKER,
        LIGHT
    }

    public enum ColorMode {
        RGB,
        HSB
    }

    public Type type;
    public UGen out;
    public int[] rgb;
    public int[] hsb;
    public int stripSize;
    public ColorMode colorMode = ColorMode.RGB;
    public HashMap<String, String> csvData  = new HashMap<>();
    public RendererController rc = RendererController.getInstance();
    public HB hb = HB.HBInstance;


    /**
     * Part of the Processing project - http://processing.org
     */
    /** Max value for red (or hue) set by colorMode */
    public float colorModeX; // = 255;

    /** Max value for green (or saturation) set by colorMode */
    public float colorModeY; // = 255;

    /** Max value for blue (or value) set by colorMode */
    public float colorModeZ; // = 255;


    public Renderer() {
    }

    public void setupAudio() {
    }

    public void setupLight() {
    }

    public void tick(Clock clock) {
    }

    public void displayColor(int red, int green, int blue) {
        rc.displayColor(this, red, green, blue);
    }

    public void initialize(String hostname, Type type, float x, float y, float z, String name, int id) {
        this.hostname = hostname;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.id = id;
        rgb = new int[3];
        hsb = new int[3];
        rgb[0] = rgb[1] = rgb[2] = 0;
        hsb = new int[3];
        rgbToHsb();
    }

    public void colorMode(ColorMode colorMode, float max) {
        colorMode(colorMode, max, max, max);
    }

    public void colorMode(ColorMode colorMode, float max1, float max2, float max3) {
        this.colorMode = colorMode;
        colorModeX = max1;  // still needs to be set for hsb
        colorModeY = max2;
        colorModeZ = max3;
    }

    public void setColor(float c1, float c2, float c3) {
        if(colorMode == ColorMode.RGB) {
            rgb[0] = (int)((c1 / colorModeX) * 255);
            rgb[1] = (int)((c2 / colorModeY) * 255);
            rgb[2] = (int)((c3 / colorModeZ) * 255);
        }
        if(colorMode == ColorMode.HSB) {
            hsb[0] = (int)((c1 / colorModeX) * 255);
            hsb[1] = (int)((c2 / colorModeY) * 255);
            hsb[2] = (int)((c3 / colorModeZ) * 255);
            hsbToRgb();
        }
    }

    public void changeHue(int amount) {
        rgbToHsb();
        hsb[0] = hsb[0] + (int)((amount/colorModeX)*255);
        hsbToRgb();
    }

    public void changeSaturation(int amount) {
        rgbToHsb();
        hsb[1] = hsb[1] + (int)((amount/colorModeY)*255);
        hsbToRgb();
    }

    public void changeBrigthness(int amount) {
        rgbToHsb();
        hsb[2] = hsb[2] + (int)((amount/colorModeZ)*255);
        hsbToRgb();
    }

    public void changeRed(int amount) {
        rgb[0] = rgb[0] + (int) ((amount / colorModeX) * 255);
    }

    public void changeGreen(int amount) {
        rgb[1] = rgb[1] + (int)((amount/colorModeY)*255);
    }

    public void changeBlue(int amount) {
        rgb[2] = rgb[2] + (int)((amount/colorModeZ)*255);
    }

    /*
     * Based on https://stackoverflow.com/a/7898685
     * */
    public void hsbToRgb() {

        double brightness = hsb[2] / 255.0;
        double saturation = hsb[1] / 255.0;
        double hue = hsb[0] / 255.0;

        if (saturation == 0) {
            rgb[0] = rgb[1] = rgb[2] = (int) (brightness * 255.0f + 0.5f);
        } else {
            double h = (hue - (float) Math.floor(hue)) * 6.0f;
            double f = h - (float) java.lang.Math.floor(h);
            double p = brightness * (1.0f - saturation);
            double q = brightness * (1.0f - saturation * f);
            double t = brightness * (1.0f - (saturation * (1.0f - f)));

            switch ((int) h) {
                case 0:
                    rgb[0] = (int) (brightness * 255.0f + 0.5f);
                    rgb[1] = (int) (t * 255.0f + 0.5f);
                    rgb[2] = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    rgb[0] = (int) (q * 255.0f + 0.5f);
                    rgb[1] = (int) (brightness * 255.0f + 0.5f);
                    rgb[2] = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    rgb[0] = (int) (p * 255.0f + 0.5f);
                    rgb[1] = (int) (brightness * 255.0f + 0.5f);
                    rgb[2] = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    rgb[0] = (int) (p * 255.0f + 0.5f);
                    rgb[1] = (int) (q * 255.0f + 0.5f);
                    rgb[2] = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    rgb[0] = (int) (t * 255.0f + 0.5f);
                    rgb[1] = (int) (p * 255.0f + 0.5f);
                    rgb[2] = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    rgb[0] = (int) (brightness * 255.0f + 0.5f);
                    rgb[1] = (int) (p * 255.0f + 0.5f);
                    rgb[2] = (int) (q * 255.0f + 0.5f);
                    break;
                default:
                    throw new RuntimeException("Something went wrong when converting from HSB to RGB. Input was " + hsb[0] + ", " + hsb[1] + ", " + hsb[2]);
            }
            if (rgb[0] > 255) rgb[0] = 255;
            if (rgb[1] > 255) rgb[1] = 255;
            if (rgb[2] > 255) rgb[2] = 255;
            if (rgb[0] < 0) rgb[0] = 0;
            if (rgb[1] < 0) rgb[1] = 0;
            if (rgb[2] < 0) rgb[2] = 0;
        }
    }


    /*
     * Based on https://stackoverflow.com/questions/2399150/convert-rgb-value-to-hsv
     * */
    public void rgbToHsb() {

        double R = rgb[0] / 255.0;
        double G = rgb[1] / 255.0;
        double B = rgb[2] / 255.0;

        double min = Math.min(Math.min(R, G), B);
        double max = Math.max(Math.max(R, G), B);
        double delta = max - min;

        double H = max;
        double S = max;
        double V = max;

        if (delta == 0) {
            H = 0;
            S = 0;
        } else {

            S = delta / max;

            double delR = (((max - R) / 6.0) + (delta / 2.0)) / delta;
            double delG = (((max - G) / 6.0) + (delta / 2.0)) / delta;
            double delB = (((max - B) / 6.0) + (delta / 2.0)) / delta;

            if (R == max) {
                H = delB - delG;
            } else if (G == max) {
                H = (1.0 / 3.0) + delR - delB;
            } else if (B == max) {
                H = (2.0 / 3.0) + delG - delR;
            }

            if (H < 0) H += 1;
            if (H > 1) H -= 1;
        }

        hsb[0] = (int)(H * 255);
        hsb[1] = (int)(S * 255);
        hsb[2] = (int)(V * 255);
    }


}
