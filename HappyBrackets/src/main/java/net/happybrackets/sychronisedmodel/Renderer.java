package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.device.HB;
import net.happybrackets.device.config.DeviceConfig;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;


public class Renderer {

    private List<Light> lights = new ArrayList<Light>();
    public List<Speaker> speakers = new ArrayList<Speaker>();
    private final Serial serial = SerialFactory.createInstance();
    public ArrayList<Device> structure = new ArrayList<>();
    private boolean isSerialEnabled = false;
    public int LEDStripSize = 18;
    private String serialString;

    public Renderer(HB hb) {
        /*
        positions.put("hb-b827ebe529a6", new float[] {9,10});
        positions.put("hb-b827ebe6f198", new float[] {11,4.5f});
        positions.put("hb-b827ebc11b4a", new float[] {7.5f,5});
        positions.put("hb-b827ebc165c5", new float[] {4,5.5f});
        positions.put("hb-b827eb15dc82", new float[] {7,3});
        positions.put("hb-b827eb1a0c8d", new float[] {11,6.5f});
        positions.put("hb-b827eb01d68a", new float[] {1,2});
        positions.put("hb-b827eb24587c", new float[] {2.5f,4});
        positions.put("hb-b827ebc7d478", new float[] {10.5f,3});
        positions.put("hb-b827eb118819", new float[] {10.5f,3});
        positions.put("hb-b827eb24fc91", new float[] {2,6});
        positions.put("hb-b827ebb507fd", new float[] {1,4});
        positions.put("hb-b827eb824c81", new float[] {11,11});
        positions.put("hb-b827eb4635ff", new float[] {8.5f,3});
        positions.put("hb-b827eb6cb1f8", new float[] {10,5});
        positions.put("hb-b827eb9089ee", new float[] {8,8});
        positions.put("hb-b827ebf55d4d", new float[] {8,10});
        positions.put("hb-b827eb3d046a", new float[] {9,4});
        positions.put("hb-b827eb945f3f", new float[] {9,9.5f});
        positions.put("hb-b827ebbf17a8", new float[] {11,4.5f});
        positions.put("hb-b827ebeccfab", new float[] {8.5f,6});
        positions.put("hb-b827eb7561a0", new float[] {9,6});
        positions.put("hb-b827eb8221a3", new float[] {10.5f,8});
        */

        structure.add(new Light("hb-b827ebaac945",9,6, 0,"Light-1", 0));
        structure.add(new Speaker(hb, "hb-b827eb302afa",10.5f,8, 0,"Speaker-Left", 0));

        structure.add(new Speaker(hb, "hb-b827eb999a03",10.5f,8, 0,"Speaker-Left", 0));
        structure.add(new Speaker(hb, "hb-b827eb999a03",10.5f,8, 0,"Speaker-Right", 1));
        structure.add(new Light("hb-b827eb999a03",10.5f,8, 0,"Light-1", 0));
        structure.add(new Light("hb-b827eb999a03",10.5f,8, 0,"Light-2", 1));
        structure.add(new Light("hb-b827eb999a03",10.5f,8, 0,"Light-3", 2));
        structure.add(new Light("hb-b827eb999a03",10.5f,8, 0,"Light-4", 3));

        enableDevices();

        // create and register the serial data listener
        /*
        serial.addListener(new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                // NOTE! - It is extremely important to read the data received from the
                // serial port.  If it does not get read from the receive buffer, the
                // buffer will continue to grow and consume memory.
                try {
                    //System.out.println("[HEX DATA]   " + event.getHexByteString());
                    String empty = "[ASCII DATA] " + event.getAsciiString();
                } catch (IOException e) {
                    System.out.println(" ==>> SERIAL READ FAILED : " + e.getMessage());
                }
            }
        });
        */
        setup();
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
        }
        catch(IOException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            return;
        }

        try {
            serial.write("[04]@[03]s");
        }
        catch(IOException ex){
            System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
            return;
        }
        isSerialEnabled = true;
    }

    public void disableSerial() {
        if(!isSerialEnabled  || lights.size() == 0) return;
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
            if(!speakers.contains(d))
                speakers.add((Speaker) d);
        }
        if(d instanceof Light) {
            if(!lights.contains(d))
                lights.add((Light)d);
            enableSerial();
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

    public void DisplayColor(Light light, int stripSize, int red, int green, int blue) {
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
        //try {
            serialString += "["+ String.format("%02x",ledAddress) + "]@[" + String.format("%02x",stripSize) + "] sn[" + String.format("%02x",red) + "]sn[" + String.format("%02x",green) + "]sn[" + String.format("%02x",blue) + "]s";
            //serial.write("["+ String.format("%02x",ledAddress) + "]@[" + String.format("%02x",stripSize) + "] sn[" + String.format("%02x",red) + "]sn[" + String.format("%02x",green) + "]sn[" + String.format("%02x",blue) + "]s");
            //serial.discardInput();
        //}
        /*
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
        }
        */
    }

    private void sendGcommand() {
        try {
            serial.write(serialString + "G");
            serial.discardInput();
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
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
        public Speaker(HB hb, String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
            out = new Gain(1, 1);
            hb.getAudioOutput().addInput(id, out, 0);
        }
    }

    public static class Light extends Device {
        public Light(String hostname, float x, float y, float z, String name, int id) {
            super(hostname, x, y, z, name, id);
        }
    }
}
