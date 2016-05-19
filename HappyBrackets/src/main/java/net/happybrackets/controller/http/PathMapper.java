package net.happybrackets.controller.http;

import java.util.HashMap;

/**
 * A basic class for connecting url paths to response implementing objects.
 *
 * Created by Samg on 19/05/2016.
 */
public class PathMapper {

    private HashMap<String, PathResponse> pathMap;
    private EmptyResponse empty;

    public PathMapper() {
        empty = new EmptyResponse();
        pathMap = new HashMap<>();
    }

    public boolean addPath(String path, PathResponse response) {
        if ( pathMap.containsKey(path) ) {
            System.err.println("Unable to add path to PathMapper, path already exists! Path: " + path);
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
