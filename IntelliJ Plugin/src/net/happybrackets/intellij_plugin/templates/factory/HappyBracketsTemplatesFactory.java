/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package net.happybrackets.intellij_plugin.templates.factory;

import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.util.Properties;

/**
 * @version "$Id$"
 */
public class HappyBracketsTemplatesFactory implements FileTemplateGroupDescriptorFactory {

    public static final String ESSENTIALS_VERSION = "1.01.02-SNAPSHOT";

    public static String getTemplateText(HappyBracketsTemplate template) {
        String ret = "";
        final FileTemplate fileTemplate = FileTemplateManager.getInstance().getInternalTemplate(template.getName());
        final Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());

        try {
            ret = fileTemplate.getText(properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load template for " + template.getName(), e);
        }
        return ret;
    }

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        String title = "HappyBrackets templates";
        final Icon icon = IconLoader.getIcon("/icons/hb.png");
        final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(title, icon);
        for (HappyBracketsTemplate template : HappyBracketsTemplate.values()) {
            group.addTemplate(new FileTemplateDescriptor(template.getName(), icon));
        }
        return group;
    }

    public enum HappyBracketsTemplate {
        HAPPY_BRACKETS_TEMPLATE("HappyBracketsProject"),
        HAPPY_BRACKETS_SKETCH("HappyBracketsSketch"),
        HAPPY_BRACKETS_MODULES("modules"),
        HAPPY_BRACKETS_WORKSPACE("workspace"),
        ;

        final String name;

        HappyBracketsTemplate(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }


}
