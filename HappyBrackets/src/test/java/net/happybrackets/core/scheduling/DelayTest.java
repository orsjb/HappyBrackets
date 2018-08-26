package net.happybrackets.core.scheduling;

import org.junit.Test;

public class DelayTest {

    final int TEST_TIMEOUT = 2000;



    final double MAX_ALLOWED_JITTER = 10;


    double maxJitter = 0;

    int MAX_TESTS = 1;

    // uncomment one of these if we want to display ticks
    //private final boolean SHOW_TICK = false;
    private final boolean SHOW_TICK = true;

    // we will wait on this and notify wen we have completed
    final Object completeWait = new Object();

    boolean delayRecieved = false;

    @Test
    public void testDelay() {

        for (int i = 1; i <= MAX_TESTS; i++) {
            final double DELAY_INTERVAL =   Math.random() * 100 + 10;


            System.out.println("---------------- Delay test " + i + " Interval: " + (long) DELAY_INTERVAL + " -----------------------");

            Delay testDelay = new Delay(DELAY_INTERVAL, HBScheduler.getGlobalScheduler().getSchedulerTime(),
                    new Delay.DelayListener() {
                        @Override
                        public void delayComplete(double offset, Object param) {
                            double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime();
                            if (SHOW_TICK) {
                                System.out.println("Tick " + (long) elapsed_time + " " + offset);
                            }
                            delayRecieved = true;
                            // make sure we are at the correct time
                            double called_time = (double) param;
                            System.out.println(elapsed_time + " " + called_time + " " + DELAY_INTERVAL);

                            assert (Math.abs(elapsed_time - (called_time + DELAY_INTERVAL)) < MAX_ALLOWED_JITTER);

                            if (maxJitter < Math.abs(offset)) {
                                maxJitter = Math.abs(offset);
                            }

                            synchronized (completeWait) {
                                completeWait.notify();
                            }
                        }
                    });


            // wait until
            synchronized (completeWait) {
                try {
                    completeWait.wait(TEST_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    assert (false);
                }
            }

            if (!delayRecieved) {
                System.out.println("Delay not received");
            }
            assert (delayRecieved);

            // check our jitter is acceptable
            assert (maxJitter < MAX_ALLOWED_JITTER);

            System.out.println("Maximum jitter: " + maxJitter);
        }

        System.out.println("------------------- Delay testing complete -----------------------");
    }
}
