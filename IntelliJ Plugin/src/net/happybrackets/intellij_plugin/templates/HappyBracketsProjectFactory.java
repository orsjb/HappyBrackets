package net.happybrackets.intellij_plugin.templates;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.platform.ProjectTemplate;

import com.intellij.platform.templates.ArchivedTemplatesFactory;
import com.intellij.platform.templates.LocalArchivedTemplate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.impl.UrlUtil;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.ClearableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.platform.ProjectTemplate;
import com.intellij.platform.ProjectTemplatesFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import java.net.URL;

public class HappyBracketsProjectFactory extends ArchivedTemplatesFactory {

    void displayDialog(String text)
    {
        new Thread(() -> {
            try {

                JOptionPane.showMessageDialog(null,
                        text);


            } catch (Exception ex) {
            }
        }).start();
    }

    public static final String HAPPY_BRACKETS_GROUP = "Happy Brackets";

    static final String ZIP = ".zip";
    private final ClearableLazyValue<MultiMap<String, Pair<URL, ClassLoader>>> myGroups = new ClearableLazyValue<MultiMap<String, Pair<URL, ClassLoader>>>() {
        @NotNull
        @Override
        protected MultiMap<String, Pair<URL, ClassLoader>> compute() {
            MultiMap<String, Pair<URL, ClassLoader>> map = new MultiMap<String, Pair<URL, ClassLoader>>();
            Map<URL, ClassLoader> urls = new HashMap<URL, ClassLoader>();
            //for (IdeaPluginDescriptor plugin : plugins) {
            //  if (!plugin.isEnabled()) continue;
            //  try {
            //    ClassLoader loader = plugin.getPluginClassLoader();
            //    Enumeration<URL> resources = loader.getResources("resources/projectTemplates");
            //    ArrayList<URL> list = Collections.list(resources);
            //    for (URL url : list) {
            //      urls.put(url, loader);
            //    }
            //  }
            //  catch (IOException e) {
            //    LOG.error(e);
            //  }
            //}
            URL configURL = getCustomTemplatesURL();
            if (configURL != null) {
                displayDialog(" getCustomTemplatesURL" + configURL);
                urls.put(configURL, ClassLoader.getSystemClassLoader());
            }
            else
            {
                displayDialog(" getCustomTemplatesURL - null");
            }
            for (Map.Entry<URL, ClassLoader> url : urls.entrySet()) {
                try {
                    List<String> children = UrlUtil.getChildrenRelativePaths(url.getKey());
                    Boolean url_match = configURL == url.getKey();
                    if (url_match && !children.isEmpty()) {
                        map.putValue(HAPPY_BRACKETS_GROUP, Pair.create(url.getKey(), url.getValue()));
                        continue;
                    }
                    for (String child : children) {
                        int index = child.indexOf('/');
                        if (index != -1) {
                            child = child.substring(0, index);
                        }
                        String name = child.replace('_', ' ');
                        map.putValue(name, Pair.create(new URL(url.getKey().toExternalForm() + "/" + child), url.getValue()));
                    }
                }
                catch (IOException e) {
                    LOG.error(e);
                }
            }
            return map;
        }
    };

    private  URL getCustomTemplatesURL() {
        String path = getCustomTemplatesPath();

        try {
            return new File(path).toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }


    }

    String getCustomTemplatesPath() {
        return this.getClass().getResource("/projectTemplates").getPath();
    }


    @NotNull
    @Override
    public String[] getGroups() {
        myGroups.drop();
        Set<String> groups = myGroups.getValue().keySet();
        return ArrayUtil.toStringArray(groups);
    }
    @NotNull
    @Override
    public ProjectTemplate[] createTemplates(String group, WizardContext context) {
        Collection<Pair<URL, ClassLoader>> urls = myGroups.getValue().get(group);
        List<ProjectTemplate> templates = new ArrayList<ProjectTemplate>();
        for (Pair<URL, ClassLoader> url : urls) {
            try {
                List<String> children = UrlUtil.getChildrenRelativePaths(url.first);
                for (String child : children) {
                    if (child.endsWith(ZIP)) {
                        URL templateUrl = new URL(url.first.toExternalForm() + "/" + child);
                        templates.add(new LocalArchivedTemplate(templateUrl, url.second));
                    }
                }
            }
            catch (IOException e) {
                LOG.error(e);
            }
        }
        return templates.toArray(new ProjectTemplate[templates.size()]);
    }
    @Override
    public int getGroupWeight(String group) {
        return HAPPY_BRACKETS_GROUP.equals(group) ? -2 : 0;
    }
    @Override
    public Icon getGroupIcon(String group) {
        return HAPPY_BRACKETS_GROUP.equals(group) ? AllIcons.Modules.Types.UserDefined : super.getGroupIcon(group);
    }
    private final static Logger LOG = Logger.getInstance(ArchivedTemplatesFactory.class);
}

