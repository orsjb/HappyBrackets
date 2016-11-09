package net.happybrackets.tutorial.session9;

import net.beadsproject.beads.core.Bead;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This example demonstrates the observer pattern. It runs standalone.
 */
public class ObservableObject {

    public interface Listener {
        public void eventOccurred();
    }
    private List<Listener> myListeners;
    private int x;

    public ObservableObject() {
        myListeners = new ArrayList<>();
        x = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
        somethingHappened();
    }

    public void addListener(Listener newListener) {
        myListeners.add(newListener);
    }

    private void somethingHappened() {
        for(Listener l : myListeners) {
            l.eventOccurred();
        }
    }

    public static void main(String[] args) {
        //create the observable object
        ObservableObject o = new ObservableObject();
        //add a new listener that will react when something happens
        o.addListener(new Listener() {
            @Override
            public void eventOccurred() {
                System.out.println("Event Occurred: x=" + o.getX());
            }
        });
        //set up a thread that keeps repeating the somethingHappenedAction().
        new Thread() {
            public void run() {
                int count = 0;
                while(true) {
                    o.setX(count++);
                    try {
                        Thread.sleep(100 * count);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

}
