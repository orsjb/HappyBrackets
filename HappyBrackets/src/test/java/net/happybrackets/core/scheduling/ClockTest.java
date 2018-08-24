package net.happybrackets.core.scheduling;

import org.junit.Test;

public class ClockTest {

    final int TEST_TIMEOUT = 1000;

    final int CLOCK_INTERVAL =  100;

    final int MAX_ALLOWED_JITTER = 10;


    boolean clockSynchronised = false;

    double maxJitter = 0;

    int numTicks = 0;

    // uncomment one of these if we want to display ticks
    //private final boolean SHOW_TICK = false;
    private final boolean SHOW_TICK = true;


    @Test
    public void testClock() {

        // let us define how many ticks we should expect

        double onDuration = TEST_TIMEOUT * 2 / 3;

        int expectedNumTicks = (int) onDuration / CLOCK_INTERVAL;


        Clock clock = new Clock(CLOCK_INTERVAL).addClockTickListener((offset, clock1) -> {
            double elapsed_time = HBScheduler.getGlobalScheduler().getElapsedTime();
            if (SHOW_TICK) {
                System.out.println("Tick " + (long) elapsed_time + " " + offset);
            }

            numTicks ++;
            if (!clockSynchronised){
                // we want out scheduled time to be spot on the 1000 mark
                double clock_shift = (elapsed_time - offset) % CLOCK_INTERVAL;

                // Now shift the clock
                clock1.shiftTime(clock_shift * -1);
                clockSynchronised = true;
            }

            if (maxJitter < Math.abs(offset)){
                maxJitter = Math.abs(offset);
            }
            // Let us shift
        });

        clock.start();


        try {
            Thread.sleep((long) onDuration);
            clock.stop();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long number_ticks = clock.getNumberTicks();

        try {
            Thread.sleep(TEST_TIMEOUT - (long)onDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check that our clock actually stopped
        assert (number_ticks == clock.getNumberTicks());

        // check our jitter is acceptable
        assert (maxJitter < MAX_ALLOWED_JITTER);

        System.out.println("Maximum jitter: " + maxJitter);
        System.out.println("Num Ticks: " + numTicks + " Expected Ticks:" + expectedNumTicks);

        // check that we have the expected number of ticks, allowing for a rounding in our expectation
        assert (Math.abs(expectedNumTicks - numTicks) <= 1);


        System.out.println("------------------- Clock testing complete -----------------------");
    }
}
