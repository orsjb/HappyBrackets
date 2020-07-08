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
        String buffer;

        String strLocation = joBoid.getString("location");
        buffer = "482.6318900962165,355.0863414835118";
        assert (strLocation.equals(buffer));

        String strVelocity = joBoid.getString("velocity");
        buffer = "2.973183192608333,-0.13836703488023905";
        assert (strVelocity.equals(buffer));

        String strAcceleration = joBoid.getString("acceleration");
        buffer = "-0.0,0.0";
        assert (strAcceleration.equals(buffer));

        double value = myModel.getDiadIntensity();
        assert(value == 4.839886197253851E-31);
    }
}
