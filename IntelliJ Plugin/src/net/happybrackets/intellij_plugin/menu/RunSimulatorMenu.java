package net.happybrackets.intellij_plugin.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.controller.ControllerEngine;
import net.happybrackets.controller.network.ControllerAdvertiser;
import net.happybrackets.intellij_plugin.SimulatorShell;

public class RunSimulatorMenu extends AnAction {


    // Flag to store if they had multicast on when they ran simulator
    boolean multicastOnly = false;

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {

            Project current_project = e.getProject();

            String project_path = current_project.getBaseDir().getCanonicalPath();

            ControllerAdvertiser advertiser = ControllerEngine.getInstance().getControllerAdvertiser();

            if (SimulatorShell.isRunning()){
                SimulatorShell.killSimulator();
                if (multicastOnly){
                    advertiser.setOnlyMulticastMessages(multicastOnly);
                }
            }
            else {
                if (SimulatorShell.runSimulator(project_path)){
                    multicastOnly = advertiser.isOnlyMulticastMessages();

                    // we need broadcast as well as multicast to communicate with simulator
                    advertiser.setOnlyMulticastMessages(false);
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
