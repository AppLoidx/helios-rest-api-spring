package com.apploidxxx.heliosrestapispring.api.testutil;

import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class UserBuilderTest {

    @Test
    public void createUser_with_() {
        User expectedUser = new User("1", "2", "3", "4", "5");
        User actualUser = UserBuilder.createUser().withName("1").withPassword("2").withFirstName("3").withLastName("4").withEmail("5").build();

        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        // password will be hashed
        assertEquals(expectedUser.getFirstName(), actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(), actualUser.getLastName());
        assertEquals(expectedUser.getContactDetails().getEmail(), expectedUser.getContactDetails().getEmail());
    }

    @Test
    public void createUser_with_empty_args(){
        User randomGeneratedUser = UserBuilder.createUser().build();
        assertNotNull(randomGeneratedUser);
        assertNotNull(randomGeneratedUser.getUsername());
        assertNotNull(randomGeneratedUser.getPassword());
        assertTrue(randomGeneratedUser.getPassword().length() > 9);
        assertNotNull(randomGeneratedUser.getFirstName());
        assertNotNull(randomGeneratedUser.getLastName());
        assertNotNull(randomGeneratedUser.getContactDetails());
        assertNotNull(randomGeneratedUser.getContactDetails().getEmail());

        System.out.println(randomGeneratedUser);
    }
}