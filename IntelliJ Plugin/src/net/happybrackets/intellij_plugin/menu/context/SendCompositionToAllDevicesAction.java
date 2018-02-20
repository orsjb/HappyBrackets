package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import net.happybrackets.controller.network.SendToDevice;

public class SendCompositionToAllDevicesAction extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        VirtualFile vfile = selectedFile(e);
        boolean enable = false;

        if (vfile != null) {

            if (vfile.getExtension().equalsIgnoreCase(JAVA_EXTENSION))
            {
                String fileName = vfile.getNameWithoutExtension();
                e.getPresentation().setText("Send " + fileName + " to all devices");
                enable = getClassFile(e) != null && getDevices().size() > 0;
            }

        }

        e.getPresentation().setEnabled(enable);

    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = getClassFile(e);

        if (vfile != null) {

            try {
                String full_class_name = getFullClassName(vfile.getCanonicalPath());
                SendToDevice.send(full_class_name, getDevices());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


}
