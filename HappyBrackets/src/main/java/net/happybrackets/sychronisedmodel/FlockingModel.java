package net.happybrackets.sychronisedmodel;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

public class FlockingModel extends SynchronisedModel {
    public Flock flock;
    int numBoids = 10;
    int randomSeed = 0;

    @Override
    public void update() {
        super.update();
        flock.run();
    }

    public void start() {
        super.start();
        flock = Flock.spawn(0, height * 0.5, width,height, numBoids, randomSeed);
        for (Diad2d d : diad2ds) {
            flock.addDiad(d);
        }
    }

    public void stop() {
        super.stop();
        flock = null;
    }

    public double getDiadIntensity() {
        if(modelField != null) {
            Diad2d d = diad2ds.get(myPosition);
            return super.getFieldInsensityAtXY(d.x, d.y, d.size);
        } else {
            return flock.getDiadGaussianIntensity(myPosition);
            //return flock.getDiadActivation(myPosition);
        }
    }

    @Override
    public double getAverageRangeIntensityAtXY(int x, int y, int range) {
        NormalDistribution d = new NormalDistribution(0, 1);
        EuclideanDistance e = new EuclideanDistance();
        double[] from = {x,y};
        double[] to = {0,0};
        double distance;
        double sumDensity = 0;
        for (Boid b : flock.boids) {
            to[0] = b.location.x;
            to[1] = b.location.y;
            distance = e.compute(from, to);
            if(distance <= range) {
                sumDensity = sumDensity + d.density(distance);
            }
        }
        return sumDensity;
    }

    public double getIntensityAtXY(int x, int y) {
        return flock.getGaussianIntensityAtXY(x,y);
    }

    public void setupFlock(int numBoids, int randomSeed) {
        this.numBoids = numBoids;
        this.randomSeed = randomSeed;
    }

    public double getDiadGaussianIntensity() {
        return flock.getDiadGaussianIntensity(myPosition);
    }

    @Override
    public void setFrameState(int frameState) {
        int updateTimes = (frameState - frameCount);
        if(frameState <= frameCount) return;
        for (int i = 0; i < updateTimes; i++) {
            update();
        }
    }

}

