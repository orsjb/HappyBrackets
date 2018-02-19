package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

public class SendCompositionToAllDevicesAction extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        VirtualFile vfile = selectedFile(e);
        boolean enable = false;

        if (vfile != null) {

            if (vfile.getExtension().equalsIgnoreCase("java"))
            {
                String fileName = vfile.getNameWithoutExtension();
                e.getPresentation().setText("Send " + fileName + " to all devices");
                enable = true;
            }

        }

        e.getPresentation().setEnabled(enable);

    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = selectedFile(e);

        if (vfile != null) {
            String fileName = vfile.getName();
            System.out.println(fileName);
        }
    }


}
