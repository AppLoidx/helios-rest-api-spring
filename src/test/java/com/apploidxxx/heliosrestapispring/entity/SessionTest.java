package com.apploidxxx.heliosrestapispring.entity;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class SessionTest {

    @Test
    public void generateSession() {
        User user = new User();
        Session session = new Session();
        session.generateSession(user);

        assertNotNull(session.getAccessToken());
        assertNotNull(session.getRefreshToken());
        assertNotNull(user.getSession());
        assertEquals(user.getSession(), session);
    }
}