package net.happybrackets.core.scheduling;

import org.junit.Test;

public class ClockTest {

    final int TEST_TIMEOUT = 1000;

    final double MAX_ALLOWED_JITTER = 10;


    boolean clockSynchronised = false;

    double maxJitter = 0;

    int numTicks = 0;

    // uncomment one of these if we want to display ticks
    //private final boolean SHOW_TICK = false;
    private final boolean SHOW_TICK = true;

    // JUst set to 1 unless we are doing exhaustive test
    final int _MAX_TESTS = 1;

    @Test
    public void testClock() {

        // check our static conversion functions

        double interval = Clock.BPM2Interval(120);

        assert (interval == 500);

        double BPM = Clock.Interval2BPM(interval);
        System.out.println("" + BPM);
        assert (BPM == 120);
        // let us define how many ticks we should expect

        double onDuration = TEST_TIMEOUT * 2 / 3;

        for (int i = 1; i <= _MAX_TESTS; i++) {
            final double CLOCK_INTERVAL =   Math.random() * 100 + 10;

            System.out.println("------------------- Clock test " + i + " Interval: " + (long) CLOCK_INTERVAL + " -----------------------");
            numTicks = 0;
            int expectedNumTicks = (int) (onDuration / CLOCK_INTERVAL);


            Clock testclock1 = new Clock(CLOCK_INTERVAL);
            testclock1.addClockTickListener((offset, clock) -> {
                double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime();
                if (SHOW_TICK) {
                    System.out.println("Tick " + (long) elapsed_time + " " + offset);
                }

                numTicks++;
                if (!clockSynchronised) {
                    // we want out scheduled time to be spot on the 1000 mark
                    double clock_shift = (elapsed_time - offset) % CLOCK_INTERVAL;

                    // Now shift the clock
                    clock.shiftTime(clock_shift * -1);
                    clockSynchronised = true;
                }

                if (maxJitter < Math.abs(offset)) {
                    maxJitter = Math.abs(offset);
                }
                // Let us shift
            });

            testclock1.start();

            // test if we can stop clock in context of their callback
            Clock testclock2 = new Clock(CLOCK_INTERVAL).addClockTickListener((offset, clock) -> {
                clock.stop();
            });

            testclock2.start();

            try {
                Thread.sleep((long) onDuration);
                testclock1.stop();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long number_ticks = testclock1.getNumberTicks();

            try {
                Thread.sleep(TEST_TIMEOUT - (long) onDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // check that our clock actually stopped
            assert (number_ticks == testclock1.getNumberTicks());

            assert (testclock2.getNumberTicks() == 1);

            // check our jitter is acceptable
            assert (maxJitter < MAX_ALLOWED_JITTER);

            System.out.println("Maximum jitter: " + maxJitter);
            System.out.println("Num Ticks: " + numTicks + " Expected Ticks:" + expectedNumTicks);

            // check that we have the expected number of ticks, allowing for a rounding in our expectation
            assert (Math.abs(expectedNumTicks - numTicks) <= 1);
        }

        System.out.println("------------------- Clock testing complete -----------------------");
    }
}
