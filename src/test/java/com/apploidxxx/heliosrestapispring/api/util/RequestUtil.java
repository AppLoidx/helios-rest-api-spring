package com.apploidxxx.heliosrestapispring.api.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arthur Kupriyanov
 */
public class RequestUtil {

    /**
     * Converts the String[] array to Map with key[k] and value[k+1] from
     * k = 0 (+2) to k = array.length - 2
     * <p>
     * if key or value is null - he is skipping this param and don't pass
     * them to result map
     *
     * @param vars string array with key, value ordered
     * @return map of key with values
     */
    public static Map<String, String> getMap(String... vars) {
        Map<String, String> map = new HashMap<>();
        int countLast = vars.length%2!=0?1:0;

        for (int i = vars.length - countLast; i > 1; i = i - 2) {
            if (vars[i - 1] == null || vars[i - 2] == null) continue;
            map.put(vars[i - 2], vars[i - 1]);
        }
        return map;
    }

    public static String generatePathWithParams(String path, Map<String, String> map) {
        if (map.isEmpty()) return path;
        StringBuilder sb = new StringBuilder();

        for (String k : map.keySet()) {
            if (k == null) continue;
            sb.append(k).append("={").append(k).append("}&");
        }
        return path + "?" + sb.toString().substring(0, sb.length() - 1);
    }
}