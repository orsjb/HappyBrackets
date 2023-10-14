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

package net.happybrackets.intellij_plugin.templates.project;

import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.platform.WebProjectGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 * @version "$Id$"
 */
public class HappyBracketsGeneratorPeer implements WebProjectGenerator.GeneratorPeer<SettingsData> {
    SettingsData data = new SettingsData();

    private JPanel myMainPanel;

    private JTextField vendorName;


    public HappyBracketsGeneratorPeer() {
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myMainPanel;
    }

    @Override
    public void buildUI(@NotNull final SettingsStep settingsStep) {
        //settingsStep.addSettingsField("Vendor", vendorName);
        //settingsStep.addSettingsField("Plugin id", pluginName);
        //settingsStep.addSettingsField("Group id", groupId);
        //settingsStep.addSettingsField("Artifact id", artifactId);
        //settingsStep.addSettingsField("Version", version);
        //settingsStep.addSettingsField("Create REST skeleton", createRESTClassCheckBox);
        //settingsStep.addSettingsField("Plugin group", pluginGroup);
    }

    @NotNull
    @Override
    public SettingsData getSettings() {

        getData(data);
        return data;
    }

    @Nullable
    @Override
    public ValidationInfo validate() {

        // Returning null is the same as it is OK
        return null;
    }


    @Override
    public boolean isBackgroundJobRunning() {
        return false;
    }

    @Override
    public void addSettingsStateListener(@NotNull final WebProjectGenerator.SettingsStateListener settingsStateListener) {

    }

    private void createUIComponents() {
    }


    public void setData(SettingsData data) {

        vendorName.setText(data.getVendor());

    }

    public void getData(SettingsData data) {

        data.setVendor(vendorName.getText());

    }

    public boolean isModified(SettingsData data) {

        if (vendorName.getText() != null ? !vendorName.getText().equals(data.getVendor()) : data.getVendor() != null) {
            return true;
        }

        return false;
    }


}
