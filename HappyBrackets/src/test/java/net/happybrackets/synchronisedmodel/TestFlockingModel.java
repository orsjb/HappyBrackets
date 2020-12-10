package net.happybrackets.synchronisedmodel;

import net.happybrackets.sychronisedmodel.Diad2d;
import net.happybrackets.sychronisedmodel.FlockingModel;

import org.json.JSONObject;
import org.junit.Test;

/**
 * Test that the Flocking Model run and returns the expected intensity.
 */

public class TestFlockingModel {

    @Test
    public void testThisFlockingModel() {


        FlockingModel myModel = new FlockingModel();  // a SynchronisedModel type
        //myModel.setup(this, hb);
        myModel.setup2DSpaceSize(600, 400);
        myModel.setupFlock(10, 0);

        myModel.addDiad(new Diad2d("Diad-3", 460, 310));
        myModel.addDiad(new Diad2d("Diad-1", 120, 90));
        myModel.addDiad(new Diad2d("Diad-2", 120, 310));
        myModel.addDiad(new Diad2d("Diad-4", 460, 90));

        myModel.start();

        JSONObject jo = null;

        if(myModel.isRunning()) {
            for (int i = 0; i < 633; i++) {
                myModel.update();
                jo = myModel.exportModelState();
                myModel.flock.resetBoids(0);
                myModel.importModelState(jo);
            }
        }

        assert(myModel.getFrameCount() == 633);

        JSONObject joBoid = (JSONObject) jo.get("5");

        double lx = joBoid.getDouble("lx");
        double ly = joBoid.getDouble("ly");

        double vx = joBoid.getDouble("vx");
        double vy = joBoid.getDouble("vy");

        assert (lx == 482.6318900962165d);
        assert (ly == 355.0863414835118d);

        assert (vx == 2.973183192608333d);
        assert (vy == -0.13836703488023905d);


        double value = myModel.getDiadIntensity();
        assert(value == 4.839886197253851E-31);
    }
}
