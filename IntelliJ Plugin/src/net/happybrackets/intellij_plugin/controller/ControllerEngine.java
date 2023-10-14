package net.happybrackets.intellij_plugin.controller;

import net.happybrackets.core.control.ControlMap;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.scheduling.HBScheduler;
import net.happybrackets.intellij_plugin.controller.config.ControllerConfig;
import net.happybrackets.intellij_plugin.controller.config.ControllerSettings;
import net.happybrackets.intellij_plugin.controller.http.FileServer;
import net.happybrackets.intellij_plugin.controller.network.ControllerAdvertiser;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ControllerEngine {

    final static Logger logger = LoggerFactory.getLogger(ControllerEngine.class);
    private static ControllerEngine ourInstance = new ControllerEngine();
    protected ControllerAdvertiser controllerAdvertiser;     //runs independently, no interaction needed
    DeviceConnection deviceConnection = null;
    ControllerConfig controllerConfig;
    ControllerSettings settings;
    String currentConfigString;

    //protected BroadcastManager broadcastManager;
    private boolean controllerStarted = false;
    private FileServer httpServer;

    private ControllerEngine() {
        DynamicControl.setDisableScheduler(true);
    }

    public static ControllerEngine getInstance() {
        return ourInstance;
    }

    /**
     * Get the Controller Config
     *
     * @return the Controller config
     */
    public ControllerConfig getControllerConfig() {
        return controllerConfig;
    }

    /**
     * Sets current Controller Config
     * Resets the statically stored configuration to the provided config, and
     * recreates or resets the relevant components (DeviceConnection,
     * FileServer, ControllerAdvertiser).
     *
     * @param controller_config the config
     */
    public void setControllerConfig(ControllerConfig controller_config) {
        controllerConfig = controller_config;

        // Dispose of previous components if necessary.
        if (httpServer != null) {
            logger.debug("Stopping FileServer.");
            httpServer.stop();
        }
        if (controllerAdvertiser != null) {
            logger.debug("Stopping ControllerAdvertiser");
            controllerAdvertiser.stop();
        }
/*
        if (broadcastManager != null) {
            logger.debug("Disposing of BroadcastManager");
            broadcastManager.dispose();
        }
*/
        if (deviceConnection != null) {
            logger.debug("Disposing of DeviceConnection");
            deviceConnection.dispose();
        }

        logger.debug("Compositions path: {}", controllerConfig.getCompositionsPath());


        logger.info("Starting ControllerAdvertiser");
        //broadcastManager = new BroadcastManager(controllerConfig.getMulticastAddr(), listen_port);

        //set up device connection
        deviceConnection = new DeviceConnection(controllerConfig);

        // Do Not start until we are at the end of initialisation
        //controllerAdvertiser.start();

        //setup http httpServer
        try {
            httpServer = new FileServer(controllerConfig);
        } catch (IOException e) {
            logger.error("Unable to start HTTP server!", e);
        }
    }

    public String getCurrentConfigString() {
        return currentConfigString;
    }

    public void setCurrentConfigString(String current_config_string) {
        currentConfigString = current_config_string;
    }

    public DeviceConnection getDeviceConnection() {
        return deviceConnection;
    }

    public ControllerAdvertiser getControllerAdvertiser() {
        return controllerAdvertiser;
    }

    /**
     * Load the Controller Settings from the path specified
     *
     * @param path the path to load settings from
     * @return the Settings instance
     */
    public synchronized ControllerSettings loadSettings(String path) {
        ControllerSettings.setDefaultSettingsFolder(path);
        settings = ControllerSettings.load();
        return settings;
    }

    public ControllerSettings getSettings() {
        return settings;
    }

/*
    public BroadcastManager getBroadcastManager() {
        return broadcastManager;
    }

*/

    /**
     * Make or controller start it network Communication
     */
    public synchronized void startDeviceCommunication() {

        ControlMap.disableControlMimic(true);

        if (controllerAdvertiser == null) {
            deviceConnection.startDeviceConnection();

            deviceConnection.addDeviceAliveListener(device_address -> {
                // this event will not actually happen until we get some sort of advertisement
                if (controllerAdvertiser != null) {
                    controllerAdvertiser.deviceAliveReceived(device_address);
                }
            });

            int listen_port = deviceConnection.getReplyPort();

            controllerAdvertiser = new ControllerAdvertiser(controllerConfig.getMulticastAddr(), controllerConfig.getBroadcastPort(), listen_port);

        }

        if (controllerAdvertiser != null && !controllerStarted) {
            controllerStarted = true;
            controllerAdvertiser.start();

            // Tell Our Scheduler we are conrtroller
            HBScheduler.setDeviceController();
        }
    }

    /**
     * Do a probe of devices. If advertiser has not started, do the start
     */
    public void doProbe() {
        DeviceConnection connection = this.getDeviceConnection();
        startDeviceCommunication();
        // we will make sure we do not have advertising disabled
        connection.setDisableAdvertise(false);

        // we will do a single broadcast type advertise if broadcast is not enabled
        ControllerAdvertiser advertiser = getControllerAdvertiser();

        boolean multicast_only = advertiser.isOnlyMulticastMessages();
        if (multicast_only) {
            advertiser.doBroadcastProbe();

        }
    }
}


