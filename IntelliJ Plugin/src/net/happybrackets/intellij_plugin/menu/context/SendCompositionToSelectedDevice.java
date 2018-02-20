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

            if (vfile.getExtension().equalsIgnoreCase(JAVA_EXTENSION))
            {
                String fileName = vfile.getNameWithoutExtension();
                e.getPresentation().setText("Send " + fileName + " to selected device");
                enable = getClassFile(e) != null && getDevices().size() > 0;
            }

        }

        e.getPresentation().setEnabled(enable);

    }

    @Override
        public void actionPerformed(AnActionEvent e) {

        VirtualFile vfile = getClassFile(e);

        if (vfile != null) {
            String full_class_name = getFullClassName(vfile.getCanonicalPath());

            System.out.println(full_class_name);
        }
    }


}
