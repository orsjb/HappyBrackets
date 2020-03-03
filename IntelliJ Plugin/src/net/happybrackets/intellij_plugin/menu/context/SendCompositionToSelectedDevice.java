package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.intellij_plugin.controller.network.LocalDeviceRepresentation;
import net.happybrackets.intellij_plugin.controller.network.SendToDevice;

import java.util.ArrayList;
import java.util.List;

import static net.happybrackets.intellij_plugin.NotificationMessage.displayNotification;

public class SendCompositionToSelectedDevice extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        try {
            if (e != null) {
                VirtualFile vfile = selectedFile(e);
                Project current_project = e.getProject();
                LocalDeviceRepresentation selected_device = getSelectedDevice(e.getProject());

                String device_name = "selected device";

                if (selected_device != null) {
                    device_name = selected_device.getFriendlyName();

                }

                boolean enable = false;

                if (vfile != null) {

                    SendDataType sendDataType = getSendDataType(vfile, e);
                    String fileName;

                    switch (sendDataType){
                        case SEND_CLASS:
                            fileName = vfile.getNameWithoutExtension();
                            e.getPresentation().setText("Send " + fileName + " to " + device_name);
                            enable = getClassFile(e) != null && selected_device != null;
                            break;

                        case SEND_FOLDER:
                            fileName = vfile.getName();
                            e.getPresentation().setText("Send folder " + fileName + " to " + device_name);

                            enable = fileInDeviceFolder(current_project, vfile) && selected_device != null;
                            break;

                        case SEND_FILE:
                            fileName = vfile.getName();
                            e.getPresentation().setText("Send file " + fileName + " to " + device_name);
                            enable = fileInDeviceFolder(current_project, vfile) && selected_device != null;
                            break;

                        default:
                            e.getPresentation().setText("Send file to " + device_name);
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

        LocalDeviceRepresentation selected_device = getSelectedDevice(e.getProject());

        if (vfile != null) {

            SendDataType sendDataType = getSendDataType(vfile, e);


            switch (sendDataType){
                case SEND_CLASS:
                    VirtualFile classFile = getClassFile(e);
                    String full_class_name = getFullClassName(classFile.getCanonicalPath());
                    List<LocalDeviceRepresentation> selected = new ArrayList<>();
                    selected.add(selected_device);
                    try {
                        displayNotification("Sending " + classFile.getNameWithoutExtension() + " to " + selected_device.getFriendlyName(), NotificationType.INFORMATION);
                        SendToDevice.send(full_class_name, selected);
                    } catch (Exception e1) {
                        String message = "Unable to send class. The class may not have finished compiling or you may have an error in your code.";
                        displayNotification(message, NotificationType.ERROR);
                    }
                    break;

                case SEND_FOLDER:
                    ArrayList<VirtualFile> files = getFilesInFolder(vfile);

                    for (VirtualFile child:
                         files) {
                        if (!selected_device.sendFileToDevice(child.getCanonicalPath(), getTargetFolder(e.getProject(), child))) {
                            displayNotification("Unable to send file", NotificationType.ERROR);
                        }
                    }

                    break;

                case SEND_FILE:
                    if (!selected_device.sendFileToDevice(vfile.getCanonicalPath(), getTargetFolder(e.getProject(), vfile))) {
                        displayNotification("Unable to send file", NotificationType.ERROR);
                    }
                    break;

                default:
                    displayNotification("Unable to send", NotificationType.WARNING);

            }


        } else{
            displayNotification("Unable to find file", NotificationType.ERROR);
        }
    }



}
