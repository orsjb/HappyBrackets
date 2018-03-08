package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.controller.network.LocalDeviceRepresentation;
import net.happybrackets.controller.network.SendToDevice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendCompositionToSelectedDevice extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        try {
            if (e != null) {
                VirtualFile vfile = selectedFile(e);
                LocalDeviceRepresentation selected_device = getSelectedDevice(e.getProject());

                boolean enable = false;

                if (vfile != null) {

                    if (vfile.getExtension().equalsIgnoreCase(JAVA_EXTENSION)) {

                        String fileName = vfile.getNameWithoutExtension();
                        String text = "Send " + fileName + " to ";

                        if (selected_device != null) {
                            String device_name = selected_device.friendlyName();
                            enable = true;
                            text += device_name;
                        } else {
                            text += "selected device";
                        }
                        e.getPresentation().setText(text);

                    }

                }

                e.getPresentation().setEnabled(enable);
            }
        }
        catch (Exception ex){}

    }

    @Override
        public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = getClassFile(e);
        LocalDeviceRepresentation selected_device = getSelectedDevice(e.getProject());



        if (vfile != null) {
            String full_class_name = getFullClassName(vfile.getCanonicalPath());
            List<LocalDeviceRepresentation> selected = new ArrayList<>();
            selected.add(selected_device);
            try {
                SendToDevice.send(full_class_name, selected);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }



}
