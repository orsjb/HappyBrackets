package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    public List<Renderer> renderers = new ArrayList<>();
    public HashMap<String, Renderer> rendererHashMap = new HashMap<>();

    private final Serial serial = SerialFactory.createInstance();
    private boolean isSerialEnabled = false;
    private boolean hasSpeaker = false;
    private boolean hasLight = false;
    private boolean isUnity = false;
    private String serialString = "";
    private String[] stringArray = new String[256];
    private boolean hasSerial = false;
    private Class<? extends Renderer> rendererClass;
    private Clock internalClock;
    private String currentHostname = "";

    private final String targetAddress = "127.0.0.1";
    private final int oscPort =  9001;
    private OSCUDPSender oscSender;

    /**
     * Singleton Design Pattern
     */
    private static RendererController rendererController = new RendererController();
    private RendererController() {
        internalClock = new Clock(50);
        internalClock.stop();
        initialiseArray();
        try {
            currentHostname = InetAddress.getLocalHost().getHostName(); //getLocalHost() method returns the Local Hostname and IP Address
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public static RendererController getInstance( ) {
        return rendererController;
    }
    // Finish Singleton

    public void reset() {
        serialString = "";
        turnOffLEDs();
        disableSerial();
        renderers.clear();
        HB.getAudioOutput().clearInputConnections();
        hasLight = hasSpeaker = hasSerial = false;
        internalClock.clearClockTickListener();
        internalClock.stop();
        internalClock.reset();
    }

    public Clock getInternalClock(){
        return internalClock;
    }

    public void addClockTickListener(Clock.ClockTickListener listener) {
        internalClock.addClockTickListener(listener);
    }

    public void setRendererClass(Class<? extends Renderer> rendererClass) {
        this.rendererClass = rendererClass;
    }

    public void addRenderer(Renderer.Type type, String hostname, float x, float y, float z, String name, int id) {
        addRenderer(type, hostname,x,y,z,name,id,16);
    }

    public void addRenderer(Renderer.Type type, String hostname, float x, float y, float z, String name, int id, int stripSize) {
        if(!currentHostname.contains("hb-") && hostname.equals("Unity") && type == Renderer.Type.LIGHT) {
            Renderer r = null;
            try {
                r = rendererClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            r.initialize(hostname, type, x, y, z, name, id);
            renderers.add(r);
            r.setupLight();
            if(isUnity == false) {
                oscSender = new OSCUDPSender();
            }
            isUnity = true;
            return;
        }

        try {
            if(currentHostname.equals(hostname)) {
                Renderer r = rendererClass.newInstance();
                r.initialize(hostname, type, x, y, z, name, id);
                renderers.add(r);
                rendererHashMap.put(name, r);
                if(type == Renderer.Type.SPEAKER) {
                    hasSpeaker = true;
                    r.out = new Gain(1, 1);
                    HB.getAudioOutput().addInput(id, r.out, 0);
                    r.setupAudio();
                }
                if(type == Renderer.Type.LIGHT) {
                    hasLight = true;
                    enableSerial();
                    r.setupLight();
                    r.stripSize = stripSize;
                }
            }
        }
        catch (IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    private void enableSerial() {
        if(isSerialEnabled || !hasLight) return;
        try {
            // create serial config object
            SerialConfig config = new SerialConfig();
            config.device("/dev/ttyS0")
                    .baud(Baud._115200)
                    .dataBits(DataBits._8)
                    .parity(Parity.NONE)
                    .stopBits(StopBits._1)
                    .flowControl(FlowControl.NONE);

            serial.open(config);
            hasSerial = true;
        }
        catch(UnsatisfiedLinkError | IOException ex) {
            System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
            hasSerial = false;
            return;
        }

        try {
            serial.write("[04]@[03]s");
        } catch (IOException ex) {
            System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
            hasSerial = false;
            return;
        }
        isSerialEnabled = true;
    }

    public void disableSerial() {
        if(!isSerialEnabled  || !hasLight || !hasSerial) return;
        try {
            serial.close();
        }
        catch(IOException ex){
            System.out.println(" ==>> SERIAL CLOSE FAILED : " + ex.getMessage());
            return;
        }
        isSerialEnabled = false;
    }

    private void initialiseArray() {
        for (int i = 0; i < 256; i++) {
            stringArray[i] = String.format("%02x",i);
        }
    }

    public void pushLightColor(Renderer light) {
        if(light.type == Renderer.Type.LIGHT) {
            displayColor(light.id, light.rgb[0], light.rgb[1], light.rgb[2], light.stripSize);
        }
    }

    public void displayColor(Renderer light, int red, int green, int blue) {
        if(light.type == Renderer.Type.LIGHT) {
            light.rgb[0] = red;
            light.rgb[1] = green;
            light.rgb[2] = blue;
            displayColor(light.id, red, green, blue, light.stripSize);
        }
    }

    public void displayColor(int whichLED, int red, int green, int blue) {
        displayColor(whichLED,red,green,blue,16);
    }

    public void displayColor(int whichLED, int red, int green, int blue, int stripSize) {
        if(!isSerialEnabled || !hasSerial || !hasLight) {
            return;
        }
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

    public void sendSerialcommand() {
        if(isSerialEnabled && hasSerial && hasLight) {
            try {
                serial.write(serialString + "G");
                serial.discardInput();
                serialString = "";
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
            }
        }
        if(isUnity) {
            JSONObject renderersColors = new JSONObject();
            int count = 0;
            for(Renderer r: renderers) {
                if(r.type == Renderer.Type.LIGHT && r.hostname.equals("Unity")) {
                    JSONObject jo = new JSONObject();
                    jo.put("name", r.name);
                    jo.put("rgb", r.rgb[0] + "," + r.rgb[1] + "," + r.rgb[2]);

                    renderersColors.put("" + count++, jo);

                    if(renderersColors.length() > 20) {
                        OSCMessage message = HB.createOSCMessage("/colors", renderersColors.toString());
                        oscSender.send(message, targetAddress, oscPort);

                        // clear Json object
                        renderersColors = new JSONObject();
                        count = 0;
                    }
                }
            }
            if(renderersColors.length() > 0) {
                OSCMessage message = HB.createOSCMessage("/colors", renderersColors.toString());
                oscSender.send(message, targetAddress, oscPort);
            }
        }
    }

    public void turnOffLEDs() {
        serialString = "";
        for(Renderer r: renderers) {
            if(r.type == Renderer.Type.LIGHT) {
                displayColor(r, 0, 0, 0);
            }
        }
        sendSerialcommand();
    }

    public void loadHardwareConfigurationforUnity(String filepath) {
        loadHardwareConfiguration("Unity", filepath);
    }

    public void loadHardwareConfiguration(String filepath) {
        loadHardwareConfiguration("", filepath);
    }

    private void loadHardwareConfiguration(String deviceName,String filepath) {
        //List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line, deviceID;
            Renderer.Type rType;
            br.readLine(); // skip header line
            while ((line = br.readLine()) != null) {
                String[] values = line.split("," );
                if(deviceName.isEmpty()) {
                    deviceID = values[2];
                } else {
                    deviceID = deviceName;
                }
                String objectName = values[0] + "-" + values[1] + "-" + values[3];
                int objectId = Integer.parseInt(values[3].substring(values[3].length() - 1)) - 1 ;
                float x = Float.parseFloat(values[4]);
                float y = Float.parseFloat(values[5]);
                float z = Float.parseFloat(values[6]);
                int LEDstripSize = Integer.parseInt(values[7]);

                /* Debug
                System.out.println(Arrays.toString(values));
                System.out.println(rType.toString() + " " + deviceID + " " + objectName + " " + objectId);
                */

                if(values[3].contains("LED")) {
                    rType = Renderer.Type.LIGHT;
                    addRenderer(rType,deviceID,x,y,z,objectName,objectId,LEDstripSize);
                } else {
                    rType = Renderer.Type.SPEAKER;
                    addRenderer(rType,deviceID,x,y,z,objectName,objectId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Renderer getRendererByName(String name) {
        return rendererHashMap.get(name);
    }

}
