package net.happybrackets.intellij_plugin;

import com.intellij.notification.NotificationType;
import net.happybrackets.core.ShellExecute;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

import java.io.File;
import java.io.IOException;

public class SimulatorShell {
    static final String DEVICE_SCRIPT_PATH = "/Device/HappyBrackets/scripts/";
    static String projectPath = "";
    private static String MAC_SIMULATOR = "run-simulator.sh";
    private static String WIN_SIMULATOR = "run-simulator.bat";

    private static Process simulatorProcess = null;

    /**
     * Run the simulator from the specified project path
     *
     * @param sdk_path     the path that the SDK is. We need this to run correct JDK
     * @param project_path the path to load from
     * @return true on success
     */
    public static boolean runSimulator(String sdk_path, String project_path) {
        boolean ret = false;

        String script_path = project_path + DEVICE_SCRIPT_PATH;

        ShellExecute execute = new ShellExecute();
        execute.addProcessCompleteListener((shellExecute, exit_value) -> {

            try {
                // we have a success
                if (exit_value == 0) {
                    projectPath = project_path;
                    NotificationMessage.displayNotification("Simulator Started", NotificationType.INFORMATION);
                    // store our project path

                } else {
                    NotificationMessage.displayNotification("Unable to determine process ID of simulator", NotificationType.WARNING);
                }
            } catch (Exception ex) {
            }

        });

        try {
            execute.runProcess(script_path + MAC_SIMULATOR, sdk_path);

            //execute.executeCommand(script_path + MAC_SIMULATOR  + " " + sdk_path);
            NotificationMessage.displayNotification("Starting simulator", NotificationType.INFORMATION);
            ret = true;
        } catch (IOException e) {

            // Ok - let us try to run windows version instead
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", WIN_SIMULATOR, sdk_path);
                File dir = new File(script_path);
                pb.directory(dir);
                simulatorProcess = pb.start();
                NotificationMessage.displayNotification("Starting simulator", NotificationType.INFORMATION);
                ret = true;
            } catch (IOException ex) {


                NotificationMessage.displayNotification("Failed to start simulator", NotificationType.ERROR);
            }
        }

        return ret;
    }

    /**
     * See if we have a local simulator riunning on localhost
     *
     * @return true if we have one on a loopback address
     */
    public static boolean isRunning() {

        boolean ret = false;
        for (LocalDeviceRepresentation device :
                ControllerEngine.getInstance().getDeviceConnection().getDevices()) {

            // se if the device is a local simulator
            if (device.isLocalSimulator()) {
                ret = device.getIsConnected();
                break;
            }
        }

        return ret;
    }

    /**
     * If the closing project is the one that started simulator, then kill simulator
     * If this is not the path that the simulator was loaded at, we will ask if we want to shut simulator
     *
     * @param project_path the path of project
     */
    public static void projectClosing(String project_path) {
        try {

            if (!project_path.trim().isEmpty()) {
                if (project_path.equalsIgnoreCase(projectPath)) {
                    killSimulator();
                    projectPath = "";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Kill the simulator based on process ID
     */
    public static void killSimulator() {

        // Also see if we have one running on localhost that we did not start
        LocalDeviceRepresentation local_device = null;
        for (LocalDeviceRepresentation device :
                ControllerEngine.getInstance().getDeviceConnection().getDevices()) {

            // se if the device is a local simulator
            if (device.isLocalSimulator()) {
                local_device = device;
                break;
            }
        }
        if (local_device != null) {
            try {
                local_device.shutdownDevice();
                NotificationMessage.displayNotification("Shutdown sent to simulator", NotificationType.INFORMATION);
                local_device.removeDevice();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Test if the simulator exists in this project
     *
     * @param project_path the project path
     * @return true if s simulator exists
     */
    public static boolean simulatorExists(String project_path) {
        boolean ret = false;

        String script_path = project_path + DEVICE_SCRIPT_PATH;
        File simulator_file = new File(script_path + MAC_SIMULATOR);
        if (simulator_file.exists()) {
            ret = true;
        }

        return ret;
    }
}