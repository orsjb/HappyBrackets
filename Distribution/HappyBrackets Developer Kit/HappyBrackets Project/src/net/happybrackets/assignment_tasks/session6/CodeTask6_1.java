package net.happybrackets.assignment_tasks.session6;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.extras.assignment_autograding.SimpleCheckable;

/**
 * For the assessable tasks in Sessions 6 onwards we use a simplified version of the code checker interface.
 */
public class CodeTask6_1 implements SimpleCheckable, HBAction {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();
        new CodeTask6_1().task(new Object[]{buf});
        System.out.println(buf);
    }

    @Override
    public void action(HB hb) {
        StringBuffer buf = new StringBuffer();
        task(new Object[]{buf});
        System.out.println(buf);
    }

    @Override
    public void task(Object... objects) {
        //********** do your work here ONLY **********
        StringBuffer buf = (StringBuffer)objects[0];
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
