package net.happybrackets.sychronisedmodel;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.OSCUDPSender;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.json.JSONObject;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class SynchronisedModel {
    int me;
    List<Integer> myArrayIds = new ArrayList<Integer>();
    protected int myPosition;
    HB hb = null;

    protected List<Diad2d> diad2ds;
    protected boolean isRunning;
    protected int frameCount;
    protected int width, height;

    protected JSONObject modelField = null;

    IntegerControl textControl2 = null;

    ExecutionMode executionMode = null;

    public enum ExecutionMode {
        LOCAL,
        REMOTE,
        SERVER_FRAME,
        SERVER_STATE,
        SERVER_FIELD
    }

    static final int PORT = 9001;   // this is silence
    OSCUDPSender oscSender;
    String targetAddress;

    public SynchronisedModel() {
        executionMode = ExecutionMode.LOCAL;
        diad2ds = new ArrayList<>();
        defineMe();
    }

    public void setup(HBAction parentSketch, HB hb, ExecutionMode pExecutionMode) {
        this.executionMode = pExecutionMode;
        this.setup(parentSketch, hb);
    }

    public void setup(HBAction parentSketch, HB hb) {
        if(executionMode == null) {
            executionMode = ExecutionMode.LOCAL;
        }
        this.hb = hb;

        // Type textControl to generate this code
        textControl2 = new IntegerControl(parentSketch, "HBSynchronisedModel2_Everyone", 0) {
            @Override
            public void valueChanged(int control_val) {// Write your DynamicControl code below this line
                receiveDevicesId(control_val);
                // Write your DynamicControl code above this line
            }
        };
        textControl2.setControlScope(ControlScope.GLOBAL);
        textControl2.setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_HIDDEN);
        // End DynamicControl textControl code

        broadcastMe();

        if(executionMode == ExecutionMode.REMOTE) {
            openOSCListner();
        }

        if(executionMode == ExecutionMode.SERVER_FIELD
            || executionMode == ExecutionMode.SERVER_STATE
            || executionMode == ExecutionMode.SERVER_FRAME){
            openOSCServer();
        }

    }

    public void defineMe() {
        me = (int )(Math.random() * 100 + 1);
        myArrayIds.add(me);
        myPosition = myArrayIds.indexOf(me);
    }

    public void broadcastMe() {
        textControl2.setValue(me);
    }

    public void receiveDevicesId(int bufferInt) {
        if(!myArrayIds.contains(bufferInt)) {
            myArrayIds.add(bufferInt);
            Collections.sort(myArrayIds);
            broadcastMe();
            myPosition = myArrayIds.indexOf(me);
            hb.setStatus(me + ": Array - " + myArrayIds.toString());
        }
    }

    public int getMyPosition(){
        return myPosition;
    }

    public int getMe() {
        return me;
    }

    public int getDiadsCount() {
        return myArrayIds.size();
    }

    public void setup2DSpaceSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void update() {
        frameCount++;
        if(executionMode == ExecutionMode.SERVER_FIELD) {
            sendOSCModelField();
        }
        if(executionMode == ExecutionMode.SERVER_STATE) {
            sendOSCModelState();
        }
        if(executionMode == ExecutionMode.SERVER_FRAME) {
            sendOSCModelFrame();
        }
    }

    public void start() {
        isRunning = true;
        frameCount = 0;
        if(executionMode == ExecutionMode.SERVER_FIELD
                || executionMode == ExecutionMode.SERVER_STATE
                || executionMode == ExecutionMode.SERVER_FRAME){
            sendOSCStart();
        }
    }

    public void stop() {
        isRunning = false;
        frameCount = 0;
        if(executionMode == ExecutionMode.SERVER_FIELD
                || executionMode == ExecutionMode.SERVER_STATE
                || executionMode == ExecutionMode.SERVER_FRAME){
            sendOSCStop();
        }
    }

    public double getAverageRangeIntensityAtXY(int x, int y, int range) {
        int count = 0;
        double sumIntensity = 0;
        for (int i = x-range; i < x+range; i++) {
            for (int j = y-range; j < y+range; j++) {
                count++;
                sumIntensity = sumIntensity + getIntensityAtXY(i,j);
            }
        }
        return sumIntensity/count;
    }

    public JSONObject exportModelField(int resolution) {
        JSONObject obj = new JSONObject();
        obj.put("resolution", (Number)resolution);
        JSONObject map = new JSONObject();
        for (int i = resolution; i < (width-resolution); i=i+resolution*2) {
            for (int j = resolution; j < (height-resolution); j=j+resolution*2) {
                double intensity = getAverageRangeIntensityAtXY(i,j,resolution);
                if(intensity > 0) {
                    map.put(i + "," + j, intensity);
                }
            }

        }
        obj.put("map",map);
        return obj;
    }

    public void importModelField(JSONObject modelField) {
        frameCount++;
        this.modelField = modelField;
    }

    public double getFieldInsensityAtXY(int x, int y, int range) {
        if(modelField == null) return 0;
        JSONObject map = (JSONObject)modelField.get("map");
        Iterator<String> keys = map.keys();
        double returnIntensity = 0;
        EuclideanDistance e = new EuclideanDistance();
        double[] from = new double[]{(double)x, (double)y};
        double[] to = new double[]{0.0, 0.0};
        while(keys.hasNext()) {
            String key = keys.next();
            double intensity = map.getDouble(key);
            String[] xyString = key.split(",");
            to[0] = new Integer(xyString[0]);
            to[1] = new Integer(xyString[1]);
            if(e.compute(from,to) <= range) {
                returnIntensity = returnIntensity + intensity;
            }
        }
        return returnIntensity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameState(int frameState) {
        frameCount = frameState;
    }

    public JSONObject exportModelState() {
        JSONObject jo = new JSONObject();
        jo.put("framecount", frameCount);
        return jo;
    }

    private void openOSCServer() {
        targetAddress = "192.168.1.255";
        oscSender = new OSCUDPSender();
    }

    private void sendOSCStart() {
        OSCMessage message = HB.createOSCMessage("/start", 1);
        oscSender.send(message, targetAddress, PORT);
    }

    private void sendOSCStop() {
        OSCMessage message = HB.createOSCMessage("/start", 0);
        oscSender.send(message, targetAddress, PORT);
    }

    private void sendOSCModelState() {
        OSCMessage message = HB.createOSCMessage("/frameState", frameCount);
        oscSender.send(message, targetAddress, PORT);
    }

    private void sendOSCModelField() {
        JSONObject modelFieldJson = exportModelField(10);
        OSCMessage message = HB.createOSCMessage("/fieldState", modelFieldJson.toString());
        oscSender.send(message, targetAddress, PORT);
    }

    private void sendOSCModelFrame() {
        JSONObject modelStateJson = exportModelState();
        OSCMessage message = HB.createOSCMessage("/modelState", modelStateJson.toString());
        oscSender.send(message, targetAddress, PORT);
    }

    private void openOSCListner() {
        /* type osclistener to create this code */
        OSCUDPListener oscudpListener = new OSCUDPListener(PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                /* type your code below this line */
                // first display the source of message and message name
                String display_val = socketAddress.toString() + ": " + oscMessage.getName();
                String just_val = "";

                for (int i = 0; i < oscMessage.getArgCount(); i++){
                    // add each arg to display message
                    display_val = display_val + " " + oscMessage.getArg(i);
                    just_val = just_val + " " + oscMessage.getArg(i);
                }

                if(oscMessage.getName().equals("/start")) {
                    int value = (int) oscMessage.getArg(0);
                    if(value == 0) {
                        stop();
                    } else {
                        start();
                    }
                }

                if(oscMessage.getName().equals("/frameState")) {
                    int value = (int) oscMessage.getArg(0);
                    setFrameState(value);
                }

                if(oscMessage.getName().equals("/fieldState")) {
                    JSONObject fieldState = new JSONObject(just_val);
                    importModelField(fieldState);
                }

                if(oscMessage.getName().equals("/modelState")) {
                    JSONObject modelState = new JSONObject(just_val);
                    importModelState(modelState);
                }

                /* type your code above this line */
            }
        };
        if (oscudpListener.getPort() < 0){ //port less than zero is an error
            String error_message =  oscudpListener.getLastError();
            System.out.println(getMe() + "says: Error opening port " + PORT + " " + error_message);
        } else {
            System.out.println(getMe() + "says: Success opening port " + PORT);
        }
        /** end oscListener code */
    }

    public void importModelState(JSONObject modelState) {
        frameCount = modelState.getInt("framecount");
    }

    public void addDiad(Diad2d d) {
        diad2ds.add(d);
    }

    public Diad2d getDiad2d(int position) {
        return diad2ds.get(position);
    }

    public abstract double getIntensityAtXY(int x, int y);

    public abstract double getDiadIntensity();

}
