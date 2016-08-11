package net.happybrackets.extras.assignment_autograding;

import java.io.IOException;

/**
 * Created by ollie on 8/08/2016.
 */
public class TestBeadsChecker {

    public static void main(String[] args) throws IOException {

        ExampleBeadsCheckable checkable = new ExampleBeadsCheckable();
        BeadsChecker checker = new BeadsChecker(checkable, 10000, 1000, "build/Test Results");

    }


}
