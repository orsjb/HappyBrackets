package net.happybrackets.assignment_tasks.session6;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

import java.util.Hashtable;

/**
 * For the assessable tasks in Sessions 6 onwards we use a simplified version of the code checker interface.
 *
 * This task is about using Hashtables. Given the Hashtable that is passed to you from the checker, print to the StringBuffer the name of the index, followed by the type of the value at that index.
 *
 * For example, the example data below would print out as follows (the order doesn't matter):
 *
 * height Float
 * age Integer
 * children Integer
 * city String
 */
public class CodeTask6_1 implements SimpleCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        Hashtable<String, Object> testShare = new Hashtable<>();
        testShare.put("age", 39);
        testShare.put("height", 180.1f);
        testShare.put("children", 0);
        testShare.put("city", "Sydney");
        new CodeTask6_1().task(new Object[]{buf, testShare});
        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        StringBuffer buf = (StringBuffer)objects[0];
        Hashtable<String, Object> share = (Hashtable<String, Object>)objects[1];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
