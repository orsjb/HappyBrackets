package net.happybrackets.intellij_plugin.menu.context;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class SendCompositionToSelectedDevice extends SendCompositionAction {

    @Override
    public void update(AnActionEvent e) {
        VirtualFile vfile = selectedFile(e);
        boolean enable = false;

        if (vfile != null) {

            if (vfile.getExtension().equalsIgnoreCase("java"))
            {
                String fileName = vfile.getNameWithoutExtension();
                e.getPresentation().setText("Send " + fileName + " to selected device");
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
