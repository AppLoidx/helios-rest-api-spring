package com.apploidxxx.heliosrestapispring.api.model;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arthur Kupriyanov
 */
@Data@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CurrentAndNextUser {

    public CurrentAndNextUser(com.apploidxxx.heliosrestapispring.entity.user.User currentUser){
        this.currentUser = new User(currentUser);
    }

    public CurrentAndNextUser(com.apploidxxx.heliosrestapispring.entity.user.User currentUser, com.apploidxxx.heliosrestapispring.entity.user.User nextUser) {
        this(currentUser);
        this.nextUser = new User(nextUser);
    }

    private User currentUser;
    private User nextUser;

    @Data
    public static class User {

        public User(com.apploidxxx.heliosrestapispring.entity.user.User user){
            username = user.getUsername();
            fullname = user.getFirstName() + " " + user.getLastName();

            additionalInfo = new HashMap<>();
        }

        String username;
        String fullname;

        Map<String, String > additionalInfo;

    }

}
