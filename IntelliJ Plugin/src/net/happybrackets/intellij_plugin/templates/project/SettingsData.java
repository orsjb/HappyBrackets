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

public class SettingsData {
    private String vendor;

    private String version;
    private String projectName;
    private String projectDirectory;
    private String groupId;
    private String projectPackage;
    private String artifactId;
    private String pluginGroup;
    private boolean createRestSkeleton;


    public SettingsData() {
    }

    public String getPluginGroup() {
        return pluginGroup;
    }

    public void setPluginGroup(final String pluginGroup) {
        this.pluginGroup = pluginGroup;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public boolean isCreateRestSkeleton() {
        return createRestSkeleton;
    }

    public void setCreateRestSkeleton(final boolean createRestSkeleton) {
        this.createRestSkeleton = createRestSkeleton;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(final String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(final String vendor) {
        this.vendor = vendor;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }

    public String getProjectPackage() {
        return projectPackage;
    }

    public void setProjectPackage(final String projectPackage) {
        this.projectPackage = projectPackage;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }
}