/*
 * Copyright 2013 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.intellij_plugin.templates.project;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import net.happybrackets.intellij_plugin.templates.factory.HappyBracketsTemplatesFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;

/**
 * @version "$Id$"
 */
public class HappyBracketsProject extends WebProjectTemplate {

    private static final Logger log = Logger.getInstance(HappyBracketsProject.class);

    public static final String HAPPY_BRACKETS_PROJECT_NAME =  "HappyBrackets Project";
    public static final String HAPPY_BRACKETS_PROJECT_IML = HAPPY_BRACKETS_PROJECT_NAME + ".iml";

    static final  String WORKSPACE_FILE = ".idea" + File.separatorChar + "workspace.xml";
    static final  String MODULES_FILE = ".idea" + File.separatorChar + "modules.xml";

    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/logo.png");
    }

    @NotNull
    @Override
    public ModuleBuilder createModuleBuilder() {
        //ModuleBuilder ret = new JavaModuleType().createModuleBuilder();
        ModuleBuilder ret = super.createModuleBuilder(); //ModuleBuilder. JavaModuleType;
        return ret;
    }


    @Nls
    @NotNull
    @Override
    public String getName() {
        return "HappyBrackets Project";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "<html>HappyBrackets Project</html>";
    }

    @Override
    @Nullable
    public Integer getPreferredDescriptionWidth() {
        return 390;
    }

    @SuppressWarnings("InstanceofInterfaces")
    @Override
    public void generateProject(@NotNull final Project project, @NotNull final VirtualFile baseDirectory, @NotNull final Object settings, @NotNull final Module module) {

        if (!(settings instanceof SettingsData)) {
            return;
        }

        if (baseDirectory.getCanonicalPath() == null) {
            return;
        }

        // unzip our archived project
        ProjectUnzip unzip = new ProjectUnzip();

        unzip.addSkipFile(HAPPY_BRACKETS_PROJECT_IML);
        unzip.addSkipFile(MODULES_FILE);
        unzip.addSkipFile(WORKSPACE_FILE);


        try {
            unzip.unzipReseourceProject(File.separatorChar + "projectTemplates" + File.separatorChar + "HappyBracketsProject.zip", baseDirectory.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        String project_text = HappyBracketsTemplatesFactory.getTemplateText(HappyBracketsTemplatesFactory.HappyBracketsTemplate.HAPPY_BRACKETS_TEMPLATE).replace(HAPPY_BRACKETS_PROJECT_NAME, module.getName());;

        String workspace_text = HappyBracketsTemplatesFactory.getTemplateText(HappyBracketsTemplatesFactory.HappyBracketsTemplate.HAPPY_BRACKETS_WORKSPACE).replace(HAPPY_BRACKETS_PROJECT_NAME, module.getName());;

        String modules_text = HappyBracketsTemplatesFactory.getTemplateText(HappyBracketsTemplatesFactory.HappyBracketsTemplate.HAPPY_BRACKETS_MODULES).replace(HAPPY_BRACKETS_PROJECT_NAME, module.getName());

        String project_filename = baseDirectory.getCanonicalPath() + File.separatorChar + module.getName() + ".iml";
        String workspace_filename = baseDirectory.getCanonicalPath() + File.separatorChar + WORKSPACE_FILE;
        String modules_filename = baseDirectory.getCanonicalPath() + File.separatorChar + MODULES_FILE;


        Path file = Paths.get(project_filename );
        try {
            Files.write(file, Collections.singleton(project_text), Charset.forName("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        file = Paths.get(workspace_filename);
        try {
            Files.write(file, Collections.singleton(workspace_text), Charset.forName("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        file = Paths.get(modules_filename);
        try {
            Files.write(file, Collections.singleton(modules_text), Charset.forName("UTF-8"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        LocalFileSystem.getInstance().refresh(true);


        StartupManager.getInstance(project).runWhenProjectIsInitialized(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {

                        final String rootPath = baseDirectory.getCanonicalPath();
                        final PsiDirectory rootDirectory = PsiManager.getInstance(project).findDirectory(getVirtualFile(rootPath));

                        String project_name = project.getName();
                        if (rootDirectory == null) {
                            return;
                        }

                        // we need to do unzip again because project has been reloaded
                        try {
                            unzip.unzipReseourceProject(File.separatorChar + "projectTemplates" + File.separatorChar + "HappyBracketsProject.zip", baseDirectory.getCanonicalPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        // we need to write these files again becasue projec create would ave overwritten them
                        Path file = Paths.get(project_filename );
                        try {
                            Files.write(file, Collections.singleton(project_text), Charset.forName("UTF-8"));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        file = Paths.get(workspace_filename);
                        try {
                            Files.write(file, Collections.singleton(workspace_text), Charset.forName("UTF-8"));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        file = Paths.get(modules_filename);
                        try {
                            Files.write(file, Collections.singleton(modules_text), Charset.forName("UTF-8"));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        LocalFileSystem.getInstance().refresh(true);
                    }

                });
            }
        });


    }

    @SuppressWarnings("rawtypes")
    @NotNull
    @Override
    public GeneratorPeer createPeer() {
        return new HappyBracketsGeneratorPeer();
    }

    private VirtualFile getVirtualFile(String path) {
        File pluginPath = new File(path);

        if (!pluginPath.exists()) {
            return null;
        }

        String url = VfsUtilCore.pathToUrl(pluginPath.getAbsolutePath());

        return VirtualFileManager.getInstance().findFileByUrl(url);
    }

}
