package net.happybrackets.sychronisedmodels;

import java.util.List;
import java.util.Random;
import static java.lang.Math.*;

public class Boid {
    Random margin = null;
    static final Vec migrate = new Vec(0.02, 0);
    static final int size = 3;
    int width, height;

    final double maxForce, maxSpeed;

    Vec location, velocity, acceleration;
    private boolean included = true;

    Boid(double x, double y, int seed, int width, int height) {
        this.width = width;
        this.height = height;
        margin = new Random(seed);
        acceleration = new Vec();
        velocity = new Vec(margin.nextInt(3) + 1, margin.nextInt(3) - 1);
        location = new Vec(x, y);
        maxSpeed = 3.0;
        maxForce = 0.05;
    }

    void update() {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        location.add(velocity);
        acceleration.mult(0);
    }

    void applyForce(Vec force) {
        acceleration.add(force);
    }

    Vec seek(Vec target) {
        Vec steer = Vec.sub(target, location);
        steer.normalize();
        steer.mult(maxSpeed);
        steer.sub(velocity);
        steer.limit(maxForce);
        return steer;
    }

    void flock(List<Boid> boids) {
        view(boids);

        Vec rule1 = separation(boids);
        Vec rule2 = alignment(boids);
        Vec rule3 = cohesion(boids);

        rule1.mult(2.5);
        rule2.mult(1.5);
        rule3.mult(1.3);

        applyForce(rule1);
        applyForce(rule2);
        applyForce(rule3);
        applyForce(migrate);
    }

    void view(List<Boid> boids) {
        double sightDistance = 100;
        double peripheryAngle = PI * 0.85;

        for (Boid b : boids) {
            b.included = false;

            if (b == this)
                continue;

            double d = Vec.dist(location, b.location);
            if (d <= 0 || d > sightDistance)
                continue;

            Vec lineOfSight = Vec.sub(b.location, location);

            double angle = Vec.angleBetween(lineOfSight, velocity);
            if (angle < peripheryAngle)
                b.included = true;
        }
    }

    Vec separation(List<Boid> boids) {
        double desiredSeparation = 25;

        Vec steer = new Vec(0, 0);
        int count = 0;
        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < desiredSeparation)) {
                Vec diff = Vec.sub(location, b.location);
                diff.normalize();
                diff.div(d);        // weight by distance
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.div(count);
        }

        if (steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        return new Vec(0, 0);
    }

    Vec alignment(List<Boid> boids) {
        double preferredDist = 50;

        Vec steer = new Vec(0, 0);
        int count = 0;

        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                steer.add(b.velocity);
                count++;
            }
        }

        if (count > 0) {
            steer.div(count);
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
        }
        return steer;
    }

    Vec cohesion(List<Boid> boids) {
        double preferredDist = 50;

        Vec target = new Vec(0, 0);
        int count = 0;

        for (Boid b : boids) {
            if (!b.included)
                continue;

            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                target.add(b.location);
                count++;
            }
        }
        if (count > 0) {
            target.div(count);
            return seek(target);
        }
        return target;
    }

    // Wraparound
    void borders() {
        int margin = 2;
        if (location.x < -margin) location.x = width+ margin;
        if (location.y < -margin) location.y = height+ margin;
        if (location.x > width+ margin) location.x = -margin;
        if (location.y > height+ margin) location.y = -margin;
    }

    void run(List<Boid> boids, int w, int h) {
        flock(boids);
        update();
        borders();
    }
}

class Vec {
    double x, y;

    Vec() {
    }

    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }

    void add(Vec v) {
        x += v.x;
        y += v.y;
    }

    void sub(Vec v) {
        x -= v.x;
        y -= v.y;
    }

    void div(double val) {
        x /= val;
        y /= val;
    }

    void mult(double val) {
        x *= val;
        y *= val;
    }

    double mag() {
        return sqrt(pow(x, 2) + pow(y, 2));
    }

    double dot(Vec v) {
        return x * v.x + y * v.y;
    }

    void normalize() {
        double mag = mag();
        if (mag != 0) {
            x /= mag;
            y /= mag;
        }
    }

    void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x *= lim / mag;
            y *= lim / mag;
        }
    }

    double heading() {
        return atan2(y, x);
    }

    static Vec sub(Vec v, Vec v2) {
        return new Vec(v.x - v2.x, v.y - v2.y);
    }

    static double dist(Vec v, Vec v2) {
        return sqrt(pow(v.x - v2.x, 2) + pow(v.y - v2.y, 2));
    }

    static double angleBetween(Vec v, Vec v2) {
        return acos(v.dot(v2) / (v.mag() * v2.mag()));
    }
}