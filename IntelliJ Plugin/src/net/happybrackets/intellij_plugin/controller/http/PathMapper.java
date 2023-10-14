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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * A basic class for connecting url paths to response implementing objects.
 * <p>
 * Created by Samg on 19/05/2016.
 */
public class PathMapper {

    final static Logger logger = LoggerFactory.getLogger(PathMapper.class);

    private HashMap<String, PathResponse> pathMap;
    private EmptyResponse empty;

    public PathMapper() {
        empty = new EmptyResponse();
        pathMap = new HashMap<>();
    }

    public boolean addPath(String path, PathResponse response) {
        if (pathMap.containsKey(path)) {
            logger.error("Unable to add path to PathMapper, path already exists! Path: {}", path);
            return false;
        }
        pathMap.put(path, response);
        return true;
    }

    public PathResponse removePath(String path) {
        return pathMap.remove(path);
    }

    public String respond(String path) {
        return pathMap.getOrDefault(path, empty).response();
    }

    protected class EmptyResponse implements PathResponse {

        @Override
        public String response() {
            return null;
        }
    }
}
