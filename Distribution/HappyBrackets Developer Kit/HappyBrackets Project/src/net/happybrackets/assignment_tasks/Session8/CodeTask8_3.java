/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.assignment_tasks.session8;

import net.beadsproject.beads.core.AudioContext;
import net.happybrackets.extras.assignment_autograding.BeadsChecker;

/**
 * In this task transform the 3-dimensional accelerometer data into force, which is a directionless unit that tells you
 * how much force is being applied to the device. No matter which way up the device is positioned, it should show the
 * same value.
 *
 * To do this we'll use pythagorus' theorem but in 3-D. That is, we'll calculate the length of the hypotenuse twice.
 *
 * Think of the x, y and z values as pointers pointing along each axis in 3 dimensions.
 * Then think of the distance from the centre out to the coordinate that each of these values locate.
 * This distance is the magnitude we are trying to calculate.
 *
 * This value should just show how much force is being applied to the device, so it should be almost constant as
 * it will be measuring gravity, and as long as you are resting the device on your hand or on the table, gravity will
 * be constant.
 *
 * However, it should show a zero value if you throw it in the air at the point at the top where it is weightless.
 *
 * To do pythagoras for 3-D, the algorithm is as follows
 * 1. Do pythagoras along x and y to find a third value, which we'll call h(x,y).
 * 2. Do pythagoras along h(x,y) and z to find the distance from the origin to (x,y,z).
 *
 *
 *
 */
public class CodeTask8_3 implements BeadsChecker.BeadsCheckable {

    public static void main(String[] args) {
        StringBuffer buf = new StringBuffer();

        float[][] accelData = {{0.6495126097972502f, -0.667230831773830f, 0.2882933721957966f},
                {0.5406543933735977f, -0.5594028557062594f, 0.13476435741697002f},
                {0.7096391970423858f, -0.6761561899718176f, 0.04971415716647718f},
                {0.6427241241405415f, -0.6199505559142244f, -0.11617577939025836f},
                {0.742854287576997f, -0.55361343417243f, -0.2950189916466312f},
                {0.8163154002192391f, -0.5702580210821894f, -0.3246655079640623f},
                {0.7040629409672321f, -0.429864548886828f, -0.5121857493865125f},
                {0.7091543052097637f, -0.3316856087089722f, -0.617033185143281f},
                {0.7770391617768523f, -0.2354364757090595f, -0.6228178712539992f},
                {0.8054053339852428f, -0.1384636650174182f, -0.7177831349049573f},
                {0.7692808924548993f, -0.2530459662077946f, -0.8026251978621584f},
                {0.6453910292199627f, -0.1526959929547487f, -0.8067226838572505f},
                {0.575324159406075f, -0.132433017586346f, -0.7580349090920385f},
                {0.4880436295341041f, -0.020745427162888f, -0.8296203997121769f},
                {0.5343507995495108f, -0.034736529202976f, -0.9185599486644702f},
                {0.48392204895681656f, -0.0470390499623633f, -0.9388063500519841f},
                {0.6124183846016626f, -0.0190568458821882f, -1.02967412770785f},
                {0.4262199208747913f, -0.11385862349864356f, -0.8590258874416614f},
                {0.6269651395803244f, -0.1408759239898471f, -0.7941891972840276f},
                {0.5173795854077388f, -0.4740088880822767f, -0.5760583251923599f},
                {0.5132580048304513f, -0.5750825390270471f, -0.4090255137453702f},
                {0.3925199385075581f, -1.029069677638164f, -0.21475647186041535f}};
        new CodeTask8_3().task(null, buf, new Object[]{accelData});
        System.out.println(buf);
    }

    @Override
    public void task(AudioContext ac, StringBuffer buf, Object... objects) {
        //********** do your work here ONLY **********
        //your objects...
        float[][] accelData = (float[][]) objects[0];
        float threshold = (float) objects[1];
        //do stuff here, remove the following line
        buf.append("Hello World!\n");
        //********** do your work here ONLY **********
    }
}
