package net.happybrackets.sychronisedmodel;

import org.json.JSONObject;

public class IncrementalModel extends SynchronisedModel {

    public double getIntensityAtXY(int x, int y) {
        return frameCount+x+y;
    }

    public double getDiadIntensity() {
        if((frameCount % myArrayIds.size()) == myPosition) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public JSONObject exportModelState() {
        JSONObject jo = new JSONObject();
        jo.put("framecount", frameCount);
        return jo;
    }

    @Override
    public void importModelState(JSONObject modelState) {
        frameCount = modelState.getInt("framecount");
    }

}

