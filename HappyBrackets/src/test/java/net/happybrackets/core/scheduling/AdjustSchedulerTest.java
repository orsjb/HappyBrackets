package net.happybrackets.core.scheduling;

import org.junit.Test;

public class AdjustSchedulerTest {

    final int TEST_TIMEOUT = 1000;

    final double CLOCK_INTERVAL = Math.random() * 100 + 10;

    final double MAX_ALLOWED_JITTER = 20;

    final double RESHEDULE_AMOUNT = 150;

    double maxJitter = 0;

    int numTicks = 0;

    // uncomment one of these if we want to display ticks
    //private final boolean SHOW_TICK = false;
    private final boolean SHOW_TICK = true;


    @Test
    public void adjustScheduler() {

        HBScheduler scheduler = HBScheduler.getGlobalScheduler();

        // let us define how many ticks we should expect

        double onDuration = TEST_TIMEOUT * 2 / 3;

         int expectedNumTicks = (int) (onDuration / CLOCK_INTERVAL) ;


        Clock clock = new Clock(CLOCK_INTERVAL).addClockTickListener((offset, clock1) -> {
            double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime();
            if (SHOW_TICK) {
                System.out.println("Tick " + (long) elapsed_time + " " + offset);
            }

            numTicks ++;

            if (maxJitter < Math.abs(offset)){
                maxJitter = Math.abs(offset);
            }


            // Let us shift
        });

        clock.start();


        try {
            Thread.sleep((long)RESHEDULE_AMOUNT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduler.adjustScheduleTime(-CLOCK_INTERVAL, 10);




        try {
            Thread.sleep((long) onDuration - (long)RESHEDULE_AMOUNT);
            clock.stop();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(TEST_TIMEOUT - (long)onDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long number_ticks = clock.getNumberTicks();

        // check that our clock actually stopped
        assert (number_ticks == clock.getNumberTicks());

        // check our jitter is acceptable
        assert (maxJitter < MAX_ALLOWED_JITTER);

        System.out.println("Maximum jitter: " + maxJitter);
        System.out.println("Num Ticks: " + numTicks + " Expected Ticks:" + expectedNumTicks);

        // check that we have the expected number of ticks, allowing for a rounding in our expectation
        assert (Math.abs(expectedNumTicks - numTicks) <= 1);


        System.out.println("------------------- Schedule Adjust complete -----------------------");
    }
}
