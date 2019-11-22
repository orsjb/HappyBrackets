package net.happybrackets.core.scheduling;

import org.junit.Test;

public class ClockMultiListenerTest {

    final int TEST_TIMEOUT = 10;


    @Test
    public void testClock() {


        final int TICKS_PER_BEAT = 4;
        double interval = Clock.BPM2Interval(120 * TICKS_PER_BEAT);

        Clock testclock1 = new Clock(interval);

        // create a listener for each Beat
        testclock1.addClockTickListener((offset, clock) -> {
            if (clock.getNumberTicks() % TICKS_PER_BEAT == 0){
                System.out.println("Beat " + clock.getNumberTicks() / TICKS_PER_BEAT);
            }

        });

        testclock1.start();

        testclock1.addClockTickListener((offset, clock) -> {

        });

        // We can even add listeners after clock is started
        // Create a Listener for every Tick not on the beat
        testclock1.addClockTickListener((offset, clock) -> {
            long tickNum = clock.getNumberTicks() % TICKS_PER_BEAT;
            System.out.println("tick " + tickNum);
        });


        try {
            Thread.sleep(TEST_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
