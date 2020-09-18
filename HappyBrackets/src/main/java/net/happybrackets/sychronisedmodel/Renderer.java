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


public class Renderer {

    private List<Light> lights = new ArrayList<Light>();
    public List<Speaker> speakers = new ArrayList<Speaker>();
    private final Serial serial = SerialFactory.createInstance();
    public ArrayList<Device> structure = new ArrayList<>();
    private boolean isSerialEnabled = false;
    public int LEDStripSize = 18;
    private String serialString;
    public SynchronisedModel model;
    private HB hb;
    String[] stringArray = new String[256];
    private boolean hasSerial = false;

    public Renderer(HB hb, SynchronisedModel model) {
        this(hb);
        this.model = model;
    }

    public Renderer(HB hb) {
        this.hb = hb;
        setup();
        initialiseArray();
    }

    public void setup() {
    }

    public void enableDevices() {
        InetAddress currentIPAddress;
        try {
            currentIPAddress = InetAddress.getLocalHost(); //getLocalHost() method returns the Local Hostname and IP Address
            for(Device d: structure) {
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

    public void addDevice(Device d) {
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

    public void RenderLight(Light light) {

    }

    public void RenderSpeaker(Speaker speaker) {

    }

    public void executeRender() {
        for (Speaker s : speakers) {
            RenderSpeaker(s);
        }
        serialString = "";
        for (Light l : lights) {
            RenderLight(l);
        }
        sendGcommand();
    }

    public void PushLightColor(Light light, int stripSize) {
        DisplayColor(light.id, stripSize, light.red, light.green, light.blue);
    }

    public void DisplayColor(Light light, int stripSize, int red, int green, int blue) {
        light.red = red;
        light.green = green;
        light.blue = blue;
        DisplayColor(light.id, stripSize, red, green, blue);
    }

    public void DisplayColor(int whichLED, int stripSize, int red, int green, int blue) {
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
            DisplayColor(i, 0, 0, 0, 0);
        }
        sendGcommand();
    }

    public static abstract class Device {
        public String hostname;
        public float x;
        public float y;
        public float z;
        public String name;
        public int id;


        public Device() {
        }

        public Device(String hostname, float x, float y, float z, String name, int id) {
            this.hostname = hostname;
            this.x = x;
            this.y = y;
            this.z = z;
            this.name = name;
            this.id = id;
        }
    }

    public static class Speaker extends Device {
        public UGen out;
        public Speaker(String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
        }
    }

    public static class Light extends Device {
        public int red,green,blue;
        double[] hsbValues;
        public Light(String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
            red = green = blue = 0;
            hsbValues = new double[3];
        }

        public void changeBrigthness(int amount) {
            rgbToHsv();
            hsbValues[2] = hsbValues[2] + amount;
            hsvToRgb();
        }

        public void changeSaturation(int amount) {
            rgbToHsv();
            hsbValues[1] = hsbValues[1] + amount;
            hsvToRgb();
        }

        public void changeHue(int amount) {
            rgbToHsv();
            hsbValues[0] = hsbValues[0] + amount;
            hsvToRgb();
        }

        public void changeRed(int amount) {
            red = red + amount;
        }

        public void changeBlue(int amount) {
            blue = blue + amount;
        }

        public void changeGreen(int amount) {
            green = green + amount;
        }

        /*
        * Based on https://stackoverflow.com/a/7898685
        * */
        public void hsvToRgb() {

            int h = (int)(hsbValues[0] * 6);
            double f = hsbValues[0] * 6 - h;
            double p = hsbValues[2] * (1 - hsbValues[1]);
            double q = hsbValues[2] * (1 - f * hsbValues[1]);
            double t = hsbValues[2] * (1 - (1 - f) * hsbValues[1]);

            switch (h) {
                case 0:
                    red = (int)hsbValues[2];
                    green = (int)t;
                    blue = (int)p;
                    break;
                case 1:
                    red = (int)q;
                    green = (int)hsbValues[2];
                    blue = (int)p;
                    break;
                case 2:
                    red = (int)p;
                    green = (int)hsbValues[2];
                    blue = (int)t;
                    break;
                case 3:
                    red = (int)p;
                    green = (int)q;
                    blue = (int)hsbValues[2];
                    break;
                case 4:
                    red = (int)t;
                    green = (int)p;
                    blue = (int)hsbValues[2];
                    break;
                case 5:
                    red = (int)hsbValues[2];
                    green = (int)p;
                    blue = (int)q;
                    break;
                default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hsbValues[0] + ", " + hsbValues[1] + ", " + hsbValues[2]);
            }
        }


        /*
        * Based on https://stackoverflow.com/questions/2399150/convert-rgb-value-to-hsv
        * */
        public void rgbToHsv(){

            double R = red / 255.0;
            double G = green / 255.0;
            double B = blue / 255.0;

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

            hsbValues[0] = H;
            hsbValues[1] = S;
            hsbValues[2] = V;
        }

    }
}
