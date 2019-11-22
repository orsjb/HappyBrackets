package net.happybrackets.core.scheduling;

import org.junit.Test;

public class MultiClockListenerTest {

    final int TEST_TIMEOUT = 10000;


    @Test
    public void testClock() {

Clock testclock1 = new Clock(100);
Clock testclock2 = new Clock(37);

Clock.ClockTickListener clockTickListener = (offset, clock) -> {
    double clock_interval = clock.getClockInterval();
    long num_ticks =  clock.getNumberTicks();

    System.out.println("Clock interval " + clock_interval + " has had " + num_ticks + " ticks");
};

// Now add listener to both clocks
testclock1.addClockTickListener(clockTickListener);
testclock2.addClockTickListener(clockTickListener);

testclock1.start();
testclock2.start();


        try {
            Thread.sleep(TEST_TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
