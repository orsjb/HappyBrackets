package net.happybrackets.tutorial.session9;

import net.beadsproject.beads.core.Bead;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This example demonstrates the observer pattern.
 */
public class ObservableObject {

    public interface Listener {
        public void eventOccurred();
    }
    List<Listener> myListeners;
    float[] myarray = new float[] {1.4f, 2.8f, 3.6f, 4.8f};

    public ObservableObject() {
        myListeners = new ArrayList<>();
        new Thread() {
            public void run() {
                while(true) {
                    somethingHappened();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void doSomethingToTheFloat(float[] x) throws IOException, ClassNotFoundException {
        for(float f : x) {
            System.out.println(f * 3 % 2);
            try {
                Thread.sleep(1000);
            } catch(Exception e) {
                e.printStackTrace();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream("myFile");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                ObjectInputStream ois = new ObjectInputStream(fis);
                Bead b = (Bead)ois.readObject();
                b.getKillListener().kill();
            }

        }
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
        ObservableObject o = new ObservableObject();
        o.addListener(new Listener() {
            @Override
            public void eventOccurred() {
                System.out.println("Event Occurred");
            }
        });
    }



}
