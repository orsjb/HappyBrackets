package net.happybrackets.core.scheduling;

import org.junit.Test;

public class ClockSycnhroniseTest {

    final int TEST_TIMEOUT = 10000;

    final double CLOCK_INTERVAL =   100;


    double maxJitter = 0;

    int numTicks = 0;

    // uncomment one of these if we want to display ticks
    //private final boolean SHOW_TICK = false;
    private final boolean SHOW_TICK = true;

    double clock_1_time = 0;
    double clock_2_time = 0;

    // we will wait on this and notify when we have done two beats
    final Object completeWait = new Object();

    @Test
    public void testClockSynchronisation() {


        Clock mastClock = new Clock(CLOCK_INTERVAL).addClockTickListener((offset, clock) -> {
            double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime();

            clock_1_time = elapsed_time + offset;

            if (SHOW_TICK) {
                System.out.println("Tick 1 " + (long) elapsed_time + " " + offset);
            }


            if (maxJitter < Math.abs(offset)){
                maxJitter = Math.abs(offset);
            }
            // Let us shift
        });

        mastClock.start();

        // test if we can stop clock in context of their callback
        Clock slaveClock = new Clock(CLOCK_INTERVAL).addClockTickListener((offset, clock) -> {
            double elapsed_time = HBScheduler.getGlobalScheduler().getSchedulerTime();

            clock_2_time = elapsed_time + offset;

            if (SHOW_TICK) {
                System.out.println("Tick 2 " + (long) elapsed_time + " " + offset);
            }

            numTicks ++;

            if (numTicks == 3 || numTicks == 8){
                // ok - we will synchronise now
                synchronized (completeWait){
                    completeWait.notify();
                }
            }

        });

        // make sure our clocks are not equal
        try {
            Thread.sleep((long)CLOCK_INTERVAL / 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        slaveClock.start();

        // lets wait for two clock beats, which is when the event will be hit
        synchronized (completeWait){
            try {
                completeWait.wait(TEST_TIMEOUT);
            } catch (InterruptedException e) {

            }
        }


        System.out.println(Math.abs(clock_1_time) + " " + Math.abs(clock_2_time) );
        assert (Math.abs(clock_1_time - clock_2_time) > CLOCK_INTERVAL / 4);
        System.out.println("Do Synch");
        slaveClock.synchronizeClock(mastClock);


        // now wait a couple of beats and make sure they are now synchronised
        synchronized (completeWait){
            try {
                completeWait.wait(TEST_TIMEOUT);
            } catch (InterruptedException e) {

            }
        }

        // make sure our clocks are  at same point in time
        System.out.println( (long)clock_1_time + " " + (long) clock_2_time);
        assert (Math.abs(clock_1_time) - Math.abs(clock_2_time) < 2);

        System.out.println("Num Ticks: " + numTicks);


        System.out.println("-------------------  testClockSynchronisation complete -----------------------");
    }
}
