package examples.rendererengine;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.rendererengine.Renderer;
import net.happybrackets.rendererengine.RendererController;

import java.lang.invoke.MethodHandles;

public class RendererTemplate extends Renderer {

    //    public static RendererController.RenderMode renderMode = RendererController.RenderMode.REAL;
    public static RendererController.RenderMode renderMode = RendererController.RenderMode.UNITY; //if working with unity sim
    public static String installationConfig = "config/2-pi-4-speakers.csv";
    public static int clockInterval = 50; //gap between ticks
    public static int oscPost = 5555; //default listening port for the device carrying this renderer

    public void setupAudio(){
        WavePlayer wp = new WavePlayer(300 + 200 * x, Buffer.SINE);
        out.addInput(wp);
        ((Gain)out).setGain(0.1f);
    }

    public void setupLight(){
        rc.LEDsRefreshMode = RendererController.EightLEDsMode.ONE_COMMAND_FOR_8_LEDS; //for a weird hack on biotica board to make other ports work
        displayColor(0,0,0);
    }

    public void tick(Clock clock){ //frame rate updates.
        hb.setStatus( "ticks: " + clock.getNumberTicks());
    }

    //main method for debugging when running in intelliJ
    public static void main(String[] args) {
        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
