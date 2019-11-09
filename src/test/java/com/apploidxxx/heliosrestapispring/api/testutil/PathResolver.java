package com.apploidxxx.heliosrestapispring.api.testutil;


/**
 * @author Arthur Kupriyanov
 */
public class PathResolver {

    private static final String host = "http://localhost";
    private static final String pathToApi = "/api";

    public static String getEndpointPath(String endpointName, int port){
        if (endpointName.startsWith("/")) endpointName = endpointName.substring(1);
        return String.format("%s:%d%s/%s", host, port,pathToApi, endpointName);
    }

    public static String getHost(){ return host;}
    public static String getPathToApi() { return pathToApi;}
}
