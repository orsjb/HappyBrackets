package net.happybrackets.develop;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.LSM9DS1;
import net.happybrackets.device.sensors.Sensor;
import net.happybrackets.device.sensors.SensorUpdateListener;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;

import java.util.LinkedList;

public class OrientationDrone2 implements HBAction {


    int state = 0;
    LinkedList<double[]> sensorHistory;

    @Override
    public void action(HB hb) {



        hb.reset();
        hb.testBleep();
        System.out.println("OrientationDrone2");

        sensorHistory = new LinkedList<double[]>();
        for(int i = 0; i < 10; i++) {
            sensorHistory.add(new double[] {0,0,0});
        }

        float level = 0.5f;
        float fadeout = 1000;
        float fadein = 10;

        //load a set of sounds
        Sample d1 = SampleManager.sample("data/audio/drone1.wav");
        Sample d2 = SampleManager.sample("data/audio/drone2.wav");
        Sample d3 = SampleManager.sample("data/audio/drone3.wav");

        Glide mod1 = new Glide(hb.ac, 40, 400);
//        Glide mod2 = new Glide(hb.ac, 40);

        GranularSamplePlayer sp1 = new GranularSamplePlayer(hb.ac, d1);
        GranularSamplePlayer sp2 = new GranularSamplePlayer(hb.ac, d2);
        GranularSamplePlayer sp3 = new GranularSamplePlayer(hb.ac, d3);
        sp1.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        sp2.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        sp3.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);

        sp3.getRandomnessUGen().setValue(1);

        sp1.getLoopStartUGen().setValue(100);
        sp2.getLoopStartUGen().setValue(100);
        sp3.getLoopStartUGen().setValue(100);

        sp1.getLoopEndUGen().setValue(500);
        sp2.getLoopEndUGen().setValue(500);
        sp3.getLoopEndUGen().setValue(500);

        sp1.getPitchUGen().setValue(8);
        sp2.getPitchUGen().setValue(3);
        sp3.getPitchUGen().setValue(4);

        sp1.getRateUGen().setValue(0.001f);
        sp2.getRateUGen().setValue(0.001f);
        sp3.getRateUGen().setValue(0.001f);

        sp1.setGrainInterval(mod1);
        sp2.setGrainInterval(mod1);
        sp3.setGrainInterval(mod1);


//        sp1.setGrainSize(mod2);
//        sp2.setGrainSize(mod2);
//        sp3.setGrainSize(mod2);

        Reverb rb = new Reverb(hb.ac, 1);
        hb.sound(rb);

        Envelope genv1 = new Envelope(hb.ac, 0);
        Gain g1 = new Gain(hb.ac, 1, genv1);
        g1.addInput(sp1);
        rb.addInput(g1);
        hb.sound(g1);
        Envelope genv2 = new Envelope(hb.ac, 0);
        Gain g2 = new Gain(hb.ac, 1, genv2);
        g2.addInput(sp2);
//        rb.addInput(g2);
//        hb.sound(g2);
        Envelope genv3 = new Envelope(hb.ac, 0);
        Gain g3 = new Gain(hb.ac, 1, genv3);
        g3.addInput(sp3);
//        rb.addInput(g3);
//        hb.sound(g3);

        Sensor sensor = (LSM9DS1)hb.getSensor(LSM9DS1.class);
        sensor.addListener(new SensorUpdateListener() {
            @Override
            public void sensorUpdated() {
                //state stuff, with averaging
                double[] accel = ((AccelerometerSensor)sensor).getAccelerometerData();
                sensorHistory.removeFirst();
                sensorHistory.add(accel);
                double xsmooth = 0, ysmooth = 0, zsmooth = 0;
                for(double[] histValue : sensorHistory) {
                    xsmooth += histValue[0] / sensorHistory.size();
                    ysmooth += histValue[1] / sensorHistory.size();
                    zsmooth += histValue[2] / sensorHistory.size();
                }
                double mag = Math.sqrt(zsmooth*zsmooth + ysmooth*ysmooth + xsmooth*xsmooth);
//                System.out.println("Mag: " + mag + " " +zsmooth+ " "+ysmooth+" " + xsmooth );
                //choose sound based on orientation
                if ((Math.abs(xsmooth) > Math.abs(ysmooth)) && (Math.abs(xsmooth) > Math.abs(zsmooth))) {
                    if (xsmooth > 0) {
                        if(state != 1) {
                            genv1.addSegment(level, fadein);
                            genv2.addSegment(0, fadeout);
                            genv3.addSegment(0, fadeout);
                            System.out.println("Dir 1");
                            sp1.getPitchUGen().setValue(8);
                            state = 1;
                        }
                    }
                    else {
                        if(state != 2) {
                            genv1.addSegment(0, fadeout);
                            genv2.addSegment(0, fadeout);
                            genv3.addSegment(0, fadeout);
                            System.out.println("Dir 2");
                            sp1.getPitchUGen().setValue(2);
                            state = 2;
                        }
                    }
                } else if (Math.abs(ysmooth) > Math.abs(zsmooth)) {
                    if (ysmooth > 0) {
                        if(state != 3) {
                            genv1.addSegment(level, fadeout);
                            genv2.addSegment(level, fadein);
                            genv3.addSegment(0, fadeout);
                            System.out.println("Dir 3");
                            sp1.getPitchUGen().setValue(3);
                            state = 3;
                        }
                    }
                    else {
                        if(state != 4) {
                            genv1.addSegment(level, fadeout);
                            genv2.addSegment(level, fadein);
                            genv3.addSegment(0, fadeout);
                            System.out.println("Dir 4");
                            sp1.getPitchUGen().setValue(4);
                            state = 4;
                        }
                    }
                } else {
                    if (zsmooth > 0) {
                        if(state != 5) {
                            genv1.addSegment(level, fadeout);
                            genv2.addSegment(0, fadeout);
                            genv3.addSegment(level, fadein);
                            System.out.println("Dir 5");
                            sp1.getPitchUGen().setValue(2);
                            state = 5;
                        }
                    }
                    else {
                        if(state != 6) {
                            genv1.addSegment(0, fadeout);
                            genv2.addSegment(0, fadeout);
                            genv3.addSegment(level, fadein);
                            System.out.println("Dir 6");
                            sp1.getPitchUGen().setValue(6);
                            state = 6;
                        }
                    }
                }
                //modify other aspects
//                mod1.setValue((float)mag * 500f + 10f);
                mod1.setValue((float)Math.max(0, (mag - 1)) * 500f + 40f);

                //threshold the mag
                if(mag > 2) {

                }

            }
        });


    }


}
