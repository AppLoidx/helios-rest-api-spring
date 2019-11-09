package com.apploidxxx.heliosrestapispring.api.testutil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Arthur Kupriyanov
 */
public class PathResolverTest {

    @Test
    public void getEndpointPath() {
        String host = PathResolver.getHost();
        String pathToApi = PathResolver.getPathToApi();
        int randomPort = 5566;

        String expectedUri = host + ":" + randomPort + pathToApi + "/endpoint";
        // example: exceptedUri = http://localhost:5566/api/endpoint

        String actualUri = PathResolver.getEndpointPath("endpoint", randomPort);
        String actualUriWithSlash = PathResolver.getEndpointPath("/endpoint", randomPort);

        assertEquals(expectedUri, actualUri);
        assertEquals(expectedUri, actualUriWithSlash);

        System.out.println(actualUri);
    }
}