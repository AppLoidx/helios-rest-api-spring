package com.apploidxxx.heliosrestapispring.api.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class PasswordTest {
    @Test
    public void password_equals_test(){
        String rawPassword = "password";
        String hashedPassword = Password.hash(rawPassword);

        assertTrue(Password.isEqual(rawPassword, hashedPassword));
        assertFalse(Password.isEqual("another-password", hashedPassword));
        assertFalse(Password.isEqual(hashedPassword, rawPassword)); // another order
    }

}