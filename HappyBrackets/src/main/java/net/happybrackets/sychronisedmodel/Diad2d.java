package net.happybrackets.sychronisedmodel;

import java.util.List;

public class Diad2d {
    public int x,y,size;
    public String name;

    public Diad2d(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        size = 20;
    }

    public int checkBoidsAround(List<Boid> boids) {
        int countBoids = 0;
        for (Boid b : boids) {
            // Check if Boid is inside diad's range
            if(b.location.x < (x+(size/2)) &&  b.location.x > (x-(size/2))
                    && b.location.y < (y+(size/2)) &&  b.location.y > (y-(size/2))
            ) {
                countBoids++;
            }
        }
        return countBoids;
    }

}
