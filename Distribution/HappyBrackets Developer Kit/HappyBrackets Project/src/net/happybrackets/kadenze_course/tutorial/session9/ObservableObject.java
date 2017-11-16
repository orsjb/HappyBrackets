/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.kadenze_course.tutorial.session9;

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
        for (Listener l : myListeners) {
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
                while (true) {
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
