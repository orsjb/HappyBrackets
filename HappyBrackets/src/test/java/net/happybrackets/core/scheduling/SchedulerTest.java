package net.happybrackets.core.scheduling;

import org.junit.Test;

public class SchedulerTest {

    // if we reach this amount of time we have failed
    final int TEST_TIMEOUT = 3000;

    // this is the maximum scheduled event time
    final int MAX_SCHEDULED_DURATION = 2000;

    // the number of items we will test
    final int NUM_ITEMS =  10000;

    int numberRecieved = 0;

    // we will wait on this and notify when we have completed
    final Object completeWait = new Object();

    // record the maximum jitter
    long maxJitter = 0;

    // during our test we will check that we did not get any out of order
    double previousEventTime =  0;
    @Test
    public void testScheduler() {

        assert (TEST_TIMEOUT > MAX_SCHEDULED_DURATION);

        HBScheduler scheduler = new HBScheduler(Thread.MAX_PRIORITY);

        //scheduler.displayNotifyMessage();

        Thread addThread = new Thread(new Runnable() {
            public void run() {
                // add our items to the queue
                for (int i = 0; i < NUM_ITEMS; i++)
                {
                    // This the minimum time we are waiting as our first item

                    final int MIN_SCHEDULE_TIME = 200;
                    // we want to start at minimum 1 second
                    double next = Math.random() * (MAX_SCHEDULED_DURATION - MIN_SCHEDULE_TIME) + MIN_SCHEDULE_TIME;

                    scheduler.addScheduledObject(next, next, new ScheduledEventListener() {
                        @Override
                        public void doScheduledEvent(double scheduledTime, Object param) {

                            // make sure that none of our events come back at wrong order
                            assert (previousEventTime <= scheduledTime);

                            previousEventTime = scheduledTime;

                            double jitter = Math.abs(scheduledTime - scheduler.getSchedulerTime());
                            if (maxJitter < jitter ){
                                maxJitter = (long)jitter;
                            }

                            numberRecieved++;
                            if (numberRecieved >= NUM_ITEMS){
                                synchronized (completeWait){
                                    completeWait.notifyAll();
                                }
                            }
                            //System.out.println((long) scheduledTime + " " + scheduler.getSchedulerTime());
                        }
                    });

                    //System.out.println("Added " + i);
                }

            }
        });
        addThread.setPriority(Thread.NORM_PRIORITY);
        addThread.start();


        // wait until all items are received or our test has timed out
        synchronized (completeWait){
            try {
                completeWait.wait(TEST_TIMEOUT);
            } catch (InterruptedException e) {

            }
        }


        scheduler.endScheduler();
        assert (numberRecieved == NUM_ITEMS);
        System.out.println("Number received " + numberRecieved + " Max Jitter " + maxJitter);
        System.out.println("------------------- Scheduler testing complete -----------------------");
    }
}
