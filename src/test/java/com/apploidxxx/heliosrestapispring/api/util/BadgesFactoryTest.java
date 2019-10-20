package com.apploidxxx.heliosrestapispring.api.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class BadgesFactoryTest {

    @Test
    public void getBadge() {
        assertEquals(BadgesFactory.ADMIN, BadgesFactory.getBadge("admin"));
        assertEquals(BadgesFactory.DEVELOPER, BadgesFactory.getBadge("developer"));
        assertEquals(BadgesFactory.ADMIN, BadgesFactory.valueOf("ADMIN"));
    }
}