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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ollie on 19/10/16.
 */
public class TestSimpleCheker {

    public static void main(String[] args) {

        //create the checkable thing
        //in reality this would already exist as some class provided by the student
        //e.g., SimpleCheckable checkable = new AssignmentXCheckable(); <<-- compiled from student src code
        SimpleCheckable checkable = new SimpleCheckable() {
            @Override
            public void task(Object... args) {

                //extract the args as required (each assignment will provide the relevant code)
                float input = (Float)args[0];
                List<Float> results = (List<Float>)args[1];

                //do some calculating (this is the bit done by the student
                float theAnswer = input * 2f;

                //put the result somewhere for the autograder to access (this code also provided in assignment)
                results.add(theAnswer);

            }
        };

        //call the task
        List<Float> results = new ArrayList<Float>();
        checkable.task((Float)1.4f, results);

        //grab the results
        float theAnswer = results.get(0);
        System.out.println("The answer is: " + theAnswer);

    }
}
