package examples.rendererengine;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.Device;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.instruments.SampleModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.rendererengine.*;

import java.lang.invoke.MethodHandles;

/**
 * This sketch shows how to extends the Renderer classes in the new Renderer Engine.
 */
public class RendererExample extends Renderer implements HBAction {

    /**
     * Replace de default interval parameter of the RendererController internal clock. Default is 50.
     */
    public static int clockInterval = 50;

    /**
     * Replace de default interval parameter of the RendererController for the OSC receiver for HB Annotations. Default is 9002.
     */
    public static int oscPort = 6000;

    /**
     * Determined the path of the hardware configuration file to be loaded by the RendererControlle.loadHardwareConfiguration()
     */
    public static String installationConfig = "config/hardware_setup_casula.csv";

    /**
     * Define the rendering mode.
     */
    public static RendererController.RenderMode renderMode = RendererController.RenderMode.UNITY;

    /**
     * This field becomes automatically exposed to OSC control > i.e., the message "/armX 0.5" would set the value of armX.
     * It can handles the following type: String, OSCMessage, float, int and boolean.
     */
    @HBParam
    public float armX;

    /**
     * Same as @HBParam but restricted to number types and controlling the min/max values sent to this field.
     */
    @HBNumberRange(min = 0, max = 100)
    public int test;

    private boolean step1Finished = false;
    private boolean step2Finished = false;

    public RendererExample() {
    }

    /**
     * This works too as an OSC message.
     * A function that runs when the message someOtherThing is sent on port 9001 from somewhere else
     * @param message It accepts any number of parameters from the following type: String, OSCMessage, float, int and boolean.
     */
    @HBCommand
    public void someOtherThing(OSCMessage message) {

        String s = "someOtherThing " + message.getArg(0);
        System.out.println(s);
    }

    /**
     * When extending Renderer, a setupAudio can be define to set anything that needs to be set for a Audio/Speaker Renderer
     * To be executed once when a Light renderer is added to the RendererController list.
     */
    @Override
    public void setupAudio() {
        System.out.println("setupAudio: |" + this.name + "|");
        final String sample_name = "data/audio/long/1979.wav";
        SampleModule sampleModule = new SampleModule();
        if (this.name.contains("Group 1-Outer_Section_1-2_1 Speaker 1") && sampleModule.setSample(sample_name)) {
            sampleModule.connectTo(out);
            sampleModule.setGainValue(0.2f);
            sampleModule.setLoopStart(0);
            sampleModule.setLoopEnd(20000);
        }

    }

    /**
     * When extending Renderer, a setupLight can be define to set anything that needs to be set for a Light Renderer
     * To be executed once when a Light renderer is added to the RendererController list.
     */
    @Override
    public void setupLight() {
        System.out.println("setupLight: " + this.name);
        rc.displayColor(this, 255,255,255);
        colorMode(ColorMode.RGB, 255);
    }

    /**
     * The tick method will be called for each renderer every time a tick of the internal clock happens.
     */
    @Override
    public void tick(Clock clock) {
        super.tick(clock);
        System.out.println("tick: " + clock.getNumberTicks() + " device: " + this.name);

        if (type == Type.LIGHT) {
            if (rgb[0] < 50 && !step1Finished) {
                changeBrigthness(2);
            } else {
                step1Finished = true;
            }
            if (step1Finished && !step2Finished) {
                changeHue(2);
                if (rgb[0] > 250 && rgb[1] < 4 && rgb[2] < 4) {
                    step2Finished = true;
                }
            }
            if (step1Finished && step2Finished) {
                changeBrigthness(-2);
                if (rgb[0] == 0) {
                    rgb[0] = 1;
                    step1Finished = step2Finished = false;
                }
            }

            if (id == 0) {
                //System.out.println(id + " - red: " + rgb[0] + " green: " + rgb[1] + " blue: " + rgb[2]);
                //System.out.println(step1Finished +   " " + step2Finished);
            }

            // After calculating the new color. Push it to the serial 'queue'
            rc.pushLightColor(this);
        }
    }

    /**
     * To implements HBAction is optional. You can implements HBAction if your Renderer needs to setup any special code.
     * This will be called once, after the RendererController setup() (which adds renderers and calls setupAudio / setupLight).
     * @param hb singleton HB
     */
    @Override
    public void action(HB hb) {
        String name = Device.getDeviceName();
        rc.addRenderer(Renderer.Type.LIGHT, name,   63f,  100f, 214f, "LED-W", 0);
        rc.addRenderer(Renderer.Type.LIGHT, name,   214f, 100f, 365f,  "LED-N", 1);
        rc.addRenderer(Renderer.Type.LIGHT, name,   214f, 100f, 63f,  "LED-S", 2);
        rc.addRenderer(Renderer.Type.LIGHT, name,   365f, 100f, 214f,  "LED-E", 3);
        rc.addRenderer(Renderer.Type.SPEAKER, name, 63f,  100f, 214f, "SPEAKER-W", 0);
        rc.addRenderer(Renderer.Type.SPEAKER, name, 365f, 100f, 214f, "SPEAKER-E", 1);
        hb.setStatus("I Have " + rc.renderers.size() + " objects");
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">
    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

}
