package com.apploidxxx.heliosrestapispring.api.testutil;

import com.apploidxxx.heliosrestapispring.api.util.Password;
import com.apploidxxx.heliosrestapispring.entity.user.User;
import com.apploidxxx.heliosrestapispring.entity.user.UserType;

import java.util.Base64;
import java.util.Date;
import java.util.Random;

/**
 * @author Arthur Kupriyanov
 */
public abstract class UserBuilder {

    public static class UserBuilderNode{
        private final User user;
        private UserBuilderNode(User user){
            this.user = user;
        }

        public UserBuilderNode withName(String name){
           user.setUsername(name);
           return this;
        }

        public UserBuilderNode withPassword(String password){
            user.setPassword(Password.hash(password));
            return this;
        }

        public UserBuilderNode withFirstName(String firstName){
            user.setFirstName(firstName);
            return this;
        }

        public UserBuilderNode withRandomFirstName(){
            user.setFirstName(generateName());
            return this;
        }

        public UserBuilderNode withLastName(String lastName){
            user.setLastName(lastName);
            return this;
        }

        public UserBuilderNode withRandomLastName(){
            user.setLastName(generateName());
            return this;
        }

        public UserBuilderNode withEmail(String email){
            user.getContactDetails().setEmail(email);
            return this;
        }

        public UserBuilderNode withUsertype(UserType usertype){
            user.setUserType(usertype);
            return this;
        }

        public User build(){
            return user;
        }

    }

    public static UserBuilderNode createUser(){
        User user = new User(generateName(), Password.hash(generatePassword()), "firstName", "lastName", generateName() + "@mail.example" );
        return new UserBuilderNode(user);
    }

    private static String generateName(){
        long date = new Date().getTime() + new Random().nextInt();
        return Base64.getEncoder().encodeToString((date + "").getBytes());
    }

    public static String generatePassword(){
        long date = new Date().getTime() + new Random().nextInt();
        String password = Base64.getEncoder().encodeToString((date + "").getBytes());
        if (password.length() < 10) return generatePassword() + generateName();
        return password;
    }
}
