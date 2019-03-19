package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.controller.network.DeviceConnection;
import net.happybrackets.intellij_plugin.SimulatorShell;

public class RunSimulatorMenu extends AnAction {


    // Flag to store if they had multicast on when they ran simulator
    boolean multicastOnly = false;

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {

            Project current_project = e.getProject();
            ProjectRootManager rootManager = ProjectRootManager.getInstance(current_project);
            Sdk sdk = rootManager.getProjectSdk();

            String sdk_path =  sdk.getHomePath() + "/bin/";

            DeviceConnection connection = ControllerEngine.getInstance().getDeviceConnection();
            ControllerEngine.getInstance().startDeviceCommunication();
            // we will make sure we do not have advertising disabled
            connection.setDisableAdvertise(false);

            String project_path = current_project.getBaseDir().getCanonicalPath();

            ControllerAdvertiser advertiser = ControllerEngine.getInstance().getControllerAdvertiser();

            if (SimulatorShell.isRunning()){
                SimulatorShell.killSimulator();

                // we only want
                advertiser.setSendLocalHost(false);
            }
            else {
                if (SimulatorShell.runSimulator(sdk_path, project_path)){
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

            Project current_project = event.getProject();

            String project_path = current_project.getBaseDir().getCanonicalPath();

            boolean simulator_exists = SimulatorShell.simulatorExists(project_path);

            if (SimulatorShell.isRunning()) {
                menu_text = "Stop Simulator";
                event.getPresentation().setEnabled(true);
            }
            else{
                event.getPresentation().setEnabled(simulator_exists);
            }

            event.getPresentation().setText(menu_text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
