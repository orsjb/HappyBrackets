package net.happybrackets.sychronisedmodel;

import com.pi4j.io.serial.*;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.device.HB;

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

    public static List<Renderer> renderers = new ArrayList<Renderer>();

    private static final Serial serial = SerialFactory.createInstance();
    private static boolean isSerialEnabled = false;
    private static boolean hasSpeaker = false;
    private static boolean hasLight = false;
    private static String serialString;
    private static HB hb;
    private static String[] stringArray = new String[256];
    private static boolean hasSerial = false;
    private static Class<Renderer> rendererClass;

    private static RendererController rendererController = new RendererController();

    private RendererController() {
        initialiseArray();
    }

    public static RendererController getInstance( ) {
        return rendererController;
    }

    public static void setHB(HB hb) {
        RendererController.hb = hb;
    }

    public static void reset() {
        disableSerial();
        renderers.clear();
        hb.getAudioOutput().clearInputConnections();
        hasLight = hasSpeaker = hasSerial = false;
    }

    /**
     * Allows the user to set the clock interval
     */
    public static void addClockTickListener() {}

    @SuppressWarnings("unchecked")
    public static void setRendererClass(Class<? extends Renderer> rendererClass) {
        RendererController.rendererClass = (Class<Renderer>) rendererClass;
    }

    public static void addRenderer(Renderer.Type type, String hostname, float x, float y, float z, String name, int id) {
        InetAddress currentIPAddress;
        Constructor<Renderer> constructor = null;
        try {
            currentIPAddress = InetAddress.getLocalHost(); //getLocalHost() method returns the Local Hostname and IP Address
            if(currentIPAddress.getHostName().equals(hostname)) {
                constructor = rendererClass.getConstructor();
                Renderer r = constructor.newInstance();
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
        catch (UnknownHostException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private static void enableSerial() {
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
        catch(IOException ex) {
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

    public static void disableSerial() {
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

    private static void initialiseArray() {
        for (int i = 0; i < 256; i++) {
            stringArray[i] = String.format("%02x",i);
        }
    }

    public static void pushLightColor(Renderer light, int stripSize) {
        displayColor(light.id, stripSize, light.rgb[0], light.rgb[1], light.rgb[2]);
    }

    public static void displayColor(Renderer light, int stripSize, int red, int green, int blue) {
        light.rgb[0] = red;
        light.rgb[1] = green;
        light.rgb[2] = blue;
        displayColor(light.id, stripSize, red, green, blue);
    }

    public static void displayColor(int whichLED, int stripSize, int red, int green, int blue) {
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

    private static void sendSerialcommand() {
        if(isSerialEnabled && hasSerial && hasLight) {
            try {
                serial.write(serialString + "G");
                serial.discardInput();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println(" ==>> SERIAL COMMAND FAILED : " + ex.getMessage());
            }
        }
    }

    public static void turnOffLEDs() {
        for (int i = 0; i < 4; i++) {
            displayColor(i, 0, 0, 0, 0);
        }
        sendSerialcommand();
    }

}
