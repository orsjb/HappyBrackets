package net.happybrackets.sychronisedmodel;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.*;
import net.happybrackets.device.HB;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.json.JSONObject;

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

    public SynchronisedModel() {
        diad2ds = new ArrayList<>();
        defineMe();
    }

    public void setup(HBAction parentSketch, HB hb) {
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
    }

    public void start() {
        isRunning = true;
        frameCount = 0;
    }

    public void stop() {
        isRunning = false;
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

    public void addDiad(Diad2d d) {
        diad2ds.add(d);
    }

    public abstract double getIntensityAtXY(int x, int y);

    public abstract double getDiadIntensity();

    public abstract JSONObject exportModelState();

    public abstract void importModelState(JSONObject modelState);

}
