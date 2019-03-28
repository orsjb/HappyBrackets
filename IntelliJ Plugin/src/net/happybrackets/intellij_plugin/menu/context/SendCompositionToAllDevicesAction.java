package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.SendToDevice;

import java.util.ArrayList;
import java.util.List;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class SendCompositionToAllDevicesAction extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        try {
            if (e != null) {
                VirtualFile vfile = selectedFile(e);
                boolean enable = false;
                Project current_project = e.getProject();

                if (vfile != null) {

                    SendDataType sendDataType = getSendDataType(vfile, e);
                    String fileName;

                    switch (sendDataType){
                        case SEND_CLASS:
                            fileName = vfile.getNameWithoutExtension();
                            e.getPresentation().setText("Send " + fileName + " to all devices");
                            enable = getClassFile(e) != null && getDevices().size() > 0;
                            break;

                        case SEND_FOLDER:
                            fileName = vfile.getName();
                            e.getPresentation().setText("Send folder " + fileName + " to all devices");

                            enable = fileInDeviceFolder(current_project, vfile)  && getDevices().size() > 0;
                            break;

                        case SEND_FILE:
                            fileName = vfile.getName();
                            e.getPresentation().setText("Send file " + fileName + " to all devices");
                            enable = fileInDeviceFolder(current_project, vfile)  && getDevices().size() > 0;
                            break;

                        default:
                            e.getPresentation().setText("Send file to all devices");
                            enable = false;
                    }


                }

                e.getPresentation().setEnabled(enable);
            }
        }
        catch (Exception ex){
            if (e != null){
                try {
                    e.getPresentation().setEnabled(false);
                }
                catch (Exception ex2){}
            }
        }

    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = selectedFile(e);

        if (vfile != null) {

            SendDataType sendDataType = getSendDataType(vfile, e);

            switch (sendDataType){
                case SEND_CLASS:
                    VirtualFile classFile = getClassFile(e);
                    String full_class_name = getFullClassName(classFile.getCanonicalPath());

                    try {
                        SendToDevice.send(full_class_name, getDevices());
                    } catch (Exception e1) {
                        displayNotification(e1.getMessage(), NotificationType.ERROR);
                    }
                    break;

                case SEND_FOLDER:
                    for (LocalDeviceRepresentation device:
                            getDevices()) {
                        if (!device.sendFolderToDevice(vfile, getTargetFolder(e.getProject(), vfile))) {
                            displayNotification("Unable to send folder", NotificationType.ERROR);
                        }
                    }

                    break;

                case SEND_FILE:
                    for (LocalDeviceRepresentation device:
                            getDevices()) {
                        if (!device.sendFileToDevice(vfile, getTargetFolder(e.getProject(), vfile))) {
                            displayNotification("Unable to send file", NotificationType.ERROR);
                        }
                    }
                    break;

                default:
                    displayNotification("Unable to send", NotificationType.WARNING);

            }

        }
        else{
            displayNotification("Unable to find file", NotificationType.ERROR);
        }

    }


}
