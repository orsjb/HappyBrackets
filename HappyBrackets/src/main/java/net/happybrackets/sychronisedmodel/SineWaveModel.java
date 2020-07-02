package net.happybrackets.sychronisedmodel;

import static org.apache.commons.math3.util.MathUtils.TWO_PI;

public class SineWaveModel extends SynchronisedModel {

    public double getIntensityAtXY(int x, int y) {
        return (Math.sin( x *Math.sin(frameCount)/10 * TWO_PI / width) + Math.sin((float)(y + frameCount) * TWO_PI / height));
    }

    public double getDiadIntensity() {
        Diad2d d = diad2ds.get(myPosition);
        return getIntensityAtXY(d.x, d.y);
    }

}

