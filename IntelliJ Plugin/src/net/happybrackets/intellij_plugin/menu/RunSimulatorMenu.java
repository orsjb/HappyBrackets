package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.IconLoader;
import net.happybrackets.intellij_plugin.SimulatorShell;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.ControllerAdvertiser;
import net.happybrackets.intellij_plugin.controller.network.DeviceConnection;

public class RunSimulatorMenu extends AnAction {

    // Add these global variables so we can run this while debugging the plugin
    static String lastSdkPath = "";
    static String lastProjectPath = "";
    // Flag to store if they had multicast on when they ran simulator
    boolean multicastOnly = false;

    public static String getLastSdkPath() {
        return lastSdkPath;
    }

    public static String getLastProjectPath() {
        return lastProjectPath;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {

            Project current_project = e.getProject();
            ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);
            Sdk sdk = rootManager.getProjectSdk();

            String sdk_path = sdk.getHomePath() + "/bin/";

            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            ControllerEngine.getInstance().startDeviceCommunication();
            // we will make sure we do not have advertising disabled
            connection.setDisableAdvertise(false);

            String project_path = current_project.getBaseDir().getCanonicalPath();

            ControllerAdvertiser advertiser = ControllerEngine.getInstance().getControllerAdvertiser();

            if (SimulatorShell.isRunning()) {
                SimulatorShell.killSimulator();

                // we only want
                advertiser.setSendLocalHost(false);
            } else {
                //displayNotification("Try run Simulator at " + project_path, NotificationType.INFORMATION);
                if (SimulatorShell.runSimulator(sdk_path, project_path)) {
                    // we need to advertise on localhost
                    advertiser.setSendLocalHost(true);
                }
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void update(AnActionEvent event) {
        try {
            String menu_text = "Run Simulator";
            String menu_icon = "/icons/play.png";

            Project current_project = event.getProject();

            String project_path = current_project.getBaseDir().getCanonicalPath();

            ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);
            Sdk sdk = rootManager.getProjectSdk();

            lastSdkPath = sdk.getHomePath() + "/bin/";
            lastProjectPath = project_path;

            boolean simulator_exists = SimulatorShell.simulatorExists(project_path);

            if (SimulatorShell.isRunning()) {
                menu_text = "Stop Simulator";
                menu_icon = "/icons/stop.png";
                event.getPresentation().setEnabled(true);
            } else {
                event.getPresentation().setEnabled(simulator_exists);
            }

            event.getPresentation().setText(menu_text);
            event.getPresentation().setIcon(IconLoader.getIcon(menu_icon));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
