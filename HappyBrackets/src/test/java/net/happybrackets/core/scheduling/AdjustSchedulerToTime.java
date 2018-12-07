package net.happybrackets.core.scheduling;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class AdjustSchedulerToTime {

    final int TEST_TIMEOUT = 1000;

    final double RESHEDULE_AMOUNT = 150;

    final LocalDateTime REFERENCE_TIME = LocalDateTime.ofEpochSecond(1543622400, 0, ZoneOffset.UTC);


    /**
     * Get the calculated time in ms from 1 Dec
     * @return number of milliseconds since 1 Dec 2018 GMT
     */
    double getCalcTime(){
        // now get current time in GMT
        final LocalDateTime gmt = LocalDateTime.now(ZoneOffset.UTC);

        Duration gmt_diff =  Duration.between(REFERENCE_TIME, gmt);
        double calc_gmt = gmt_diff.toNanos() / 1000000d;
        return calc_gmt;
    }

    @Test
    public void adjustScheduler() {

        // cal at start to prime JIT
        getCalcTime();
        // 1 December 2018
        final LocalDateTime initial = REFERENCE_TIME;

        // just do some checks to see that days are not rounded up or down
        final LocalDateTime hours_11 = LocalDateTime.ofEpochSecond(1543662000, 0, ZoneOffset.UTC);
        final LocalDateTime hours_13 = LocalDateTime.ofEpochSecond(1543669200, 0, ZoneOffset.UTC);
        final LocalDateTime hours_23 = LocalDateTime.ofEpochSecond(1543705200, 0, ZoneOffset.UTC);
        final LocalDateTime hours_24 = LocalDateTime.ofEpochSecond(1543708800, 0, ZoneOffset.UTC);



        Duration period = Duration.between(initial, hours_11);
        long day_diff = period.toDays();
        assert (day_diff == 0);

        period = Duration.between(initial, hours_13);
        day_diff = period.toDays();
        long hour_diff = period.toHours();
        System.out.println(hour_diff);
        assert (day_diff == 0);

        period = Duration.between(initial, hours_23);
        day_diff = period.toDays();
        assert (day_diff == 0);

        period = Duration.between(initial, hours_24);
        day_diff = period.toDays();
        assert (day_diff == 1);


        // Do Java calculation of date diff
        double calc_gmt = getCalcTime();
        // now get current time in GMT
        final LocalDateTime gmt = LocalDateTime.now(ZoneOffset.UTC);

        Duration gmt_diff =  Duration.between(initial, gmt);

        // do manual calculation of date diff
        day_diff = gmt_diff.toDays();
        double day = day_diff * 24d * 60d * 60d * 1000d;

        int second = gmt.getSecond();
        int hour = gmt.getHour();
        int min = gmt.getMinute();
        double nano = gmt.getNano();

        double total = nano / 1000000d;
        total += second * 1000d;
        total += min * 60000d;
        total += hour * 60d * 60000d;
        total += day;


        double diff = calc_gmt - total;

        // make sure times are less than 1
        assert (Math.abs(diff) < 1);

        System.out.println(diff);

        HBScheduler scheduler = HBScheduler.getGlobalScheduler();

        // let us define how many ticks we should expect

        double onDuration = TEST_TIMEOUT * 2 / 3;

        try {
            Thread.sleep((long)RESHEDULE_AMOUNT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double schedule_diff = HBScheduler.getGlobalScheduler().getClockSkew();

        assert (Math.abs(schedule_diff) > 10);

        scheduler.synchroniseClocks();
        // run twice to account for JIT delay
        scheduler.synchroniseClocks();

        System.out.println("Old Diff = " + schedule_diff);


        //clock.start();
        try {
            Thread.sleep((long) onDuration - (long)RESHEDULE_AMOUNT);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        schedule_diff = schedule_diff = HBScheduler.getGlobalScheduler().getClockSkew();

        System.out.println("New Schedule diff " + schedule_diff);
        assert (Math.abs(schedule_diff) < 10);
        System.out.println("------------------- Schedule Adjust complete -----------------------");
    }
}
