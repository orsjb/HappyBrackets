package net.happybrackets.intellij_plugin.menu.global;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.happybrackets.intellij_plugin.controller.ControllerEngine;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;

import java.util.List;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class NoteAppears extends AnAction {
    final String TEXT_EXTENSION = "txt";

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        try {
            String display = "hakushu was here";

//            List<LocalDeviceRepresentation> devicesByHostname = ControllerEngine.getInstance().getDeviceConnection().getAllActiveDevices();
//
//            String display = "Device IP Addresses";
//            if (devicesByHostname.size() > 0) {
//
//                for (LocalDeviceRepresentation device : devicesByHostname) {
//                    if (device.getIsConnected()) {
//                        String address = device.getAddress();
//                        String hostname = device.hostName;
//                        display += "\n" + address + "\t" + hostname;
//
//                    }
//
//                }
                displayNotification(display, NotificationType.INFORMATION);
//            }
        } catch (Exception ex) {
        }

    }

    @Override
    public void update(AnActionEvent event) {
        try {
            if(ControllerEngine.getInstance().getDeviceConnection()== null){
                System.out.println("NoteAppears: geteDeviceConnection == null!!");
                return;
            }
            List<LocalDeviceRepresentation> device_address = ControllerEngine.getInstance().getDeviceConnection().getAllActiveDevices();

            event.getPresentation().setEnabled(device_address.size() > 0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT; // Use EDT (Event Dispatch Thread)
    }
}
