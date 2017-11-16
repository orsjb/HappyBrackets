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

package net.happybrackets.kadenze_course.assignment_tasks.session6;

import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

import java.util.Hashtable;

/**
 * For the assessable tasks in Sessions 6 onwards we use a simplified version of the code checker interface.
 *
 * This task is about using Hashtables. Given the Hashtable that is passed to you from the checker, print to the
 * StringBuffer the name of the index, followed by the type of the value at that index.
 *
 * Make sure you write your answers into the StringBuffer, not to System.out.
 *
 * For example, the example data below would print out as follows (the order doesn't matter):
 *
 * height Float
 * age Integer
 * children Integer
 * city String
 */
public class CodeTask6_1 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        Hashtable<String, Object> testShare = new Hashtable<>();
        testShare.put("age", 39);
        testShare.put("height", 180.1f);
        testShare.put("children", 0);
        testShare.put("city", "Sydney");
        new CodeTask6_1().task(null, buf, new Object[]{testShare});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        Hashtable<String, Object> share = (Hashtable<String, Object>) objects[0];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
