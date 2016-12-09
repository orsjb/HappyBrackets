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

package net.happybrackets.extras.assignment_autograding;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

/**
 * Created by ollie on 8/08/2016.
 */
public class TestBeadsChecker {

    public static void main(String[] args) throws IOException {

        //this is the directory for generating results. We assume this directory exists
        //this is located relative to wherever Java is being called
        String outputDir = "build/Test Results";

        //this is the student's work, this will be loaded dynamically
        ExampleBeadsCheckable checkable = new ExampleBeadsCheckable();

        //this is your customisable checker function
        BeadsChecker.BeadsCheckerFunction myFunc = new BeadsChecker.BeadsCheckerFunction() {
            @Override
            public void runCheck(AudioContext ac, int count) {
                //INTERESTING SHIT HERE - take a look at the signal chain
                System.out.println(ac.getTime());
                //what else could you do?
                //look at what is plugged into the AudioContext out.
                Set<UGen> inputs = ac.out.getConnectedInputs();
                for(UGen item : inputs) {
                    System.out.println("Connected UGen: " + item);
                    if(item instanceof Gain) {
                        System.out.println("It's a Gain!");

                    } else if(item instanceof WavePlayer) {
                        WavePlayer wp = (WavePlayer)item;
                        System.out.println("It's a WavePlayer! Frequency = " + wp.getFrequencyUGen().getValue());
                        
                    }
                }
                //write some stuff to text file
                try {
                    PrintStream ps = new PrintStream(new File(outputDir + "/customChecker" + count));
                    ps.println("Hello checker!");
                    ps.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        //now run the checker, this outputs various things by default, as well as calling your runCheck()
        //at each of the snapshot moments.
        BeadsChecker checker = new BeadsChecker(checkable, 10000, 1000, myFunc, outputDir);

    }

}
