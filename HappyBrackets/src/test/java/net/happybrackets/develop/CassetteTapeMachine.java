package net.happybrackets.develop;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.device.sensors.*;
import net.happybrackets.device.sensors.sensor_types.AccelerometerSensor;
import net.happybrackets.device.sensors.sensor_types.GyroscopeSensor;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;


public class CassetteTapeMachine implements HBAction {

//    final static int FFSPEED = 8;
    final static float FFSPEED = 0.08f;
    final static float FFATTEN = 0.5f;

    enum State {
        PLAY, STOP, FREE, REV, FF, RW;
    }

    long timeOfLastStopToPlay = 0, timeOfLastPlayToStop = 0;
    float newLoopStart = 0;

    State currentState = State.STOP, previousState = State.STOP;
    Glide rateEnv, rateMod, gainEnv, loopStart, loopEnd;
    SamplePlayer sp;

    LinkedList<double[]> sensorHistory;

    int count = 0;

    @Override
    public void action(HB hb) {
        hb.reset();
        //audio stuff
        gainEnv = new Glide(hb.ac, 1f, 500);
        Gain g = new Gain(hb.ac, 1, gainEnv);
        rateEnv = new Glide(hb.ac, 0, 200);
        rateMod = new Glide(hb.ac, 0, 200);
        Function rate = new Function(rateEnv, rateMod) {
            @Override
            public float calculate() {
                return x[0] + x[1];
            }
        };
        SampleManager.setVerbose(true);

        String sample_name = "data/audio/Nylon_Guitar/Clean_A_harm.wav";
        //sample_name = "data/audio/hiphop.wav";


        Sample sample = SampleManager.sample(sample_name);


        System.out.println("Is sample loaded or null? " + sample_name);
        if (sample == null)
        {
            hb.setStatus("Unable to load sample " );
        }
        else {
            sp = new SamplePlayer(hb.ac, sample);
            sp.setRate(rate);
            sp.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
            loopStart = new Glide(hb.ac, 0, 500);
            loopEnd = new Glide(hb.ac, (float) sp.getSample().getLength(), 500);
            sp.setLoopStart(loopStart);
            sp.setLoopEnd(loopEnd);
            g.addInput(sp);
            BiquadFilter bf = new BiquadFilter(hb.ac, 1, BiquadFilter.HP);
            bf.setFrequency(100);
            bf.addInput(g);
            hb.sound(bf);
            //sensor averaging
            sensorHistory = new LinkedList<double[]>();
            for (int i = 0; i < 10; i++) {
                sensorHistory.add(new double[]{0, 0, 0});
            }
        }
        //set up sensor
//        AccelerometerListener sensor = (MiniMU)hb.getSensor(MiniMU.class);
        Accelerometer accel_sensor = (Accelerometer)hb.getSensor(Accelerometer.class);
        if (accel_sensor != null) {
            accel_sensor.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    count++;
                    //state stuff, with averaging
                    double[] accel = accel_sensor.getAccelerometerData();
                    sensorHistory.removeFirst();
                    sensorHistory.add(accel);
                    double xsmooth = 0, ysmooth = 0, zsmooth = 0;
                    for (double[] histValue : sensorHistory) {
                        xsmooth += histValue[0] / sensorHistory.size();
                        ysmooth += histValue[1] / sensorHistory.size();
                        zsmooth += histValue[2] / sensorHistory.size();
                    }
                    if (count % 1 == 0) {
//                    System.out.println(xsmooth + " " + ysmooth + " " + zsmooth);
                    }
                    if ((Math.abs(xsmooth) > Math.abs(ysmooth)) && (Math.abs(xsmooth) > Math.abs(zsmooth))) {
                        if (xsmooth > 0) currentState = State.FF;
                        else currentState = State.RW;
                    } else if (Math.abs(ysmooth) > Math.abs(zsmooth)) {
                        if (ysmooth > 0) currentState = State.PLAY;
                        else currentState = State.FREE;
                    } else {
                        if (zsmooth > 0) currentState = State.STOP;
                        else currentState = State.REV;
                    }

                    if (currentState != previousState) {
                        changeState();
                    }

                }
            });
        }

        Gyroscope gyroscope = (Gyroscope)hb.getSensor(Gyroscope.class);

        if (gyroscope != null){
            gyroscope.addListener(new SensorUpdateListener() {
                @Override
                public void sensorUpdated() {
                    double[] gyr = gyroscope.getGyroscopeData();
                    //magnitude
                    double mag = Math.sqrt(gyr[0] * gyr[0] + gyr[1] * gyr[1] + gyr[2] * gyr[2]);
                    double thresh = 2;
                    System.out.println(mag);
                    if (mag > thresh) rateMod.setValue((float) (mag - thresh) / 2f);
                    else rateMod.setValue(0);
                }
            });
        }
    }

    public void changeState() {
        System.out.println("State changed to " + currentState);
        switch(currentState) {
            case RW:
                rateEnv.setValue(-FFSPEED);
                gainEnv.setValue(FFATTEN);
                break;
            case FF:
                rateEnv.setValue(FFSPEED);
                gainEnv.setValue(FFATTEN);
                break;
            case PLAY:
                rateEnv.setValue(1);
                gainEnv.setValue(1f);
                break;
            case STOP:
                rateEnv.setValue(0);
                gainEnv.setValue(1);
                break;
            case REV:
                rateEnv.setValue(-1);
                gainEnv.setValue(1f);
                break;
        }
        if(previousState == State.STOP && currentState == State.PLAY) {
            System.out.println("STOP TO PLAY TRANSITION");
            if(timeOfLastPlayToStop != 0 && timeOfLastStopToPlay != 0) {
                float duration = timeOfLastPlayToStop - timeOfLastStopToPlay;
                loopStart.setValue(newLoopStart);
                loopEnd.setValue(newLoopStart + duration);
                System.out.println("Setting loop start " + newLoopStart + ", loop end " + (newLoopStart + duration));
                newLoopStart = (float)sp.getPosition();
            }
            timeOfLastStopToPlay = System.currentTimeMillis();
        } else if(previousState == State.PLAY && currentState == State.STOP) {
            System.out.println("PLAY TO STOP TRANSITION");
            timeOfLastPlayToStop = System.currentTimeMillis();
        }
        previousState = currentState;

    }

    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
