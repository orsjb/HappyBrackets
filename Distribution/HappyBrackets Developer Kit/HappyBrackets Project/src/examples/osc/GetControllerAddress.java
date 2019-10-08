package examples.osc;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;

/**
 * Displays the IP Address of thr controller that sent this code
 */
public class GetControllerAddress implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        //Write your sketch below

        // Get the IP Address of controller that sent this to us
        InetAddress controllerAddress =  hb.getSendingController(this);

        if (controllerAddress != null){
            String ipAddress =  controllerAddress.getHostAddress();
            hb.setStatus(ipAddress);
        }
        // write your code above this line
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
