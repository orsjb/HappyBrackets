package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import org.json.JSONObject;

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

    public List<Renderer> renderers = new ArrayList<Renderer>();
    public int stripSize = 16;

    private final Serial serial = SerialFactory.createInstance();
    private boolean isSerialEnabled = false;
    private boolean hasSpeaker = false;
    private boolean hasLight = false;
    private boolean isUnity = false;
    private String serialString = "";
    private HB hb;
    private String[] stringArray = new String[256];
    private boolean hasSerial = false;
    private Class<? extends Renderer> rendererClass;
    private Clock internalClock;
    private String currentHostname = "";

    private final String targetAddress = "192.168.1.255";
    private final int oscPort =  9001;
    private OSCUDPSender oscSender;

    /**
     * Singleton Design Pattern
     */
    private static RendererController rendererController = new RendererController();
    private RendererController() {
        internalClock = new Clock(50);
        internalClock.start();
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

    public void setHB(HB hb) {
        this.hb = hb;
    }

    public void reset() {
        serialString = "";
        turnOffLEDs();
        disableSerial();
        renderers.clear();
        hb.getAudioOutput().clearInputConnections();
        hasLight = hasSpeaker = hasSerial = false;
        internalClock.clearClockTickListener();
        internalClock.start();
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
        if(!currentHostname.contains("hb-") && hostname == "Unity" && type == Renderer.Type.LIGHT) {
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
                if(type == Renderer.Type.SPEAKER) {
                    hasSpeaker = true;
                    r.out = new Gain(1, 1);
                    hb.getAudioOutput().addInput(id, r.out, 0);
                    r.setupAudio();
                }
                if(type == Renderer.Type.LIGHT) {
                    hasLight = true;
                    enableSerial();
                    r.setupLight();
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
            displayColor(light.id, light.rgb[0], light.rgb[1], light.rgb[2]);
        }
    }

    public void displayColor(Renderer light, int red, int green, int blue) {
        if(light.type == Renderer.Type.LIGHT) {
            light.rgb[0] = red;
            light.rgb[1] = green;
            light.rgb[2] = blue;
            displayColor(light.id, red, green, blue);
        }
    }

    public void displayColor(int whichLED, int red, int green, int blue) {
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
            if(renderersColors.length() > 0) {
                OSCMessage message = HB.createOSCMessage("/colors", renderersColors.toString());
                oscSender.send(message, targetAddress, oscPort);
            }
        }
    }

    public void turnOffLEDs() {
        serialString = "";
        for (int i = 0; i < 4; i++) {
            displayColor(i, 0, 0, 0);
        }
        sendSerialcommand();
    }

}
