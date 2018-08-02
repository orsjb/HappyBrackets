package net.happybrackets.intellij_plugin.templates.factory;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;

import net.happybrackets.intellij_plugin.templates.project.HappyBracketsProject;
import org.jetbrains.annotations.NotNull;
import com.intellij.platform.ProjectTemplatesFactory;

import java.io.File;
import java.io.IOException;

public class HappyBracketsProjectFactory extends ProjectTemplatesFactory {

    public static final String[] HB_GROUP = new String[]{"HappyBrackets"};

    @NotNull
    @Override
    public String[] getGroups() {
        return HB_GROUP;
    }

    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(final String s, final WizardContext wizardContext) {
        final ProjectTemplate[] projectTemplates = new ProjectTemplate[1];
        projectTemplates[0] = new HappyBracketsProject();
        return projectTemplates;
    }
}
