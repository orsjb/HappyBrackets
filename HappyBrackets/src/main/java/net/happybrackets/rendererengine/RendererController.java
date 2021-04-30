package net.happybrackets.rendererengine;

import com.pi4j.io.serial.*;
import de.sciss.net.OSCMessage;
import net.beadsproject.beads.ugens.Gain;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.core.HBReset;
import net.happybrackets.device.HB;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;

/**
 * The RendererController manages the instantiated Renderer in the device.
 * It only adds Renderer if the {@link Renderer#hostname} matches the device hostname.
 * It contains an internal clock {@link RendererController#internalClock} that will be used by the Renderers.
 * This class processes the Java Annotations that are added to any sketch that extender {@link Renderer}.
 * Annotations available: {@link HBCommand}, {@link HBParam} and {@link HBNumberRange}.
 * Check the example at the HappyBrackets Plugin default project on examples/rendererengine/renderer
 */
public class RendererController {

    public static enum RenderMode {
        UNITY, REAL
    }

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
        HB.getAudioOutput().clearInputConnections();
        hasLight = hasSpeaker = hasSerial = false;
        internalClock.clearClockTickListener();
        internalClock.stop();
        internalClock.reset();

        for (Object loaded_class : renderers) {
            try {
                Class<?>[] interfaces = loaded_class.getClass().getInterfaces();
                for (Class<?> cc : interfaces) {
                    if (cc.equals(HBReset.class)) {
                        ((HBReset)loaded_class).doReset();
                    }
                }
            } catch (Exception ex){}
        }
        renderers.clear();
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

    /**
     * Add a Renderer to the Renderer list of this singleton RendererController.
     * The Renderer will only be effectively added if <b>hostname</b> is the same as the current device hostname.
     * Exception if running as UNITY mode.
     * @param type Renderer type
     * @param hostname device hostname
     * @param x x position
     * @param y y position
     * @param z z position
     * @param name Renderer name
     * @param id Renderer id
     * @return Renderer object
     */
    public Renderer addRenderer(Renderer.Type type, String hostname, float x, float y, float z, String name, int id) {
        return addRenderer(type, hostname,x,y,z,name,id,16);
    }

    public Renderer addRenderer(Renderer.Type type, String hostname, float x, float y, float z, String name, int id, int stripSize) {
        if(!currentHostname.contains("hb-") && hostname.equals("Unity")) {
            if(type == Renderer.Type.LIGHT) {
                Renderer r = null;
                try {
                    r = rendererClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                r.initialize(hostname, type, x, y, z, name, 0);
                renderers.add(r);
                r.setupLight();
                if (isUnity == false) {
                    oscSender = new OSCUDPSender();
                }
                isUnity = true;
                return r;
            }
            if(type == Renderer.Type.SPEAKER) {
                Renderer r = null;
                try {
                    r = rendererClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                r.initialize(hostname, type, x, y, z, name, 0);
                renderers.add(r);
                hasSpeaker = true;
                r.out = new Gain(1, 1);
                HB.getAudioOutput().addInput(0, r.out, 0);
                r.setupAudio();
                return r;
            }
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
                return r;
            }
        }
        catch (IllegalAccessException | InstantiationException ex) {
            ex.printStackTrace();
        }
        return null;
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

    /**
     * Loads a CSV files containing the hardware configuration of a certain exibition.
     * Required CSV columns: PParent,Parent,Device ID,ObjectName,x,y,z,stripSize
     * @param filepath location of the csv file
     * @param renderMode mode of rendering: Local or Unity {@link RenderMode}
     */
    public void loadHardwareConfiguration(String filepath, RenderMode renderMode) {
        if(renderMode == RenderMode.UNITY)
            loadHardwareConfiguration("Unity", filepath);
        else
            loadHardwareConfiguration("", filepath);
    }

    private void loadHardwareConfiguration(String deviceName,String filepath) {
        //List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line, deviceID;
            Renderer.Type rType;
            line = br.readLine(); // skip header line
            String[] header = line.split("," );
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

                Renderer r = null;
                if(values[3].contains("LED")) {
                    rType = Renderer.Type.LIGHT;
                    r = addRenderer(rType,deviceID,x,y,z,objectName,objectId,LEDstripSize);
                } else {
                    rType = Renderer.Type.SPEAKER;
                    r = addRenderer(rType,deviceID,x,y,z,objectName,objectId);
                }

                if(r != null && header.length > 8 && values.length > 8) {
                    HashMap<String, String> csvData = new HashMap<>();
                    for (int i = 8; i < values.length; i++) {
                        if(header[i] != null && values[i] != null) {
                            csvData.put(header[i], values[i]);
                        }
                    }
                    if(!csvData.isEmpty()) {
                        r.csvData = csvData;
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called by {@link HB} when loading a sketch class that extends {@link Renderer}
     */
    public void setup() {

        processAnnotations();

        RenderMode renderMode = RenderMode.REAL;

        try {
            Field f = rendererClass.getField("renderMode");
            renderMode = (RenderMode)f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Problem loading renderMode field: " + e.getMessage());
        }



        try {
            Field f = rendererClass.getField("installationConfig");
            String installationConfigFile = (String)f.get(null);
            loadHardwareConfiguration(installationConfigFile, renderMode);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Problem loading installationConfig field: " + e.getMessage());
        }

        internalClock.addClockTickListener((v, clock) -> {
                    renderers.forEach(r -> {
                        r.tick(clock);
                    });
                    sendSerialcommand();
                }
        );

        internalClock.start();
    }

    public Renderer getRendererByName(String name) {
        return rendererHashMap.get(name);
    }


    /**
     * Process Annotations {@link HBCommand}, {@link HBParam} and {@link HBNumberRange}. and create an OSCListener if any of those exists.
     */
    private void processAnnotations() {

        class HBParamAnnotations {
            Class type;
            Double min = null;
            Double max = null;
            public HBParamAnnotations(Class type, double min, double max) {
                this.type = type;
                this.min = min;
                this.max = max;
            }
            public HBParamAnnotations(Class type) {
                this.type = type;
            }
        }

        Renderer newinstance = null;
        try {
            newinstance = rendererClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Map<String, HBParamAnnotations> exposedVariables = new HashMap<>();
        Map<String, Class[]> exposedMethods = new HashMap<>();

        int oscPort =  9002;

        for(Field field : newinstance.getClass().getDeclaredFields()){

            Class type = field.getType();
            String name = field.getName();
            Annotation[] annotations = field.getAnnotations();
            boolean isExposed = false;

            for(Annotation ann: annotations) {
                if(ann.annotationType() == HBNumberRange.class) {
                    HBNumberRange annotation = field.getAnnotation(HBNumberRange.class);
                    double min = annotation.min();
                    double max = annotation.max();
                    exposedVariables.put(name,new HBParamAnnotations(type, min, max));
                    isExposed = true;
                }

                if(ann.annotationType() == HBParam.class) {
                    exposedVariables.put(name,new HBParamAnnotations(type));
                    isExposed = true;
                }
            }

            try {
                Object value = field.get(newinstance);
                if(name.equals("clockInterval")) {
                    internalClock.setInterval((int)value);
                }
            } catch (IllegalAccessException e) {
                if(isExposed || name.equals("clockInterval"))
                    System.out.println("Not possible to read field: " + name + ". Must be non static and public");
            }

            try {
                Object value = field.get(newinstance);
                if(name.equals("oscPort")) {
                    oscPort = (int)value;
                }
            } catch (IllegalAccessException e) {
                if(isExposed || name.equals("oscPort"))
                    System.out.println("Not possible to read field: " + name + ". Must be non static and public");
            }

        }

        for(Method method : newinstance.getClass().getDeclaredMethods()){
            String name = method.getName();
            Annotation[] annotations = method.getAnnotations();

            for(Annotation ann: annotations) {
                if(ann.annotationType() == HBCommand.class) {
                    if(exposedMethods.containsKey(name)) {
                        System.out.println("ERROR - Multiple methods with the same name: " + name);
                        continue;
                    }
                    exposedMethods.put(name,method.getParameterTypes());
                }
            }

        }

        if(exposedMethods.isEmpty() && exposedVariables.isEmpty()) return;

        OSCUDPListener SCListener = new OSCUDPListener(oscPort) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                try {
                    String messageName = oscMessage.getName().substring(1);
                    if(exposedVariables.containsKey(messageName)) {
                        HBParamAnnotations annotation = exposedVariables.get(messageName);

                        renderers.forEach(r -> {
                            Field field = null;
                            try {
                                field = r.getClass().getField(messageName);
                                switch (annotation.type.toString()) {
                                    case "java.lang.String" :
                                        field.set(r, String.valueOf(oscMessage.getArg(0)));
                                        break;
                                    case "class de.sciss.net.OSCMessage":
                                        field.set(r, oscMessage);
                                        break;
                                    case "float":
                                        try {
                                            float value = (float) oscMessage.getArg(0);
                                            if(annotation.min != null
                                                    && value >= annotation.min
                                                    && value <= annotation.max) {
                                                field.setFloat(r, value);
                                            }
                                        } catch( ClassCastException e) {
                                            field.setFloat(r, (int)oscMessage.getArg(0));
                                        }
                                        break;
                                    case "int":
                                        int value = (int) oscMessage.getArg(0);
                                        if(annotation.min != null
                                                && value >= annotation.min
                                                && value <= annotation.max) {
                                            field.setInt(r, value);
                                        }
                                        break;
                                    case "boolean":
                                        field.setBoolean(r, (boolean)oscMessage.getArg(0));
                                        break;
                                    default:
                                        System.out.println("ERROR Data Type not expected: " + annotation.type + ". Expected values: f i b s");
                                }
                            } catch (NoSuchFieldException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    if(exposedMethods.containsKey(messageName)) {
                        renderers.forEach(r -> {
                            Class types[] = exposedMethods.get(messageName);
                            Method m = null;
                            try {
                                m = r.getClass().getMethod(messageName, types);
                                Object[] ObjectArray = new Object[types.length];
                                /*
                                For each arguments in the OSC message, cast this argument to the expected type according to the method signature
                                Matching the osc parameter position with the method parameter position
                                 */
                                int arg = 0;
                                for(Class argType: types) {
                                    switch (argType.toString()) {
                                        case "java.lang.String" :
                                            ObjectArray[arg] = String.valueOf(oscMessage.getArg(arg));
                                            break;
                                        case "class de.sciss.net.OSCMessage":
                                            ObjectArray[arg] =  oscMessage;
                                            break;
                                        case "float":
                                            try {
                                                ObjectArray[arg] = (float)oscMessage.getArg(arg);
                                            } catch( ClassCastException e) {
                                                ObjectArray[arg] = (int)oscMessage.getArg(arg);
                                            }
                                            break;
                                        case "int":
                                            ObjectArray[arg] =  (int)oscMessage.getArg(arg);
                                            break;
                                        case "boolean":
                                            ObjectArray[arg] = (boolean)oscMessage.getArg(arg);
                                            break;
                                        default:
                                            System.out.println("ERROR Data Type not expected: " + argType.toString() + ". Expected values: f i b s");
                                    }
                                    arg++;
                                }
                                m.invoke(r, ObjectArray);
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

}
