package net.happybrackets.core.control;

import org.junit.Test;

/**
 * Test Conversion of LONGs to Integers and the two integers back to LONGS
 */
public class TestConversions {

    final long TEST_1_VAL = Long.MAX_VALUE;
    final long TEST_2_VAL = Long.MIN_VALUE;
    final long TEST_3_VAL = Long.MAX_VALUE / 2;
    final long TEST_4_VAL = Long.MIN_VALUE / 2;

    final long TEST_5_VAL = Integer.MAX_VALUE;
    final long TEST_6_VAL = Integer.MIN_VALUE;
    final long TEST_7_VAL = Integer.MAX_VALUE / 2;
    final long TEST_8_VAL = Integer.MIN_VALUE / 2;

    final long TEST_9_VAL = -1;
    final long TEST_10_VAL = 1;
    final long TEST_11_VAL = 0;


    final double PRECISION = 0.000001d;

    final long [] TEST_LONGS = new long[]{TEST_1_VAL, TEST_2_VAL, TEST_3_VAL, TEST_4_VAL,
            TEST_5_VAL, TEST_6_VAL, TEST_7_VAL, TEST_8_VAL, TEST_9_VAL, TEST_10_VAL, TEST_11_VAL};

    final double [] TEST_NANOS = new double[]{0.0, 0.1, 0.0001, 0.000001};

    @Test
    public void testConversions(){

        for (int i = 0; i < TEST_LONGS.length; i++){
            long l_val = TEST_LONGS[i];

            System.out.println("Test long " + l_val);
            int [] i_vals = DynamicControl.longToIntegers(l_val);

            long test_val = DynamicControl.integersToLong(i_vals[0], i_vals[1]);

            assert (l_val == test_val);
        }

        for (int i = 0; i < TEST_LONGS.length; i++){
            long l_val = TEST_LONGS[i];

            for (int j = 0; j < TEST_NANOS.length; j++){
                double d_val = l_val;
                d_val += TEST_NANOS [j];


                int [] i_vals = DynamicControl.scheduleTimeToIntegers(d_val);
                double test_val = DynamicControl.integersToScheduleTime(i_vals[0], i_vals[1], i_vals[2]);

                double diff = Math.abs(d_val - test_val);


                System.out.println("Test Double " + d_val + " " + test_val) ;
                assert (diff <= PRECISION);

            }




        }

    }
}
