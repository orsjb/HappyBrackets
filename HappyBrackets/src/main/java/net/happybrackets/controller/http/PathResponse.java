package net.happybrackets.controller.http;

/**
 * This interface wraps file server responses in a method call response.
 * This allows for a poymorphic response framework.
 *
 * Created by Samg on 19/05/2016.
 */
public interface PathResponse {

    String response();
}
