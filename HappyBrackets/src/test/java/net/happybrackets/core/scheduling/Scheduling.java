package net.happybrackets.core.scheduling;

import org.junit.Test;

import java.util.PriorityQueue;

import static org.junit.Assert.assertEquals;

public class Scheduling {
    // the number of items we will test
    final int NUM_ITEMS =  100000;

    @Test
    public void testPriorityQueue() {

        PriorityQueue<ScheduledObject> scheduledObjects = new PriorityQueue<>();

        // add our items to the queue
        for (int i = 0; i < NUM_ITEMS; i++)
        {
            double next = Math.random();
            scheduledObjects.add(new ScheduledObject((long)next , next, null));
        }

        // now pop them off and make sure they are in order
        double last_key =  0;

        while (!scheduledObjects.isEmpty())
        {
            ScheduledObject next = scheduledObjects.poll();
            assert next != null;
            double next_key = next.getScheduledTime();

            if (next_key != last_key) {
                //System.out.println(next_key);
            }
            assert (next_key >= last_key);
            last_key = next_key;

        }
        System.out.println("------------------- testPriorityQueue testing complete -----------------------");
    }
}
