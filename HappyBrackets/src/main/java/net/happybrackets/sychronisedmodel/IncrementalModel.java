package net.happybrackets.sychronisedmodel;

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

}

