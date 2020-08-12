package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import net.happybrackets.device.config.DeviceConfig;

import java.io.IOException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Renderer {

    private List<Light> lights = new ArrayList<Light>();
    private List<Speaker> speakers = new ArrayList<Speaker>();
    private final Serial serial = SerialFactory.createInstance();
    public Map<String, Device> positions = new HashMap<>();
    private boolean isSerialEnabled = false;

    public Renderer() {
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

        positions.put("hb-b827ebaac945", new Light(9,6, "Light-1", 1));
        positions.put("hb-b827eb302afa", new Speaker(10.5f,8, "Speaker-Left", 1));

        enableDevices();

        // create and register the serial data listener
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
    }

    public void enableDevices() {
        Iterator it = positions.entrySet().iterator();
        InetAddress currentIPAddress;

        try {
            currentIPAddress = InetAddress.getLocalHost(); //getLocalHost() method returns the Local Hostname and IP Address
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                if(currentIPAddress.getHostName().equals(pair.getKey())) {
                    addDevice((Device)pair.getValue());
                }
                it.remove(); // avoids a ConcurrentModificationException
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
        for (Light l : lights) {
            RenderLight(l);
        }
    }

    public void DisplayColor(Light light, int stripSize, int red, int green, int blue) {
        DisplayColor(light.id, stripSize, red, green, blue);
    }

    public void DisplayColor(int whichLED, int stripSize, int red, int green, int blue) {
        int ledAddress = 10;
        switch (whichLED) {
            case 1: ledAddress = 10;
                    break;
            case 2: ledAddress = 14;
                    break;
            case 3: ledAddress = 18;
                    break;
            default: ledAddress = 10;
                    break;
        }
        try {
            serial.write("["+ ledAddress + "]@[" + String.format("%02x",stripSize) + "] sn[" + String.format("%02x",red) + "]sn[" + String.format("%02x",green) + "]sn[" + String.format("%02x",blue) + "]sG");
        }
        catch(IOException ex){
            ex.printStackTrace();
            System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
        }
    }

    public static abstract class Device {
        public float x;
        public float y;
        public String name;
        public int id;


        public Device() {
        }

        public Device(float x, float y, String name, int id) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.id = id;
        }
    }

    public static class Speaker extends Device {
        public Speaker(float x, float y, String name, int id) {
            super(x, y, name, id);
        }
    }

    public static class Light extends Device {
        public Light(float x, float y, String name, int id) {
            super(x, y, name, id);
        }
    }
}
