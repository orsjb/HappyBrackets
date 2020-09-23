package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.device.HB;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

/**
 * TODO: remove all objects and clock when replacing the renderer.
 * How it interact with Reset?
 * Separate RendererController and Renderer class in different files.
 */
public class RendererController {

    public List<Light> lights = new ArrayList<Light>();
    public List<Speaker> speakers = new ArrayList<Speaker>();
    private final Serial serial = SerialFactory.createInstance();
    public ArrayList<Renderer> structure = new ArrayList<>();
    private boolean isSerialEnabled = false;
    public int LEDStripSize = 18;
    private String serialString;
    public SynchronisedModel model;
    private HB hb;
    String[] stringArray = new String[256];
    private boolean hasSerial = false;
    Class rendererClass;

    public RendererController(HB hb, SynchronisedModel model) {
        this(hb);
        this.model = model;
    }

    public RendererController(HB hb) {
        this.hb = hb;
        initialiseArray();
    }

    public RendererController() {
    }

    /**
     * Allows the user to set the clock interval
     */
    public void addClockTickListener() {}

    void setRenderer(Class<? extends Renderer> rendererClass) {
        this.rendererClass = rendererClass;
    }

    public void enableDevices() {
        InetAddress currentIPAddress;
        try {
            currentIPAddress = InetAddress.getLocalHost(); //getLocalHost() method returns the Local Hostname and IP Address
            for(Renderer d: structure) {
                if(currentIPAddress.getHostName().equals(d.hostname)) {
                    addDevice(d);
                }
            }
        }
        catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    public void enableSerial() {
        if(isSerialEnabled || lights.size() == 0) return;
        try {
            // create serial config object
            SerialConfig config = new SerialConfig();
            config.device("/dev/ttyS0")
                    .baud(Baud._115200)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._1)
                    .flowControl(FlowControl.NONE);

            String args[] = new String[]{};
            serial.open(config);
            hasSerial = true;
        }
        catch(IOException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            hasSerial = false;
            return;
        }

        if(hasSerial) {
            try {
                serial.write("[04]@[03]s");
            } catch (IOException ex) {
                System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
                return;
            }
            isSerialEnabled = true;
        }
    }

    public void disableSerial() {
        if(!isSerialEnabled  || lights.size() == 0 || !hasSerial) return;
        try {
            serial.close();
        }
        catch(IOException ex){
            System.out.println(" ==>> SERIAL CLOSE FAILED : " + ex.getMessage());
            return;
        }
        isSerialEnabled = false;
    }

    public void addDevice(Renderer d) {
        if(d instanceof Speaker) {
            if(!speakers.contains(d)) {
                speakers.add((Speaker) d);
                ((Speaker) d).out = new Gain(1, 1);
                hb.getAudioOutput().addInput(d.id, ((Speaker) d).out, 0);
            }
        }
        if(d instanceof Light) {
            if(!lights.contains(d))
                lights.add((Light)d);
            enableSerial();
        }
    }

    void initialiseArray() {
        for (int i = 0; i < 256; i++) {
            stringArray[i] = String.format("%02x",i);
        }
    }

    public void renderLight(Light light) {

    }

    public void renderSpeaker(Speaker speaker) {

    }

    public void executeRender() {
        for (Speaker s : speakers) {
            renderSpeaker(s);
        }
        serialString = "";
        for (Light l : lights) {
            renderLight(l);
        }
        sendGcommand();
    }

    public void pushLightColor(Light light, int stripSize) {
        displayColor(light.id, stripSize, light.rgb[0], light.rgb[1], light.rgb[2]);
    }

    public void displayColor(Light light, int stripSize, int red, int green, int blue) {
        light.rgb[0] = red;
        light.rgb[1] = green;
        light.rgb[2] = blue;
        displayColor(light.id, stripSize, red, green, blue);
    }

    public void displayColor(int whichLED, int stripSize, int red, int green, int blue) {
        int ledAddress;
        switch (whichLED) {
            case 0: ledAddress = 16;
                    break;
            case 1: ledAddress = 20;
                    break;
            case 2: ledAddress = 24;
                    break;
            case 3: ledAddress = 28;
                break;
            default: ledAddress = 16;
                    break;
        }
        if(red > 255) red = 255;
        if(green > 255) green = 255;
        if(blue > 255) red = 255;
        if(red < 0) red = 0;
        if(green < 0) green = 0;
        if(blue < 0) blue = 0;
        serialString += stringArray[ledAddress] + "@" + stringArray[stripSize] + "sn" + stringArray[red] + "sn" + stringArray[green] + "sn" + stringArray[blue] + "s";
    }

    private void sendGcommand() {
        if(isSerialEnabled && hasSerial) {
            try {
                serial.write(serialString + "G");
                serial.discardInput();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
            }
        }
    }

    public void turnOffLEDs() {
        for (int i = 0; i < 4; i++) {
            displayColor(i, 0, 0, 0, 0);
        }
        sendGcommand();
    }

    public static abstract class Renderer {
        public String hostname;
        public float x;
        public float y;
        public float z;
        public String name;
        public int id;

        public Gain out;


        public Renderer() {
        }

        public void setupAudio() {}
        public void setupLight() {}

        public Renderer(String hostname, float x, float y, float z, String name, int id) {
            this.hostname = hostname;
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
            this.id = id;
        }
    }

    public static class Speaker extends Renderer {
        public UGen out;
        public Speaker(String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
        }
    }

    public static class Light extends Renderer {
        protected int[] rgb;
        protected double[] hsb;
        public Light(String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
            rgb[0] = rgb[1] = rgb[2] = 0;
            hsb = new double[3];
        }

        public void changeBrigthness(float amount) {
            rgbToHsv();
            hsb[2] = hsb[2] + amount;
            hsvToRgb();
        }

        public void changeSaturation(float amount) {
            rgbToHsv();
            hsb[1] = hsb[1] + amount;
            hsvToRgb();
        }

        public void changeHue(float amount) {
            rgbToHsv();
            hsb[0] = hsb[0] + amount;
            hsvToRgb();
        }

        public void changeRed(int amount) {
            rgb[0] = rgb[0] + amount;
        }

        public void changeGreen(int amount) {
            rgb[1] = rgb[1] + amount;
        }

        public void changeBlue(int amount) {
            rgb[2] = rgb[2] + amount;
        }

        /*
        * Based on https://stackoverflow.com/a/7898685
        * */
        protected void hsvToRgb() {

            double brightness = hsb[2];
            double saturation = hsb[1];
            double hue = hsb[0];

            if (saturation == 0) {
                rgb[0] = rgb[1] = rgb[2] = (int) (brightness * 255.0f + 0.5f);
            } else {
                double h = (hue - (float) Math.floor(hue)) * 6.0f;
                double f = h - (float) java.lang.Math.floor(h);
                double p = brightness * (1.0f - saturation);
                double q = brightness * (1.0f - saturation * f);
                double t = brightness * (1.0f - (saturation * (1.0f - f)));

                switch ((int)h) {
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
                        throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hsb[0] + ", " + hsb[1] + ", " + hsb[2]);
                }
                if(rgb[0] > 255) rgb[0] = 255;
                if(rgb[1] > 255) rgb[1] = 255;
                if(rgb[2] > 255) rgb[2] = 255;
                if(rgb[0] < 0) rgb[0] = 0;
                if(rgb[1] < 0) rgb[1] = 0;
                if(rgb[2] < 0) rgb[2] = 0;
            }
        }


        /*
        * Based on https://stackoverflow.com/questions/2399150/convert-rgb-value-to-hsv
        * */
        public void rgbToHsv(){

            double R = rgb[0] / 255.0;
            double G = rgb[1] / 255.0;
            double B = rgb[2] / 255.0;

            double min = Math.min(Math.min(R, G), B);
            double max = Math.max(Math.max(R, G), B);
            double delta = max - min;

            double H = max;
            double S = max;
            double V = max;

            if(delta == 0){
                H = 0;
                S = 0;
            }else{

                S = delta / max;

                double delR = ( ( ( max - R ) / 6.0 ) + ( delta / 2.0 ) ) / delta;
                double delG = ( ( ( max - G ) / 6.0 ) + ( delta / 2.0 ) ) / delta;
                double delB = ( ( ( max - B ) / 6.0 ) + ( delta / 2.0 ) ) / delta;

                if(R == max){
                    H = delB - delG;
                }else if(G == max){
                    H = (1.0/3.0) + delR - delB;
                }else if(B == max){
                    H = (2.0/3.0) + delG - delR;
                }

                if(H < 0) H += 1;
                if(H > 1) H -= 1;
            }

            hsb[0] = H;
            hsb[1] = S;
            hsb[2] = V;
        }

    }
}
