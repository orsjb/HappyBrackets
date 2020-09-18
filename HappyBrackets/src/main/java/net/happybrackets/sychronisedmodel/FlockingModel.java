package net.happybrackets.sychronisedmodel;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlockingModel extends SynchronisedModel {
    public Flock flock;
    int numBoids = 10;
    int randomSeed = 0;

    @Override
    public void update() {
        flock.run();
        super.update();
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

    public List<Integer> getBoidsIdAroundXY(int x, int y, int range) {
        List<Integer> returnList = new ArrayList<>();
        EuclideanDistance e = new EuclideanDistance();
        double[] from = {x,y};
        double[] to = {0,0};
        double distance;
        for (Boid b : flock.boids) {
            to[0] = b.location.x;
            to[1] = b.location.y;
            distance = e.compute(from, to);
            if(distance <= range) {
                returnList.add(b.id);
            }
        }
        return returnList;
    }

    @Override
    public void setFrameState(int frameState) {
        int updateTimes = (frameState - frameCount);
        if(frameState <= frameCount) return;
        for (int i = 0; i < updateTimes; i++) {
            update();
        }
    }

    public JSONObject exportModelState() {
        JSONObject modelState = new JSONObject();
        for (Boid b : flock.boids) {
            modelState.put("" + b.id,b.toJSON());
        }
        return modelState;
    }

    public void importModelState(JSONObject modelState) {
        Iterator<String> keys = modelState.keys();
        int numBoids = flock.boids.size();
        if(numBoids != modelState.length()) {
            System.out.println("number of Boids different from JSON Boids");
            return;
        }

        for (Boid b : flock.boids) {
            if(keys.hasNext()) {
                String key = keys.next();
                JSONObject jo = (JSONObject) modelState.get(key);
                b.loadJSON(jo);
            }
        }
    }

}

