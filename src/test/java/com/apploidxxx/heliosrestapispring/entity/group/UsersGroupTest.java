package com.apploidxxx.heliosrestapispring.entity.group;

import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Arthur Kupriyanov
 */
public class UsersGroupTest {

    @Test
    public void collections_not_null_after_creation(){
        UsersGroup usersGroup = getInstance();
        assertNotNull(usersGroup.getGroupSuperUsers());
        assertNotNull(usersGroup.getUsers());
    }

    @Test
    public void check_password_and_not_password_group(){
        UsersGroup nonPasswordUsersGroup = getInstance();
        UsersGroup withPasswordUsersGroup = getInstance("123");

        assertNull(nonPasswordUsersGroup.getPassword());
        assertNotNull(withPasswordUsersGroup.getPassword());

    }

    @Test
    public void password_equaling(){
        String password = "123";
        UsersGroup usersGroup = getInstance(Password.hash(password));

        assertTrue(Password.isEqual(password, usersGroup.getPassword()));

    }

    private UsersGroup getInstance(){
        return getInstance(null);
    }

    private UsersGroup getInstance(String password){
        return new UsersGroup(new User(), "", "", "", password);
    }

}