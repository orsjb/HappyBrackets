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
