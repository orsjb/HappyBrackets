/*
 * Copyright 2016 Ollie Bown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.happybrackets.intellij_plugin.controller.http;

/**
 * A response object which wraps up the FileServer.readFile method and supplies a default suffix.
 * This method is designed for serving config files which fall back to a (file_name).suffix convention when (file_name) cannot be found.
 * <p>
 * Created by Samg on 19/05/2016.
 */
public class WithDefaultResponse implements PathResponse {

    private String configPath;
    private String defaultSuffix;

    public WithDefaultResponse(String configPath, String defaultSuffix) {
        this.configPath = configPath;
        this.defaultSuffix = defaultSuffix;
    }

    @Override
    public String response() {
        return FileServer.readFile(configPath, "utf8", defaultSuffix);
    }
}
