package com.apploidxxx.heliosrestapispring.api.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Arthur Kupriyanov
 */
public class RequestUtilTest {
    @Test
    public void generate_path(){
        String expectedPath = "http://example.com/endPoint?access_token={access_token}&username={username}";
        Map<String , String> variables = new HashMap<>();
        variables.put("access_token", "someVal");
        variables.put("username", "someVal");

        String actual = RequestUtil.generatePathWithParams("http://example.com/endPoint", variables);

        assertEquals(expectedPath, actual);
    }

    @Test
    public void getMap(){
        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("v1", "a");
        expectedMap.put("v2", "b");
        expectedMap.put("v3", "c");

        Map<String, String> actual = RequestUtil.getMap("v1", "a", "v2", "b", "v3", "c", "v4", null, "v5");
        assertEquals(expectedMap, actual);

        actual = RequestUtil.getMap("v1", "a", "v2", "b", "v3", "c", "v4", null, "v5", null, null, null);

        assertEquals(expectedMap, actual);
    }
}