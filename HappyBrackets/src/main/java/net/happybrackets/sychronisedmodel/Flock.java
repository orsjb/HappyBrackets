package net.happybrackets.sychronisedmodel;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.util.ArrayList;
import java.util.List;

public class Flock {
    public List<Boid> boids;

    List<Diad2d> diad2ds;
    static Flock flock;

    int width, height;

    Flock() {
        boids = new ArrayList<>();
        diad2ds = new ArrayList<>();
    }

    void run() {
        for (Boid b : boids) {
            b.run(boids, width, height);
        }
    }

    boolean hasLeftTheBuilding() {
        int count = 0;
        for (Boid b : boids) {
            if (b.location.x + Boid.size > width)
                count++;
        }
        return boids.size() == count;
    }

    void addBoid(Boid b) {
        boids.add(b);
    }

    void addDiad(Diad2d d) {
        diad2ds.add(d);
    }

    public int getDiadActivation(int i) {
        if(i > diad2ds.size()) return 0;
        Diad2d d = diad2ds.get(i);
        return d.checkBoidsAround(boids);
    }

    public int getBoidsAroundXY(int x, int y, int range) {
        int countBoids = 0;
        EuclideanDistance e = new EuclideanDistance();
        double[] from = {x,y};
        double[] to = {0,0};
        double distance;
        for (Boid b : boids) {
            to[0] = b.location.x;
            to[1] = b.location.y;
            distance = e.compute(from, to);
            if(distance <= range) {
                countBoids++;
            }
        }
        return countBoids;
    }


    public double getDiadGaussianIntensity(int i) {
        if(i > diad2ds.size()) return 0;
        Diad2d d = diad2ds.get(i);
        return getGaussianIntensityAtXY(d.x, d.y);
    }

    public double getGaussianIntensityAtXY(int x, int y) {
        return getGaussianIntensityAtXY(x, y, 0, 1);
    }

    public double getGaussianIntensityAtXY(int x, int y, double mean, double sd) {
        NormalDistribution d = new NormalDistribution(mean, sd);
        EuclideanDistance e = new EuclideanDistance();
        double[] from = {x,y};
        double[] to = {0,0};
        double distance;
        double sumDensity = 0;
        for (Boid b : boids) {
            to[0] = b.location.x;
            to[1] = b.location.y;
            distance = e.compute(from, to);
            sumDensity = sumDensity + d.density(distance);
        }
        return sumDensity;
    }

    static Flock spawn(double wStart, double hStart, int width, int height, int numBoids, int randomSeed) {
        flock = new Flock();
        flock.width = width;
        flock.height = height;
        for (int i = 0; i < numBoids; i++)
            flock.addBoid(new Boid(i, wStart, hStart, i + randomSeed, width, height));
        return flock;
    }

    public void resetBoids(int randomSeed) {
        int numBoids = boids.size();
        boids = new ArrayList<>();
        for (int i = 0; i < numBoids; i++)
            flock.addBoid(new Boid(i, 0, 0, i + randomSeed, width, height));
    }
}
