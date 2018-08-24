package net.happybrackets.core.scheduling;

import org.junit.Test;

import java.util.PriorityQueue;

public class ScheduledEvents {
    // the number of items we will test
    final int NUM_ITEMS =  100000;


    // We will pop EventListeners off in order to make sure we are getting scheduled events right
    double last_key =  0;

    @Test
    public void testScheduledEventListener() {

        PriorityQueue<ScheduledObject> scheduledObjects = new PriorityQueue<>();

        // add our items to the queue
        for (int i = 0; i < NUM_ITEMS; i++)
        {
            double next = Math.random();
            scheduledObjects.add(new ScheduledObject(next, next, new ScheduledEventListener() {
                @Override
                public void doScheduledEvent(double scheduledTime, Object param) {

                    if (scheduledTime != last_key)
                    {
                        //System.out.println(scheduledTime);
                    }

                    // make sure we are in the correct order
                    assert (scheduledTime >= last_key);

                    //make sure we our parameter is what it was defined as
                    assert (param instanceof Double)  ;






                    last_key = scheduledTime;
                }
            }));
        }


        while (!scheduledObjects.isEmpty())
        {
            ScheduledObject next = scheduledObjects.poll();
            assert next != null;
            ScheduledEventListener listener = next.getScheduledEventListener();
            listener.doScheduledEvent(next.getScheduledTime(), next.getScheduledObject());
        }
        System.out.println("------------------- ScheduledEventListener testing complete -----------------------");
    }
}
