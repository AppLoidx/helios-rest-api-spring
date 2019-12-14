package com.apploidxxx.heliosrestapispring.api.util;

/**
 *
 * Note: Not completed
 *
 * @author Arthur Kupriyanov
 */
public class RedirectUriChecker {

    /**
     * Not completed
     * @param redirectUri redirect uri which need to check
     * @return true if redirectUri don't equals to empty string
     */
    public static boolean checkIsSafe(String redirectUri){

        // it's not complete implementation
        // this method will contain redirect uri check
        // which connects to data source and compare with app sage uri
        // for OAuth 2.0

        return !"".equals(redirectUri);
    }
}
