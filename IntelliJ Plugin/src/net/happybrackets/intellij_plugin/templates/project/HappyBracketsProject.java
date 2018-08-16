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

/**
 * @version "$Id$"
 */
public class HappyBracketsProject extends WebProjectTemplate {

    private static final Logger log = Logger.getInstance(HappyBracketsProject.class);

    public static final String HAPPY_BRACKETS_PROJECT_NAME =  "HappyBrackets Project";


    static final  String WORKSPACE_FILE = ".idea/workspace.xml";
    public static final  String MODULES_FILE = ".idea/modules.xml";
    static final  String DESCRIPTION_FILE = ".idea/description.html";

    /**
     * Add The files from the project archive we want to skip
     */
    public static final String [] ARCHIVE_SKIP_FILES = new String[]{
            HAPPY_BRACKETS_PROJECT_NAME + ".iml",
            //MODULES_FILE,
            WORKSPACE_FILE,
            ".idea/gradle.xml",
            DESCRIPTION_FILE,
            ".idea/dictionaries/ollie.xml",
            "config/controller-config.json",
            "config/update-developer-kit.command",
            "config/update-developer-kit.sh"


    };

    public static final String HAPPY_BRACKETS_PROJECT_IML = HAPPY_BRACKETS_PROJECT_NAME + ".iml";

    public static final String HAPPY_BRACKETS_PROJECT_ZIP = "/projectTemplates/HappyBracketsProject.zip";
    public static final String HAPPY_BRACKETS_JAR_ZIP = "/projectTemplates/HB.zip";
    public static final String HAPPY_BRACKETS_JAVDOCS_ZIP = "/projectTemplates/JavaDocs.zip";

    public static final String HB_JAVADOCS_FOLDER = File.separatorChar + "libs" + File.separatorChar +  "docs" + File.separatorChar + "hb" ;

    // THis is where we need to extract the HB.jar files to in a new project
    public static final String [] HB_JAR_LOCATION = new String[]{
            File.separatorChar + "Device" + File.separatorChar + "HappyBrackets",
            File.separatorChar + "libs"
    };


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

        // do not add the files we are going to overwrite
        for (int i= 0; i < ARCHIVE_SKIP_FILES.length; i++) {
            unzip.addSkipFile(ARCHIVE_SKIP_FILES[i]);
        }

        String base_path = baseDirectory.getCanonicalPath();

        try {
            unzip.unzipReseourceProject( HAPPY_BRACKETS_PROJECT_ZIP, base_path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now unzip HB.jar into Device and Project
        try {
            for (int i = 0; i < HB_JAR_LOCATION.length; i++) {
                unzip.unzipReseourceProject(HAPPY_BRACKETS_JAR_ZIP, base_path + HB_JAR_LOCATION[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // now unzip javadocs
        try {
            unzip.unzipReseourceProject(HAPPY_BRACKETS_JAVDOCS_ZIP, base_path + HB_JAVADOCS_FOLDER);
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

                        try {
                            final String rootPath = baseDirectory.getCanonicalPath();
                            final PsiDirectory rootDirectory = PsiManager.getInstance(project).findDirectory(getVirtualFile(rootPath));

                            String project_name = project.getName();
                            if (rootDirectory == null) {
                                return;
                            }

                            // we need to do unzip again because project has been reloaded
                            try {
                                unzip.unzipReseourceProject(HAPPY_BRACKETS_PROJECT_ZIP, baseDirectory.getCanonicalPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            // we need to write these files again becasue projec create would ave overwritten them
                            Path file = Paths.get(project_filename);
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
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
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
